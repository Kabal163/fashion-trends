package com.epam.fashion.trends.core;

import com.epam.fashion.trends.api.entity.Message;
import com.epam.fashion.trends.core.service.KafkaService;
import com.vk.api.sdk.streaming.clients.StreamingEventHandler;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class EventHandler extends StreamingEventHandler {

    private KafkaProducer<Long, Message> producer = KafkaService.getProducer();

    @Override
    public void handle(StreamingCallbackMessage message) {
        log.info(message.toString());

        Message m = new Message();
        m.setUserId(message.getEvent().getAuthor().getId());
        m.setContent(message.getEvent().getText());
        m.setCreationTime(message.getEvent().getCreationTime());

        for (String tagKey : message.getEvent().getTags()) {
            m.addTag(Application.getTagByKey(tagKey));
        }

        producer.send(new ProducerRecord<>(Application.getTopicName(), m));
    }
}
