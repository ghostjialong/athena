package com.athena.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangjialong on 11/30/17.
 * 实现类型安全的异构容器
 */
public class HashMapWithMultiType {

    private final Map<Key<?> , Object> container = new HashMap();

    public <T> void put(String key, T value) {
        Class<?> type = value.getClass();
        Key<T> keyWithType = new Key(key, type);
        container.put(keyWithType, value);
    }

    public <T> T get(String key, Class<T> type) {
        Key<T> keyWithType = new Key(key, type);
        return  type.cast(container.get(keyWithType));
    }

    public class Key<T> {
        protected String identifier;
        Class<T> type;

        public Key(String identifier, Class<T> type) {
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            return identifier.equals(this.getClass().cast(obj).identifier);
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }
    }
}
