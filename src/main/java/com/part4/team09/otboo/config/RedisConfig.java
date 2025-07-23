package com.part4.team09.otboo.config;

import com.part4.team09.otboo.module.domain.notification.sse.RedisSseSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisSseSubscriber redisSseSubscriber;

  @Bean
  public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(
        redisSseSubscriber.messageListener(),
        new ChannelTopic("notification-channel")
    );
    return container;
  }
}
