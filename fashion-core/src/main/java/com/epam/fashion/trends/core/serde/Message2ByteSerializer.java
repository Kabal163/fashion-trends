package com.epam.fashion.trends.core.serde;

import com.epam.fashion.trends.api.entity.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class Message2ByteSerializer implements Serializer<Message> {

    private ObjectMapper mapper = new ObjectMapper();

    public void configure(Map<String, ?> map, boolean b) {
    }

    public byte[] serialize(String s, Message message) {
        try {
            return mapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            log.error("Error while message serialization: " + e);
            throw new IllegalArgumentException("Error while message serialization", e);
        }
    }

    public void close() {
    }
}
