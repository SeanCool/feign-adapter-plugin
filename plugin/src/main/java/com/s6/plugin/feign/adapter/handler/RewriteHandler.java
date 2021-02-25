package com.s6.plugin.feign.adapter.handler;

import java.io.File;
import java.io.IOException;

/**
 * 文件重写处理者
 *
 * @author Sean
 */
public interface RewriteHandler {
    /**
     * 重写前事件通知
     */
    default void beforeRewrite() {
        // do nothing
    }

    /**
     * 单目录解压前事件通知
     *
     * @param rootDir         解压根目录
     * @param relativeDirName 目录相对路径
     * @param lastHandler     上一个handler
     * @return 返回true允许解压，否则跳过此目录
     * @throws IOException IO异常
     */
    default boolean beforeExtractDirectory(File rootDir, String relativeDirName, RewriteHandler lastHandler) throws IOException {
        return true;
    }

    /**
     * 单目录解压完成事件通知
     *
     * @param rootDir         解压根目录
     * @param relativeDirName 目录相对路径
     * @param lastHandler     上一个handler
     * @throws IOException IO异常
     */
    default void extractDirectory(File rootDir, String relativeDirName, RewriteHandler lastHandler) throws IOException {
    }

    /**
     * 单文件解压前事件通知
     *
     * @param rootDir      解压根目录
     * @param relativeName 文件相对路径
     * @param lastHandler  上一个handler
     * @return 返回true允许解压，否则跳过此文件
     * @throws IOException IO异常
     */
    default boolean beforeExtractFile(File rootDir, String relativeName, RewriteHandler lastHandler) throws IOException {
        return true;
    }

    /**
     * 文件重写事件通知
     *
     * @param baseDir          解压根目录
     * @param relativeFileName 文件相对路径
     * @param lastHandler      上一个handler
     * @return 返回true表示已重写，后续handler不再对其处理，否则返回false
     * @throws IOException IO异常
     */
    boolean rewriteFile(File baseDir, String relativeFileName, RewriteHandler lastHandler) throws IOException;

    /**
     * 重写后事件通知
     */
    default void afterRewrite() {
        // do nothing
    }
}
