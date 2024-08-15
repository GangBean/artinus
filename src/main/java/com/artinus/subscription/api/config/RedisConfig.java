package com.artinus.subscription.api.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}") private String host;
    @Value("${spring.redis.port}") private String port;

    @Bean
    @Profile("prod")
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port));

        // config.useClusterServers()
        //         // use "rediss://" for SSL connection
        //         .addNodeAddress();

        return Redisson.create(config);
    }
}
