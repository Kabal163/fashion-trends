package com.epam.fashion.trends.core.service;

import com.epam.fashion.trends.api.entity.Message;
import com.epam.fashion.trends.core.serde.Message2ByteSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaService {

    private static Map<Long, KafkaProducer<Long, Message>> producers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static KafkaProducer<Long, Message> getProducer() {
        KafkaProducer producer = producers.getOrDefault(Thread.currentThread().getId(), createProducer());
        producers.putIfAbsent(Thread.currentThread().getId(), producer);
        return producer;
    }

    private static KafkaProducer<Long, Message> createProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "sandbox-hdp.hortonworks.com:6667");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "client_" + Thread.currentThread().getId());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, "524288");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Message2ByteSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }
}
