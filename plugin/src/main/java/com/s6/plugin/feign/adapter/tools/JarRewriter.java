package com.s6.plugin.feign.adapter.tools;

import com.s6.plugin.feign.adapter.handler.RewriteHandler;
import com.s6.plugin.feign.adapter.utils.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * jar文件修改器
 *
 * @author Sean
 */
public class JarRewriter {
    public interface Callback {
        /**
         * 是否需要改写通知
         *
         * @param srcFile 待修改文件
         * @return 需要修改返回true，否则返回false
         * @throws IOException IO异常
         */
        boolean needRewrite(File srcFile) throws IOException;
    }

    private JarExtractor extractor;

    public JarRewriter(File srcFile) {
        if (srcFile == null) {
            throw new NullPointerException("source file is null");
        }
        this.extractor = new JarExtractor(srcFile);
    }

    JarExtractor getExtractor() {
        return extractor;
    }

    public void doRewrite(File tmpDir, List<RewriteHandler> handlers) throws IOException {
        doRewrite(tmpDir, handlers, null);
    }

    public int doRewrite(File tmpDir, List<RewriteHandler> handlers, Callback callback)
                                                                                       throws IOException {
        if (CollectionUtils.isEmpty(handlers)) {
            return 0;
        }

        if (callback != null && !callback.needRewrite(getExtractor().getSourceJarFile())) {
            return 0;
        }

        broadcastStart(handlers);

        Context context = new Context(handlers);
        getExtractor().doExtract(tmpDir, false, context);

        broadcastEnd(handlers);
        return context.rewriteCount;
    }

    private void broadcastStart(List<RewriteHandler> handlers) {
        for (RewriteHandler listener : handlers) {
            listener.beforeRewrite();
        }
    }

    private void broadcastEnd(List<RewriteHandler> handlers) {
        for (RewriteHandler handler : handlers) {
            handler.afterRewrite();
        }
    }

    private static class Context implements JarExtractor.Callback {
        int                  rewriteCount;
        List<RewriteHandler> handlers;

        Context(List<RewriteHandler> handlers) {
            this.handlers = handlers;
        }

        @Override
        public boolean beforeExtractDirectory(File rootDir, String relativeDirName)
                                                                                   throws IOException {
            RewriteHandler lastHandler = null;
            for (RewriteHandler handler : handlers) {
                if (!handler.beforeExtractDirectory(rootDir, relativeDirName, lastHandler)) {
                    // 不允许解压
                    return false;
                }
                lastHandler = handler;
            }
            return true;
        }

        @Override
        public void extractDirectory(File rootDir, String relativeDirName) throws IOException {
            RewriteHandler lastHandler = null;
            for (RewriteHandler handler : handlers) {
                handler.extractDirectory(rootDir, relativeDirName, lastHandler);
                lastHandler = handler;
            }
        }

        @Override
        public boolean beforeExtractFile(File rootDir, String relativeName) throws IOException {
            RewriteHandler lastHandler = null;
            for (RewriteHandler handler : handlers) {
                if (!handler.beforeExtractFile(rootDir, relativeName, lastHandler)) {
                    // 不允许解压
                    return false;
                }
                lastHandler = handler;
            }
            return true;
        }

        @Override
        public void extractFile(File rootDir, String relativeName) throws IOException {
            RewriteHandler lastHandler = null;
            boolean changed = false;
            for (RewriteHandler handler : handlers) {
                if (handler.rewriteFile(rootDir, relativeName, lastHandler)) {
                    // 已被改写
                    changed = true;
                    break;
                }
                lastHandler = handler;
            }
            if (changed) {
                rewriteCount++;
            }
        }
    }
}
