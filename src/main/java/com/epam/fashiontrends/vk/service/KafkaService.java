package com.epam.fashiontrends.vk.service;

import com.epam.fashiontrends.vk.serde.Message2ByteSerializer;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaService {

    private static Map<Long, KafkaProducer<Long, StreamingCallbackMessage>> producers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static KafkaProducer<Long, StreamingCallbackMessage> getProducer() {
        KafkaProducer producer = producers.getOrDefault(Thread.currentThread().getId(), createProducer());
        producers.putIfAbsent(Thread.currentThread().getId(), producer);
        return producer;
    }

    private static KafkaProducer<Long, StreamingCallbackMessage> createProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "sandbox.hortonworks.com:6667");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "client_" + Thread.currentThread().getId());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "524288");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Message2ByteSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }
}
