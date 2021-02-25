package com.s6.plugin.feign.adapter.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ReplacementLibraryTest {
    @Test
    public void testEquals() {
        {
            ReplacementLibrary lib1 = new ReplacementLibrary();
            lib1.setGroupId("");
            lib1.setArtifactId("");
            lib1.setClasses(new String[] { "" });
            lib1.setSkipFiles(new String[] { "a" });

            ReplacementLibrary lib2 = new ReplacementLibrary();
            lib2.setGroupId(null);
            lib2.setArtifactId("");
            lib2.setClasses(new String[] { "" });
            lib1.setSkipFiles(new String[] { "d" });

            assertNotEquals(lib1, lib2);
            assertEquals(lib1.hashCode(), lib2.hashCode());
        }

        {
            ReplacementLibrary lib1 = new ReplacementLibrary();
            lib1.setGroupId("");
            lib1.setArtifactId("");
            lib1.setClasses(new String[] { "a" });
            lib1.setSkipFiles(new String[] { "d" });

            ReplacementLibrary lib2 = new ReplacementLibrary();
            lib2.setGroupId("");
            lib2.setArtifactId("");
            lib2.setClasses(new String[] { "b" });
            lib1.setSkipFiles(new String[] { "c" });

            assertEquals(lib1, lib2);
            assertEquals(lib1.hashCode(), lib2.hashCode());
        }

        {
            ReplacementLibrary lib1 = new ReplacementLibrary();
            lib1.setGroupId("a");
            lib1.setArtifactId("b");
            lib1.setClasses(new String[] { "c" });
            lib1.setSkipFiles(new String[] { "d" });

            ReplacementLibrary lib2 = new ReplacementLibrary();
            lib2.setGroupId("a");
            lib2.setArtifactId("b");
            lib2.setClasses(new String[] { "b" });
            lib1.setSkipFiles(new String[] { "c" });

            assertEquals(lib1, lib2);
            assertEquals(lib1.hashCode(), lib2.hashCode());
        }
    }
}