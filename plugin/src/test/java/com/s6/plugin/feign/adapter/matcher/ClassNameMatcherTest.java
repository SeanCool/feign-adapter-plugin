package com.s6.plugin.feign.adapter.matcher;

import com.s6.plugin.feign.adapter.matcher.ClassNameMatcher;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassNameMatcherTest {

    @Test
    public void testMatches() {
        ClassNameMatcher matcher = new ClassNameMatcher(
            new String[] { "a", "b.*", "c.*.TripleKill" });
        assertTrue(matcher.matchesName("a"));
        assertTrue(matcher.matchesName("b."));
        assertTrue(matcher.matchesName("b.HelloWorld"));
        assertTrue(matcher.matchesName("c.d.TripleKill"));
        assertTrue(matcher.matchesName("c/d/e/TripleKill"));

        assertFalse(matcher.matchesName(""));
        assertFalse(matcher.matchesName(" "));
        assertFalse(matcher.matchesName("a.b.HelloWorld"));
        assertFalse(matcher.matchesName("A"));
        assertFalse(matcher.matchesName("x.d.TripleKill"));
    }
}