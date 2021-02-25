package com.s6.plugin.feign.adapter.handler;

import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import java.io.IOException;
import java.util.Set;

/**
 * 修改FeignClient注解
 *
 * @author Sean
 */
public class FeignClientHandler implements ClassHandler.Callback {
    public static final String V1 = "1";
    public static final String V2 = "2";
    private final String       dstVersion;
    private final String       dstClassName;
    private final String       srcClassName;

    public FeignClientHandler(String destVersion) {
        if (V1.equals(destVersion)) {
            dstClassName = org.springframework.cloud.netflix.feign.FeignClient.class
                .getCanonicalName();
            srcClassName = org.springframework.cloud.openfeign.FeignClient.class.getCanonicalName();
        } else if (V2.equals(destVersion)) {
            dstClassName = org.springframework.cloud.openfeign.FeignClient.class.getCanonicalName();
            srcClassName = org.springframework.cloud.netflix.feign.FeignClient.class
                .getCanonicalName();
        } else {
            throw new IllegalArgumentException("unknown version " + destVersion);
        }
        dstVersion = destVersion;
    }

    public String getDstVersion() {
        return dstVersion;
    }

    public String getDstClassName() {
        return dstClassName;
    }

    public String getSrcClassName() {
        return srcClassName;
    }

    @Override
    public byte[] oneClass(CtClass ctClass) throws IOException {
        final byte[] emptyBytes = new byte[0];
        if (!ctClass.isInterface()) {
            return emptyBytes;
        }
        try {
            // 获取所有注解
            ConstPool constpool = ctClass.getClassFile().getConstPool();
            AttributeInfo srcAttrInfo = ctClass.getClassFile().getAttribute(
                AnnotationsAttribute.visibleTag);
            if (!(srcAttrInfo instanceof AnnotationsAttribute)) {
                // 忽略
                return emptyBytes;
            }

            // 注入新旧注解
            AnnotationsAttribute annoAttr = (AnnotationsAttribute) srcAttrInfo;
            Annotation oldAnno = annoAttr.getAnnotation(srcClassName);
            if (oldAnno == null) {
                // 忽略
                return emptyBytes;
            }

            Annotation newAnno = new Annotation(dstClassName, constpool);
            Set<String> memberNames = oldAnno.getMemberNames();
            if (memberNames != null) {
                for (String s : memberNames) {
                    newAnno.addMemberValue(s, oldAnno.getMemberValue(s));
                }
            }
            annoAttr.removeAnnotation(srcClassName);
            annoAttr.addAnnotation(newAnno);
            return ctClass.toBytecode();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
