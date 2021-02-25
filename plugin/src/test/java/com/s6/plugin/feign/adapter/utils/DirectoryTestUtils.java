package com.s6.plugin.feign.adapter.utils;

import java.io.File;

public class DirectoryTestUtils {
    private DirectoryTestUtils() {

    }

    public static File getTargetDir() {
        return new File(new File("").getAbsoluteFile(), "target");
    }

    public static File getAppDir() {
        return new File(getTargetDir(), "feign-adapter");
    }
}
