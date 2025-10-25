package com.homemaker.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置RedisTemplate，设置序列化方式，提高可读性和性能
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建RedisTemplate实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);
        
        // 创建Jackson2JsonRedisSerializer序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置可见性，让Jackson能够访问所有字段
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认的类型信息，这样在反序列化时能够正确识别对象类型
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 设置key的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // 设置value的序列化方式
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        // 初始化RedisTemplate
        template.afterPropertiesSet();
        
        return template;
    }
}