package com.athena.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by wangjialong on 11/30/17.
 */
public class TestHashMapWithMultiType {

    private HashMapWithMultiType context;

    @Before
    public void setUp() {
        context = new HashMapWithMultiType();
    }

    @Test
    public void testKey() {
        HashMapWithMultiType.Key keyWithType = context.new Key("test", String.class);
        HashMapWithMultiType.Key keyWithType2 = context.new Key("test", String.class);
        Object test1 = keyWithType;
        Object test2 = keyWithType2;
        assertTrue(keyWithType.equals(keyWithType2));
        assertEquals(keyWithType, keyWithType2);
    }

    @Test
    public void testPutData() {
        context.put("my_test", 1);
        context.put("beauty", "hahahahah");
        String data =   context.get("beauty", String.class);
        Integer num =  context.get("my_test", Integer.class);
        assertEquals(Integer.valueOf(1), num);
        assertEquals("hahahahah", data);
    }

}
