package it.unipi.chessApp.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Value("${redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${redis.sentinel.nodes}")
    private String sentinelNodes;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = 
            new RedisSentinelConfiguration().master(sentinelMaster);
        
        for (String node : sentinelNodes.split(",")) {
            String[] parts = node.split(":");
            sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
