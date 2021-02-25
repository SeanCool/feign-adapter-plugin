package com.s6.plugin.feign.adapter.handler;

import com.s6.plugin.feign.adapter.code.MyNewFeignClient;
import com.s6.plugin.feign.adapter.code.MyOldFeignClient;
import com.s6.plugin.feign.adapter.matcher.ClassNameMatcher;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static com.s6.plugin.feign.adapter.utils.AssertTestUtils.assertArrayNotEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ClassHandlerTest {
    @Test
    public void test() throws Exception {
        ClassHandler.Callback callback = Mockito.mock(ClassHandler.Callback.class);
        when(callback.oneClass(Mockito.any(CtClass.class))).thenReturn(null);
        ClassHandler handler = new ClassHandler(new ClassNameMatcher(
            new String[] { "com.s6.plugin.*" }), callback);
        File targetDir = new File(new File("").getAbsoluteFile(), "target");
        File adapterDir = new File(targetDir, "feign-adapter");
        File tmpDirFile = new File(adapterDir, "test-rewrite/class");

        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        CtClass oldCtClass = classPool.makeClass(MyOldFeignClient.class.getCanonicalName());
        String codeFileName = MyOldFeignClient.class.getCanonicalName().replace(".", "/")
                              + ".class";

        final byte[] oldBytes = oldCtClass.toBytecode();
        FileUtils.writeByteArrayToFile(new File(tmpDirFile, codeFileName), oldBytes);
        assertFalse(handler.rewriteFile(tmpDirFile, codeFileName, null));
        assertArrayEquals(oldBytes,
            FileUtils.readFileToByteArray(new File(tmpDirFile, codeFileName)));

        CtClass newCtClass = classPool.makeClass(MyNewFeignClient.class.getCanonicalName());
        final byte[] newBytes = newCtClass.toBytecode();
        assertArrayNotEquals(oldBytes, newBytes);

        when(callback.oneClass(Mockito.any(CtClass.class))).thenReturn(newCtClass.toBytecode());
        assertTrue(handler.rewriteFile(tmpDirFile, codeFileName, null));
        assertArrayNotEquals(oldBytes,
            FileUtils.readFileToByteArray(new File(tmpDirFile, codeFileName)));
    }
}