package com.s6.plugin.feign.adapter.code;

import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(value = "user-service", fallbackFactory = Integer.class)
public class MyOldFeignClient {
}
