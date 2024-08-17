package com.artinus.subscription.api.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}") private String host;
    @Value("${spring.redis.port}") private String port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port));

        // config.useClusterServers()
        //         // use "rediss://" for SSL connection
        //         .addNodeAddress();

        return Redisson.create(config);
    }
}
