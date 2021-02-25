package com.s6.plugin.feign.adapter.handler;

import com.s6.plugin.feign.adapter.matcher.FileNameMatcher;

import java.io.File;
import java.io.IOException;

/**
 * 处理忽略文件
 *
 * @author Sean
 */
public class SkipFileHandler implements RewriteHandler {
    private final FileNameMatcher matcher;

    public SkipFileHandler(FileNameMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean beforeExtractDirectory(File rootDir, String relativeDirName,
                                          RewriteHandler lastHandler) throws IOException {
        return !matcher.matchesName(relativeDirName);
    }

    @Override
    public boolean beforeExtractFile(File rootDir, String relativeName, RewriteHandler lastHandler)
                                                                                                   throws IOException {
        return !matcher.matchesName(relativeName);
    }

    @Override
    public boolean rewriteFile(File baseDir, String relativeFileName, RewriteHandler lastHandler)
                                                                                                 throws IOException {
        return false;
    }
}
