package com.epam.fashiontrends.vk.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class Message2ByteSerializer implements Serializer<StreamingCallbackMessage> {

    private ObjectMapper mapper = new ObjectMapper();

    public void configure(Map<String, ?> map, boolean b) {
    }

    public byte[] serialize(String s, StreamingCallbackMessage message) {
        try {
            return mapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            log.error(": " + e);
            throw new IllegalArgumentException("Error while message serialization", e);
        }
    }

    public void close() {
    }
}
