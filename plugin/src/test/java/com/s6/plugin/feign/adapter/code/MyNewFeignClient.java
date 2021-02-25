package com.s6.plugin.feign.adapter.code;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "user-service", fallbackFactory = Integer.class)
public class MyNewFeignClient {
}
