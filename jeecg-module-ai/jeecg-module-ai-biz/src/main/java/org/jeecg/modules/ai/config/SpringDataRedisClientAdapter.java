package org.jeecg.modules.ai.config;

import io.agentscope.extensions.redis.state.RedisClientAdapter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

/**
 * 基于 Jeecg / Spring Data Redis 的 AgentScope Redis 适配器。
 * 复用框架 {@link StringRedisTemplate}，不再单独创建 Jedis 连接池。
 */
public class SpringDataRedisClientAdapter implements RedisClientAdapter {

    private final StringRedisTemplate redisTemplate;

    public SpringDataRedisClientAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void rightPushList(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public List<String> rangeList(String key, long start, long end) {
        List<String> list = redisTemplate.opsForList().range(key, start, end);
        return list == null ? List.of() : list;
    }

    @Override
    public long getListLength(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public void deleteKeys(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        redisTemplate.delete(List.of(keys));
    }

    @Override
    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Set<String> getSetMembers(String key) {
        Set<String> members = redisTemplate.opsForSet().members(key);
        return members == null ? Set.of() : members;
    }

    @Override
    public long getSetSize(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public boolean keyExists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Set<String> findKeysByPattern(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys.isEmpty() ? Collections.emptySet() : keys;
    }

    @Override
    public void close() {
        // Spring 管理连接生命周期，此处不关闭共享 RedisTemplate
    }
}
