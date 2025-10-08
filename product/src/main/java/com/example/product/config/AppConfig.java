package com.example.product.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
  public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory cf) {
    RedisTemplate<String, Integer> tpl = new RedisTemplate<>();
    tpl.setConnectionFactory(cf);
    tpl.setKeySerializer(new StringRedisSerializer());
    tpl.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
    tpl.afterPropertiesSet();
    return tpl;
  }
}
