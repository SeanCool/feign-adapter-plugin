package com.s6.plugin.feign.adapter.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * jar文件解压器
 *
 * @author Sean
 */
public class JarExtractor {
    /**
     * jar文件
     */
    private File sourceJarFile;

    /**
     * 解压事件
     */
    public interface Callback {
        /**
         * 解压前事件
         *
         * @throws IOException IO异常
         */
        default void beforeExtract() throws IOException {
            // do nothing
        }

        /**
         * 目录创建事件
         *
         * @param rootDir         解压根目录
         * @param relativeDirName 目录相对名称
         * @throws IOException IO异常
         */
        default void extractDirectory(File rootDir, String relativeDirName) throws IOException {
            // do nothing
        }


        /**
         * 单个目录创建前事件
         *
         * @param rootDir         解压根目录
         * @param relativeDirName 目录相对名称
         * @return 返回true允许解压，否则跳过此目录
         * @throws IOException IO异常
         */
        default boolean beforeExtractDirectory(File rootDir, String relativeDirName) throws IOException {
            return true;
        }

        /**
         * 单个文件解压前事件
         *
         * @param rootDir      解压根目录
         * @param relativeName 文件相对路径
         * @return 返回true允许解压，否则跳过此文件
         * @throws IOException IO异常
         */
        default boolean beforeExtractFile(File rootDir, String relativeName) throws IOException {
            return true;
        }

        /**
         * 文件解压完成事件
         *
         * @param rootDir      解压根目录
         * @param relativeName 文件相对路径
         * @throws IOException IO异常
         */
        void extractFile(File rootDir, String relativeName) throws IOException;

        /**
         * 解压后事件
         *
         * @throws IOException IO异常
         */
        default void afterExtract() throws IOException {
            // do nothing
        }
    }

    private static final String SEPARATOR_UNIX = "/";
    private static final String SEPARATOR_WIN = "\\";

    public JarExtractor(File srcFile) {
        this.sourceJarFile = srcFile;
    }

    public File getSourceJarFile() {
        return sourceJarFile;
    }

    public void doExtract(File dstDir) throws IOException {
        doExtract(dstDir, true, null);
    }

    public void doExtract(File dstDir, Callback callback) throws IOException {
        doExtract(dstDir, true, callback);
    }

    public void doExtract(File dstDir, boolean delDirBeforeExtract, Callback callback) throws IOException {
        if (callback != null) {
            callback.beforeExtract();
        }
        if (delDirBeforeExtract) {
            // 解压前删除根目录
            FileUtils.deleteDirectory(dstDir);
        }
        if (sourceJarFile.isDirectory()) {
            extractFromDir(dstDir, callback);
        } else {
            extractFromJarFile(dstDir, callback);
        }
        if (callback != null) {
            callback.afterExtract();
        }
    }

    void extractFiles(File dstDir, final int srcDirLen, File file, Callback callback) throws IOException {
        if (!file.exists()) {
            return;
        }

        // 生成相对文件名称，以"/"分隔
        String name = file.getCanonicalPath().substring(srcDirLen);
        if (name.startsWith(SEPARATOR_UNIX) || name.startsWith(SEPARATOR_WIN)) {
            name = name.substring(1);
        }
        name = name.replace(SEPARATOR_WIN, SEPARATOR_UNIX);

        if (file.isFile()) {
            // 创建文件
            try (FileInputStream in = new FileInputStream(file)) {
                createFile(dstDir, in, name, callback);
            }
            return;
        }

        // 创建目录
        createDir(dstDir, name, callback);

        // 遍历循环
        File[] files = file.listFiles();
        if (files == null || files.length < 1) {
            return;
        }
        for (File f : files) {
            extractFiles(dstDir, srcDirLen, f, callback);
        }
    }

    private void extractFromDir(File dstDir, Callback callback) throws IOException {
        File[] files = sourceJarFile.listFiles();
        if (files == null || files.length < 1) {
            return;
        }
        for (File f : files) {
            extractFiles(dstDir, sourceJarFile.getCanonicalPath().length(), f, callback);
        }
    }

    private void extractFromJarFile(File dstDir, Callback callback) throws IOException {
        try (JarInputStream zipis = new JarInputStream(new FileInputStream(sourceJarFile))) {
            JarEntry jarEntry;
            while ((jarEntry = zipis.getNextJarEntry()) != null) {
                // 逐个解压
                if (jarEntry.isDirectory()) {
                    // 创建目录
                    createDir(dstDir, jarEntry.getName(), callback);
                } else {
                    // 创建文件
                    createFile(dstDir, zipis, jarEntry.getName(), callback);
                }
            }
        }
    }

    private void createDir(File dstDir, String relativeDirName, Callback callback) throws IOException {
        if (callback != null && !callback.beforeExtractDirectory(dstDir, relativeDirName)) {
            // 取消目录创建
            return;
        }
        File dir = new File(dstDir, relativeDirName);
        dir.mkdirs();
        if (callback != null) {
            callback.extractDirectory(dstDir, relativeDirName);
        }
    }

    private void createFile(File dstDir, InputStream fileIn, String relativeName, Callback callback) throws IOException {
        if (callback != null && !callback.beforeExtractFile(dstDir, relativeName)) {
            // 取消文件抽取
            return;
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(new File(dstDir, relativeName))) {
            IOUtils.copy(fileIn, out);
        }
        if (callback != null) {
            // 通知文件解压完成
            callback.extractFile(dstDir, relativeName);
        }
    }
}
