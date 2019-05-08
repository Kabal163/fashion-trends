package com.epam.fashiontrends.vk;

import com.epam.fashiontrends.vk.service.KafkaService;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.ServiceClientCredentialsFlowResponse;
import com.vk.api.sdk.objects.streaming.responses.GetServerUrlResponse;
import com.vk.api.sdk.streaming.clients.StreamingEventHandler;
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient;
import com.vk.api.sdk.streaming.clients.actors.StreamingActor;
import com.vk.api.sdk.streaming.exceptions.StreamingApiException;
import com.vk.api.sdk.streaming.exceptions.StreamingClientException;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.ExecutionException;

@Slf4j
public class Application {

    private static final Integer APP_ID = 6974634;
    private static final String CLIENT_SECRET = "A5elsaLFqQ8R8V39LyG6";
    private static final String TOPIC_NAME = "default";

    private static final String[] values = {
            "детское платье -скидка -продам -недорого -закупка",
            "летнее платье -скидка -продам -недорого -закупка",
            "куплю платье -скидка -продам -недорого -закупка",
            "хочу летнее платье -скидка -продам -недорого -закупка",
            "хочу обновить гардероб -скидка -продам -недорого -закупка",
            "хочу юбку -скидка -продам -недорого -закупка",
            "хочу блузку -скидка -продам -недорого -закупка",
            "люблю платья -скидка -продам -недорого -закупка",
            "платье мечта -скидка -продам -недорого -закупка",
            "лето платье -скидка -продам -недорого -закупка",
            "платье ребенку -скидка -продам -недорого -закупка",
            "юбку ребенку -скидка -продам -недорого -закупка",
            "блузку ребенку -скидка -продам -недорого -закупка"};

    public static void main(String[] args)
            throws ClientException,
            ApiException,
            ExecutionException,
            InterruptedException, StreamingApiException, StreamingClientException {

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        VkStreamingApiClient streamingClient = new VkStreamingApiClient(transportClient);

        ServiceClientCredentialsFlowResponse authResponse = vk.oAuth()
                .serviceClientCredentialsFlow(APP_ID, CLIENT_SECRET)
                .execute();

        ServiceActor serviceActor = new ServiceActor(APP_ID, authResponse.getAccessToken());

        GetServerUrlResponse getServerUrlResponse = vk.streaming().getServerUrl(serviceActor).execute();
        StreamingActor streamingActor = new StreamingActor(getServerUrlResponse.getEndpoint(), getServerUrlResponse.getKey());

        recreateRules(streamingClient, streamingActor);

        KafkaProducer<Long, StreamingCallbackMessage> producer = KafkaService.getProducer();
        streamingClient.stream().get(streamingActor, new StreamingEventHandler() {
            @Override
            public void handle(StreamingCallbackMessage message) {
                log.info(message.toString());
                producer.send(new ProducerRecord<>(TOPIC_NAME, message));
            }
        }).execute();
    }

    private static void recreateRules(VkStreamingApiClient streamingClient, StreamingActor streamingActor) throws StreamingClientException, StreamingApiException {
        for (int i = 0; i < values.length; i++) {
            try {
                streamingClient.rules().delete(streamingActor, String.valueOf(i)).execute();
            } catch (StreamingApiException e) {
                continue;
            }
        }

        for (int i = 0; i < values.length; i++) {
            streamingClient.rules().add(streamingActor, String.valueOf(i), values[i]).execute();
        }
        log.info(streamingClient.rules().get(streamingActor).execute().toString());
    }
}
