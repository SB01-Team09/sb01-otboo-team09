package com.part4.team09.otboo.config;

import com.part4.team09.otboo.module.domain.notification.sse.RedisSseDisconnectSubscriber;
import com.part4.team09.otboo.module.domain.notification.sse.RedisSseSendSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(name = "sse.redis.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisSseSendSubscriber redisSseSubscriber;
  private final RedisSseDisconnectSubscriber redisSseDisconnectSubscriber;

  @Bean
  public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);

    container.addMessageListener(
        redisSseSubscriber.messageListener(),
        new ChannelTopic("notification-channel")
    );

    container.addMessageListener(
        redisSseDisconnectSubscriber.messageListener(),
        new ChannelTopic("disconnect-channel")
    );

    return container;
  }
}
