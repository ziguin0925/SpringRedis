package org.example.springredis.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

@TestConfiguration
@ActiveProfiles("test")
public class EmbeddedRedis {

    // testImplementation 'com.github.codemonstur:embedded-redis:1.0.0'
    private final RedisServer redisServer;


    public EmbeddedRedis(@Value("${spring.data.redis.port}") int redisPort) throws IOException {
        this.redisServer =new RedisServer(redisPort);
    }

    @PostConstruct
    public void start() throws IOException {
        this.redisServer.start();
    }

    @PreDestroy
    public void stop() throws IOException {
        this.redisServer.stop();
    }

}
