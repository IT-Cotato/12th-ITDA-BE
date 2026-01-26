package com.cotato.itda.global.infra.redis;

public interface RedisJsonStore {
	<T> T save(T value, long ttlMinutes);

	<T> T find
}
