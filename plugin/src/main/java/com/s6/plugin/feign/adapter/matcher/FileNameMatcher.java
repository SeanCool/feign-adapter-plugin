package com.s6.plugin.feign.adapter.matcher;

import com.s6.plugin.feign.adapter.constants.PluginConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>文件名匹配器</p>
 * 支持"/"的分隔路径匹配
 *
 * @author Sean
 */
public class FileNameMatcher extends BaseNameMatcher {
    public FileNameMatcher(String[] names) {
        super(names);
    }

    @Override
    protected String[] preHandleNames(String[] expressions) {
        for (int i = 0; i < expressions.length; i++) {
            if (StringUtils.isEmpty(expressions[i])) {
                continue;
            }
            String expr = expressions[i].replace(PluginConstants.WIN_FILE_SEPARATOR,
                PluginConstants.ZIP_FILE_SEPARATOR);
            if (!expr.startsWith(PluginConstants.ZIP_FILE_SEPARATOR_STR)) {
                expressions[i] = PluginConstants.ZIP_FILE_SEPARATOR_STR + expr;
            }
        }
        return super.preHandleNames(expressions);
    }

    @Override
    public boolean matchesName(String name) {
        if (name != null) {
            name = name.replace(PluginConstants.WIN_FILE_SEPARATOR,
                PluginConstants.ZIP_FILE_SEPARATOR);
            if (!name.startsWith(PluginConstants.ZIP_FILE_SEPARATOR_STR)) {
                name = PluginConstants.ZIP_FILE_SEPARATOR_STR + name;
            }
        }
        return super.matchesName(name);
    }
}
