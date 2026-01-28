package it.unipi.chessApp.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Value("${redis.master.host}")
    private String masterHost;

    @Value("${redis.master.port}")
    private int masterPort;

    @Value("${redis.replica1.host}")
    private String replica1Host;

    @Value("${redis.replica1.port}")
    private int replica1Port;

    @Value("${redis.replica2.host}")
    private String replica2Host;

    @Value("${redis.replica2.port}")
    private int replica2Port;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStaticMasterReplicaConfiguration config = 
            new RedisStaticMasterReplicaConfiguration(masterHost, masterPort);
        config.addNode(replica1Host, replica1Port);
        config.addNode(replica2Host, replica2Port);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
