package com.s6.plugin.feign.adapter.matcher;

import com.s6.plugin.feign.adapter.matcher.FileNameMatcher;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileNameMatcherTest {
    @Test
    public void testMatches() {
        FileNameMatcher matcher = new FileNameMatcher(new String[] { "META-INF/*", "test/*",
                "src/main/java/com/hello/World.java", "resources/*/d.jpg" });
        assertTrue(matcher.matchesName("META-INF/a.txt"));
        assertTrue(matcher.matchesName("/META-INF/a.txt"));
        assertTrue(matcher.matchesName("test/b.txt"));
        assertTrue(matcher.matchesName("src\\main\\java\\com\\hello/World.java"));
        assertTrue(matcher.matchesName("resources/a/b/c/d.jpg"));

        assertFalse(matcher.matchesName(null));
        assertFalse(matcher.matchesName(""));
        assertFalse(matcher.matchesName(" "));
        assertFalse(matcher.matchesName("META-INF"));
        assertFalse(matcher.matchesName("test"));
        assertFalse(matcher.matchesName("META-IN/"));
        assertFalse(matcher.matchesName("resource/a/b/c/d.jpg"));
    }
}