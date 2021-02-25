package com.s6.plugin.feign.adapter.matcher;

/**
 * 类名匹配器
 *
 * @author Sean
 */
public class ClassNameMatcher extends BaseNameMatcher {
    public ClassNameMatcher(String[] classes) {
        super(classes);
    }

    /**
     * <p>是否匹配指定的类名，路径支持"/"分隔</p>
     *
     * @param className 类全限定名
     * @return 匹配则返回true，否则返回false
     */
    @Override
    public boolean matchesName(String className) {
        return super.matchesName((className == null) ? "" : className.replace('/', '.'));
    }
}
