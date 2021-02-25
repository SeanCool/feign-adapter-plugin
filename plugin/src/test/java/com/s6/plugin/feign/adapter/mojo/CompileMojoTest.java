package com.s6.plugin.feign.adapter.mojo;

import com.s6.plugin.feign.adapter.tools.ReplacementLibrary;
import com.s6.plugin.feign.adapter.utils.DirectoryTestUtils;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompileMojoTest {
    static class PomArtifactHandler implements ArtifactHandler {
        public String getClassifier() {
            return null;
        }

        public String getDirectory() {
            return null;
        }

        public String getExtension() {
            return "jar";
        }

        public String getLanguage() {
            return "java";
        }

        public String getPackaging() {
            return "jar";
        }

        public boolean isAddedToClasspath() {
            return false;
        }

        public boolean isIncludesDependencies() {
            return false;
        }
    }

    @Test
    public void testExecute() throws Exception {
        URL testJarUrl = this.getClass().getClassLoader().getResource("test-jar.jar");
        assertNotNull(testJarUrl);
        File srcTestJarFile = new File(testJarUrl.getFile());

        MavenProject project = Mockito.mock(MavenProject.class);
        Set<Artifact> artifacts = new HashSet<>();
        Artifact artifact = new DefaultArtifact("com.test", "test-jar", "1.0-SNAPSHOT", null,
            "jar", null, new PomArtifactHandler());
        artifact.setFile(srcTestJarFile);
        artifacts.add(artifact);
        when(project.getDependencyArtifacts()).thenReturn(artifacts);

        Log log = Mockito.mock(Log.class);
        CompileMojo mojo = Mockito.spy(new CompileMojo());
        doReturn(log).when(mojo).getLog();
        mojo.setProject(project);
        File outputDir = new File(DirectoryTestUtils.getAppDir(), "test-compile-mojo");
        FileUtils.deleteDirectory(outputDir);
        mojo.setOutputDirectory(outputDir);

        final String skipLog = "ignore code byte rewrite";
        final String warLog = "repackage goal could not be applied to war project.";
        final String pomLog = "repackage goal could not be applied to pom project.";
        final String emptyLog = "not any libraries need to be replaced";
        // skip
        when(project.getPackaging()).thenReturn("jar");
        mojo.setSkip(true);
        Mockito.reset(log);
        mojo.execute();
        verify(log, times(1)).info(skipLog);
        verify(log, times(0)).info(warLog);
        verify(log, times(0)).info(pomLog);

        // war
        when(project.getPackaging()).thenReturn("war");
        mojo.setSkip(false);
        Mockito.reset(log);
        mojo.execute();
        verify(log, times(0)).info(skipLog);
        verify(log, times(1)).info(warLog);
        verify(log, times(0)).info(pomLog);

        // pom
        when(project.getPackaging()).thenReturn("pom");
        mojo.setSkip(false);
        Mockito.reset(log);
        mojo.execute();
        verify(log, times(0)).info(skipLog);
        verify(log, times(0)).info(warLog);
        verify(log, times(1)).info(pomLog);

        // jar
        when(project.getPackaging()).thenReturn("jar");
        mojo.setSkip(false);
        {
            // empty include
            resetLog(log);
            mojo.setIncludes(null);
            mojo.execute();
            verify(log, times(0)).info(skipLog);
            verify(log, times(0)).info(warLog);
            verify(log, times(0)).info(pomLog);
            verify(log, times(1)).info(emptyLog);
        }
        {
            // to v2
            FileUtils.deleteDirectory(outputDir);
            resetLog(log);

            ReplacementLibrary library = new ReplacementLibrary();
            library.setGroupId("com.test");
            library.setArtifactId("test-jar");
            library.setClasses(new String[] { "com.test.annonation.*" });
            library.setSkipFiles(new String[] { "META-INF/*", "BOOT-INF/lib/*" });
            ReplacementLibrary[] libraries = new ReplacementLibrary[] { library };
            mojo.setIncludes(libraries);
            mojo.execute();
            verify(log, times(0)).info(skipLog);
            verify(log, times(0)).info(warLog);
            verify(log, times(0)).info(pomLog);

            ClassPool classPool = new ClassPool(null);
            classPool.appendSystemPath();
            CtClass cc;
            try (InputStream in = new FileInputStream(new File(outputDir,
                "classes/BOOT-INF/classes/com/test/annonation/MyOldFeignClient.class"))) {
                cc = classPool.makeClass(in);
            }
            assertNull(cc.getAnnotation(org.springframework.cloud.netflix.feign.FeignClient.class));
            assertNotNull(cc.getAnnotation(org.springframework.cloud.openfeign.FeignClient.class));
            try (InputStream in = new FileInputStream(new File(outputDir,
                "classes/BOOT-INF/classes/com/test/annonation/MyNewFeignClient.class"))) {
                cc = classPool.makeClass(in);
            }
            assertNull(cc.getAnnotation(org.springframework.cloud.netflix.feign.FeignClient.class));
            assertNotNull(cc.getAnnotation(org.springframework.cloud.openfeign.FeignClient.class));
            assertFalse(new File(outputDir, "classes/BOOT-INF/lib").exists());
        }

        {
            // to v1
            FileUtils.deleteDirectory(outputDir);
            resetLog(log);

            ReplacementLibrary library = new ReplacementLibrary();
            library.setGroupId("com.test");
            library.setArtifactId("test-jar");
            library.setClasses(new String[] { "com.test.annonation.*" });
            library.setSkipFiles(new String[] { "META-INF/*", "BOOT-INF/lib/*" });
            library.setAdaptVersion("1");
            mojo.setIncludes(new ReplacementLibrary[] { library });

            mojo.execute();
            verify(log, times(0)).info(skipLog);
            verify(log, times(0)).info(warLog);
            verify(log, times(0)).info(pomLog);

            ClassPool classPool = new ClassPool(null);
            classPool.appendSystemPath();
            CtClass cc;
            try (InputStream in = new FileInputStream(new File(outputDir,
                "classes/BOOT-INF/classes/com/test/annonation/MyOldFeignClient.class"))) {
                cc = classPool.makeClass(in);
            }
            assertNull(cc.getAnnotation(org.springframework.cloud.openfeign.FeignClient.class));
            assertNotNull(cc
                .getAnnotation(org.springframework.cloud.netflix.feign.FeignClient.class));
            try (InputStream in = new FileInputStream(new File(outputDir,
                "classes/BOOT-INF/classes/com/test/annonation/MyNewFeignClient.class"))) {
                cc = classPool.makeClass(in);
            }
            assertNull(cc.getAnnotation(org.springframework.cloud.openfeign.FeignClient.class));
            assertNotNull(cc
                .getAnnotation(org.springframework.cloud.netflix.feign.FeignClient.class));
            assertFalse(new File(outputDir, "classes/BOOT-INF/lib").exists());
        }
    }

    private void resetLog(Log log) {
        Mockito.reset(log);
        doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            if (ArrayUtils.isEmpty(args)) {
                return null;
            }
            for (Object o : args) {
                if (o instanceof String) {
                    System.out.print(o);
                }
            }
            System.out.println();
            return null;
        }).when(log).info(anyString());
    }
}