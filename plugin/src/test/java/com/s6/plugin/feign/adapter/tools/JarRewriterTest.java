package com.s6.plugin.feign.adapter.tools;

import com.s6.plugin.feign.adapter.handler.RewriteHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JarRewriterTest {
    @Test
    public void test() throws Exception {
        URL testJarUrl = this.getClass().getClassLoader().getResource("test-jar.jar");
        Assert.assertNotNull(testJarUrl);
        File srcTestJarFile = new File(testJarUrl.getFile());
        JarRewriter jarRewriter = Mockito.spy(new JarRewriter(srcTestJarFile));

        File targetDir = new File(new File("").getAbsoluteFile(), "target");
        File adapterDir = new File(targetDir, "feign-adapter");
        File tmpDirFile = new File(adapterDir, "test-extract/file");

        JarExtractor jarExtractor = Mockito.mock(JarExtractor.class);
        when(jarExtractor.getSourceJarFile()).thenReturn(srcTestJarFile);
        doReturn(jarExtractor).when(jarRewriter).getExtractor();
        Mockito.doAnswer(invocationOnMock -> {
            JarExtractor.Callback callback = invocationOnMock.getArgument(2);
            callback.beforeExtractDirectory(targetDir, "com");
            callback.extractDirectory(targetDir, "com");
            callback.beforeExtractFile(targetDir, "com/a.txt");
            callback.extractFile(targetDir, "com/a.txt");

            callback.beforeExtractDirectory(targetDir, "com/test");
            callback.extractDirectory(targetDir, "com/test");
            callback.beforeExtractFile(targetDir, "com/test/a.txt");
            callback.extractFile(targetDir, "com/test/a.txt");
            return null;
        }).when(jarExtractor).doExtract(Mockito.any(File.class), Mockito.anyBoolean(), Mockito.any(JarExtractor.Callback.class));

        List<RewriteHandler> handlers = new ArrayList<>();
        RewriteHandler handler1 = Mockito.mock(RewriteHandler.class);
        when(handler1.beforeExtractDirectory(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(true);
        when(handler1.beforeExtractFile(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(true);
        when(handler1.rewriteFile(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(false);
        handlers.add(handler1);

        RewriteHandler handler2 = Mockito.mock(RewriteHandler.class);
        when(handler2.beforeExtractDirectory(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(true);
        when(handler2.beforeExtractFile(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(true);
        when(handler2.rewriteFile(Mockito.any(File.class), Mockito.anyString(), Mockito.any(RewriteHandler.class)))
                .thenReturn(true);
        handlers.add(handler2);

        JarRewriter.Callback callback = Mockito.mock(JarRewriter.Callback.class);
        doReturn(true).when(callback).needRewrite(any(File.class));

        jarRewriter.doRewrite(tmpDirFile, handlers, callback);
        // handler1
        verify(handler1, times(1))
                .beforeExtractDirectory(targetDir, "com", null);
        verify(handler1, times(1))
                .beforeExtractDirectory(targetDir, "com/test", null);

        verify(handler1, times(1))
                .extractDirectory(targetDir, "com", null);
        verify(handler1, times(1))
                .extractDirectory(targetDir, "com/test", null);

        verify(handler1, times(1))
                .beforeExtractFile(targetDir, "com/a.txt", null);
        verify(handler1, times(1))
                .beforeExtractFile(targetDir, "com/test/a.txt", null);

        verify(handler1, times(1))
                .rewriteFile(targetDir, "com/a.txt", null);
        verify(handler1, times(1))
                .rewriteFile(targetDir, "com/test/a.txt", null);

        // handler2
        verify(handler2, times(0))
                .beforeExtractDirectory(targetDir, "com", handler1);
        verify(handler2, times(0))
                .beforeExtractDirectory(targetDir, "com/test", handler1);

        verify(handler2, times(1))
                .extractDirectory(targetDir, "com", handler1);
        verify(handler2, times(1))
                .extractDirectory(targetDir, "com/test", handler1);

        verify(handler2, times(0))
                .beforeExtractFile(targetDir, "com/a.txt", handler1);
        verify(handler2, times(0))
                .beforeExtractFile(targetDir, "com/test/a.txt", handler1);

        verify(handler2, times(1))
                .rewriteFile(targetDir, "com/a.txt", handler1);
        verify(handler2, times(1))
                .rewriteFile(targetDir, "com/test/a.txt", handler1);

        jarRewriter.doRewrite(tmpDirFile, handlers);
        verify(handler1, times(2))
                .beforeExtractDirectory(targetDir, "com", null);
    }
}