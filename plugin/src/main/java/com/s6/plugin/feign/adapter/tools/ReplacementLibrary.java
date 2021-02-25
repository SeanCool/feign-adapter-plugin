package com.s6.plugin.feign.adapter.tools;

import java.util.Arrays;
import java.util.Objects;

/**
 * 待替换的库
 *
 * @author Sean
 */
public class ReplacementLibrary {
    private String   groupId;
    private String   artifactId;
    private String[] classes;
    private String[] skipFiles;
    private String   adaptVersion;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String[] getClasses() {
        return classes;
    }

    public void setClasses(String[] classes) {
        this.classes = classes;
    }

    public String[] getSkipFiles() {
        return skipFiles;
    }

    public void setSkipFiles(String[] skipFiles) {
        this.skipFiles = skipFiles;
    }

    public String getAdaptVersion() {
        return adaptVersion;
    }

    public void setAdaptVersion(String adaptVersion) {
        this.adaptVersion = adaptVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReplacementLibrary library = (ReplacementLibrary) o;
        return Objects.equals(groupId, library.groupId)
               && Objects.equals(artifactId, library.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }

    @Override
    public String toString() {
        return "ReplacementLibrary{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId
               + '\'' + ", classes=" + Arrays.toString(classes) + ", skipFiles="
               + Arrays.toString(skipFiles) + ", adaptVersion='" + adaptVersion + '\'' + '}';
    }
}
