package com.s6.plugin.feign.adapter.mojo;

import com.s6.plugin.feign.adapter.matcher.ClassNameMatcher;
import com.s6.plugin.feign.adapter.matcher.FileNameMatcher;
import com.s6.plugin.feign.adapter.tools.ReplacementLibrary;
import com.s6.plugin.feign.adapter.handler.ClassHandler;
import com.s6.plugin.feign.adapter.handler.FeignClientHandler;
import com.s6.plugin.feign.adapter.tools.JarRewriter;
import com.s6.plugin.feign.adapter.handler.RewriteHandler;
import com.s6.plugin.feign.adapter.handler.SkipFileHandler;
import com.s6.plugin.feign.adapter.utils.CollectionUtils;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 编译阶段
 *
 * @author Sean
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CompileMojo extends AbstractMojo {
    private static final String  WAR_FILE = "war";
    private static final String  POM_FILE = "pom";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject         project;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File                 outputDirectory;

    /**
     * Skip the repackage goal.
     */
    @Parameter(property = "s6.feign-adapter.compile.skip", defaultValue = "false")
    private boolean              skip;

    @Parameter(property = "includes")
    private ReplacementLibrary[] includes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("ignore code byte rewrite");
            return;
        }
        if (WAR_FILE.equals(this.project.getPackaging())) {
            getLog().info("repackage goal could not be applied to war project.");
            return;
        }
        if (POM_FILE.equals(this.project.getPackaging())) {
            getLog().info("repackage goal could not be applied to pom project.");
            return;
        }
        getLog().info("project.build.directory: " + outputDirectory.getAbsolutePath());

        Map<ReplacementLibrary, Artifact> toReplaceLibs = getReplaceLib2Artifact();
        if (toReplaceLibs.isEmpty()) {
            getLog().info("not any libraries need to be replaced");
            return;
        }
        rewriteLibrary(toReplaceLibs);
    }

    void rewriteLibrary(Map<ReplacementLibrary, Artifact> toReplaceLibs)
                                                                        throws MojoExecutionException {
        File classesDir = new File(outputDirectory, "classes");
        getLog().info("extract files to " + classesDir.getAbsolutePath());
        for (Map.Entry<ReplacementLibrary, Artifact> entry : toReplaceLibs.entrySet()) {
            File srcLib = entry.getValue().getFile();
            getLog().info("replace source: ");
            getLog().info("  library: " + srcLib.getAbsolutePath());

            List<RewriteHandler> handlers = new ArrayList<>();
            // 优先添加skip handler
            handlers.add(getFileSkipHandler(entry.getKey().getSkipFiles()));
            handlers.add(getClassRewriteHandler(entry.getKey().getClasses(), entry.getKey()
                .getAdaptVersion()));
            JarRewriter jarRewriter = new JarRewriter(srcLib);
            try {
                jarRewriter.doRewrite(classesDir, handlers);
            } catch (Exception e) {
                throw new MojoExecutionException("replace error", e);
            }
        }
    }

    Map<ReplacementLibrary, Artifact> getReplaceLib2Artifact() {
        Map<ReplacementLibrary, Artifact> toReplaceLibs = new LinkedHashMap<>();
        if (ArrayUtils.isEmpty(includes)) {
            return toReplaceLibs;
        }
        Set<ReplacementLibrary> librarySet = new LinkedHashSet<>();
        Collections.addAll(librarySet, includes);

        Set<Artifact> dependencies = project.getDependencyArtifacts();
        if (CollectionUtils.isEmpty(dependencies)) {
            return toReplaceLibs;
        }
        Map<ReplacementLibrary, Artifact> depLibs = new LinkedHashMap<>(dependencies.size());
        for (Artifact d : dependencies) {
            ReplacementLibrary lib = new ReplacementLibrary();
            lib.setGroupId(d.getGroupId());
            lib.setArtifactId(d.getArtifactId());
            depLibs.put(lib, d);
        }

        for (ReplacementLibrary lib : librarySet) {
            Artifact dep = depLibs.get(lib);
            if (dep == null) {
                continue;
            }
            toReplaceLibs.put(lib, dep);
        }
        return toReplaceLibs;
    }

    ClassHandler getClassRewriteHandler(String[] classes, String destVersion) {
        destVersion = destVersion == null ? FeignClientHandler.V2 : destVersion;
        getLog().info("destination version: " + destVersion);

        ClassNameMatcher matcher = new ClassNameMatcher(classes);
        List<String> l = matcher.getPattern();
        if (CollectionUtils.isNotEmpty(l)) {
            getLog().info("  classes:");
            for (String s : matcher.getPattern()) {
                getLog().info("    - " + s);
            }
        }
        return new ClassHandler(matcher, new FeignClientHandler(destVersion));
    }

    RewriteHandler getFileSkipHandler(String[] skipFiles) {
        FileNameMatcher matcher = new FileNameMatcher(skipFiles);
        List<String> l = matcher.getPattern();
        if (CollectionUtils.isNotEmpty(l)) {
            getLog().info("  skipFiles:");
            for (String s : matcher.getPattern()) {
                getLog().info("    - " + s);
            }
        }
        SkipFileHandler handler = new SkipFileHandler(matcher);
        return new RewriteHandler() {
            @Override
            public boolean beforeExtractDirectory(File rootDir, String relativeDirName,
                                                  RewriteHandler lastHandler) throws IOException {
                boolean ret = handler.beforeExtractDirectory(rootDir, relativeDirName, lastHandler);
                if (!ret) {
                    getLog().info("ignore directory: " + relativeDirName);
                }
                return ret;
            }

            @Override
            public boolean beforeExtractFile(File rootDir, String relativeName,
                                             RewriteHandler lastHandler) throws IOException {
                boolean ret = handler.beforeExtractFile(rootDir, relativeName, lastHandler);
                if (!ret) {
                    getLog().info("ignore file: " + relativeName);
                }
                return ret;
            }

            @Override
            public boolean rewriteFile(File baseDir, String relativeFileName,
                                       RewriteHandler lastHandler) throws IOException {
                return handler.rewriteFile(baseDir, relativeFileName, lastHandler);
            }
        };
    }

    void setProject(MavenProject project) {
        this.project = project;
    }

    void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    void setSkip(boolean skip) {
        this.skip = skip;
    }

    void setIncludes(ReplacementLibrary[] includes) {
        this.includes = includes;
    }
}
