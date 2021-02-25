package com.s6.plugin.feign.adapter.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest {
    @Test
    public void test() {
        assertTrue(CollectionUtils.isEmpty((Collection) null));
        assertTrue(CollectionUtils.isEmpty((Map) null));
        assertTrue(CollectionUtils.isEmpty(new ArrayList<>()));
        assertTrue(CollectionUtils.isEmpty(new HashMap<>()));

        List<String> list = new ArrayList<>();
        list.add("a");
        assertTrue(CollectionUtils.isNotEmpty(list));
    }
}