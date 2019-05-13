package com.epam.fashion.trends.api.serde;

import com.epam.fashion.trends.api.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class Byte2MessageDeserializer implements Deserializer<Message> {

    private ObjectMapper mapper = new ObjectMapper();

    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    public Message deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, Message.class);
        } catch (IOException e) {
            log.error("Error while message deserialization: " + e);
            throw new IllegalArgumentException("Error while message deserialization", e);
        }
    }

    public void close() {
    }
}
