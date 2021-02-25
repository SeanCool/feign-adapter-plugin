package com.s6.plugin.feign.adapter.handler;

import org.junit.Test;

import static org.junit.Assert.*;

public class FeignClientHandlerTest {
    @Test
    public void testConstructor() {
        {
            FeignClientHandler handler = new FeignClientHandler("1");
            assertEquals(handler.getDstVersion(), FeignClientHandler.V1);
            assertEquals(handler.getSrcClassName(),
                org.springframework.cloud.openfeign.FeignClient.class.getCanonicalName());
            assertEquals(handler.getDstClassName(),
                org.springframework.cloud.netflix.feign.FeignClient.class.getCanonicalName());
        }
        {
            FeignClientHandler handler = new FeignClientHandler("2");
            assertEquals(handler.getDstVersion(), FeignClientHandler.V2);
            assertEquals(handler.getSrcClassName(),
                org.springframework.cloud.netflix.feign.FeignClient.class.getCanonicalName());
            assertEquals(handler.getDstClassName(),
                org.springframework.cloud.openfeign.FeignClient.class.getCanonicalName());
        }
        {
            try {
                FeignClientHandler handler = new FeignClientHandler("3");
                fail();
            } catch (IllegalArgumentException e) {
                // succ
            }
        }
    }

}