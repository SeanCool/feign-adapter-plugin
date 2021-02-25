package com.s6.plugin.feign.adapter.matcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseNameMatcherTest {
    @Test
    public void testConstructor() {
        {
            BaseNameMatcher matcher = new BaseNameMatcher(null);
            assertTrue(matcher.patterns.isEmpty());
        }
        {
            BaseNameMatcher matcher = new BaseNameMatcher(new String[] { "" });
            assertTrue(matcher.patterns.isEmpty());
        }
        {
            BaseNameMatcher matcher = new BaseNameMatcher(new String[] { "", " " });
            assertTrue(matcher.patterns.isEmpty());
        }
        {
            BaseNameMatcher matcher = new BaseNameMatcher(new String[] { "a", "b.*", "a" });
            assertEquals(2, matcher.patterns.size());
            assertEquals(2, matcher.getPattern().size());
            assertEquals("a", matcher.getPattern().get(0));
            assertEquals("b\\..*", matcher.getPattern().get(1));
        }
    }

    @Test
    public void testMatches() {
        {
            BaseNameMatcher matcher = new BaseNameMatcher(null);
            assertFalse(matcher.matchesName(null));
            assertFalse(matcher.matchesName("a"));
            assertFalse(matcher.matchesName("b"));
        }

        {
            BaseNameMatcher matcher = new BaseNameMatcher(new String[] { "a", "b-*-b",
                    "c.*.e.*.TripleKill" });
            assertTrue(matcher.matchesName("a"));
            assertTrue(matcher.matchesName("b-hehehe-b"));
            assertTrue(matcher.matchesName("c.d.e.f.g.TripleKill"));
            assertFalse(matcher.matchesName("c/*/TripleKill"));
        }
    }
}