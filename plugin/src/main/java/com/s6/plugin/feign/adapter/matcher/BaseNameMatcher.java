package com.s6.plugin.feign.adapter.matcher;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>名称匹配器</p>
 * 支持"*"模糊匹配
 *
 * @author Sean
 */
public class BaseNameMatcher {
    protected final List<Pattern> patterns;

    public BaseNameMatcher(String[] expressions) {
        expressions = preHandleNames(expressions);
        if (ArrayUtils.isEmpty(expressions)) {
            patterns = new ArrayList<>();
            return;
        }
        Set<String> exprSet = new HashSet<>();
        List<Pattern> patternList = new ArrayList<>();
        for (String expr : expressions) {
            expr = expr.trim();
            if (StringUtils.isBlank(expr) || !exprSet.add(expr)) {
                continue;
            }
            patternList.add(Pattern.compile(expr));
        }
        patterns = patternList;
    }

    public List<String> getPattern() {
        List<String> ret = new ArrayList<>();
        for (Pattern pattern : patterns) {
            ret.add(pattern.pattern());
        }
        return ret;
    }

    protected String[] preHandleNames(String[] expressions) {
        if (ArrayUtils.isEmpty(expressions)) {
            return expressions;
        }
        for (int iCls = 0; iCls < expressions.length; iCls++) {
            String expr = expressions[iCls];
            if (StringUtils.isBlank(expr)) {
                continue;
            }
            expr = expr.trim();
            String[] sections = StringUtils.splitPreserveAllTokens(expr, '*');
            if (ArrayUtils.isEmpty(sections)) {
                expressions[iCls] = expr;
            } else {
                for (int iSec = 0; iSec < sections.length; iSec++) {
                    String sec = sections[iSec];
                    if (StringUtils.isEmpty(sec)) {
                        continue;
                    }
                    sections[iSec] = sec.replace(".", "\\.");
                }
                expressions[iCls] = StringUtils.join(sections, ".*");
            }
        }
        return expressions;
    }

    public boolean matchesName(String name) {
        if (StringUtils.isBlank(name) || patterns.isEmpty()) {
            return false;
        }
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
