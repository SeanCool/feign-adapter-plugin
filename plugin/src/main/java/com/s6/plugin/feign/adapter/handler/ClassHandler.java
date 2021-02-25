package com.s6.plugin.feign.adapter.handler;

import com.s6.plugin.feign.adapter.matcher.ClassNameMatcher;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 类修改器
 *
 * @author Sean
 */
public class ClassHandler implements RewriteHandler {
    /**
     * 类文件名后缀
     */
    private static final String    CLASS_FILE_SUFFIX = ".class";
    private final Callback         callback;
    /**
     * 类名匹配器
     */
    private final ClassNameMatcher matcher;
    private ClassPool              classPool;

    /**
     * 事件回调
     */
    public interface Callback {
        /**
         * 类修改接口
         *
         * @param ctClass 已解析的类结构描述
         * @return 返回需要修改的非空字节码，不需要修改则返回null或者byte[0]
         * @throws IOException IO异常
         */
        byte[] oneClass(CtClass ctClass) throws IOException;
    }

    public ClassHandler(ClassNameMatcher matcher, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("callback");
        }
        this.matcher = matcher;
        this.callback = callback;
    }

    ClassPool getClassPool() {
        if (classPool == null) {
            classPool = new ClassPool(null);
            classPool.appendSystemPath();
        }
        return classPool;
    }

    @Override
    public boolean rewriteFile(File baseDir, String relativeFileName, RewriteHandler lastListener)
                                                                                                  throws IOException {
        if (relativeFileName == null || !relativeFileName.endsWith(CLASS_FILE_SUFFIX)) {
            return false;
        }

        // 读取字节码
        File classFile = new File(baseDir, relativeFileName);
        byte[] codeBytes = FileUtils.readFileToByteArray(classFile);

        // 解析类
        CtClass cc;
        ClassPool cp = getClassPool();
        try (InputStream in = new ByteArrayInputStream(codeBytes)) {
            cc = cp.makeClass(in);
            cc.defrost();
        }

        String className = cc.getName();
        try {
            if (matcher != null && !matcher.matchesName(className)) {
                return false;
            }

            // 事件通知
            codeBytes = callback.oneClass(cc);
            if (codeBytes == null || codeBytes.length < 1) {
                return false;
            }
        } finally {
            cc.detach(); // 释放内存
        }

        // 代码验证
        try (InputStream in = new ByteArrayInputStream(codeBytes)) {
            cc = cp.makeClass(in);
        } catch (IOException e) {
            throw new IOException("invalid byte code: " + className
                                  + ", check rewrite progress in callback ?", e);
        }
        cc.detach(); // 释放内存

        // 重新写入字节码
        FileUtils.writeByteArrayToFile(classFile, codeBytes);
        return true;
    }
}
