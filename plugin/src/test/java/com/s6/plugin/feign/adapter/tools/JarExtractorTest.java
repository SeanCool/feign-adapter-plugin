package com.s6.plugin.feign.adapter.tools;

import com.s6.plugin.feign.adapter.utils.DirectoryTestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JarExtractorTest {
    @Test
    public void test() throws Exception {
        URL testJarUrl = this.getClass().getClassLoader().getResource("test-jar.jar");
        Assert.assertNotNull(testJarUrl);
        File srcTestJarFile = new File(testJarUrl.getFile());
        JarExtractor jarExtractor = new JarExtractor(srcTestJarFile);
        assertEquals(jarExtractor.getSourceJarFile(), srcTestJarFile);

        File tmpDirFile = new File(DirectoryTestUtils.getAppDir(), "test-extract/tmp");
        FileUtils.deleteDirectory(tmpDirFile);

        assertFalse(tmpDirFile.exists());
        jarExtractor.doExtract(tmpDirFile, true, (rootDir, relativeName) -> {
        });
        assertTrue(new File(tmpDirFile, "/BOOT-INF/classes/com/test/app/MyApp.class").exists());
        FileUtils.deleteDirectory(tmpDirFile);
        jarExtractor.doExtract(tmpDirFile);
        assertTrue(new File(tmpDirFile, "/BOOT-INF/classes/com/test/app/MyApp.class").exists());

        // 目录
        jarExtractor = new JarExtractor(tmpDirFile);
        tmpDirFile = new File(DirectoryTestUtils.getAppDir(), "test-extract/dst");
        FileUtils.deleteDirectory(tmpDirFile);

        assertFalse(tmpDirFile.exists());
        jarExtractor.doExtract(tmpDirFile, true, (rootDir, relativeName) -> {
        });
        assertTrue(new File(tmpDirFile, "/BOOT-INF/classes/com/test/app/MyApp.class").exists());
        FileUtils.deleteDirectory(tmpDirFile);
        jarExtractor.doExtract(tmpDirFile);
        assertTrue(new File(tmpDirFile, "/BOOT-INF/classes/com/test/app/MyApp.class").exists());
    }
}