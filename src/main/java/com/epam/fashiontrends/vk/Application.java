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
import org.apache.commons.cli.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.epam.fashiontrends.vk.AppOptions.*;

@Slf4j
public class Application {

    private static Integer appId;
    private static String clientSecret;
    private static String topicName;
    private static String rulesFile;

    private static final Options OPTS = new Options();

    public static void main(String[] args)
            throws ClientException,
            ApiException,
            ExecutionException,
            InterruptedException,
            StreamingApiException,
            StreamingClientException,
            ParseException,
            IOException {

        initOptions();
        if (!init(args))
            System.exit(0);

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        VkStreamingApiClient streamingClient = new VkStreamingApiClient(transportClient);

        ServiceClientCredentialsFlowResponse authResponse = vk.oAuth()
                .serviceClientCredentialsFlow(appId, clientSecret)
                .execute();

        ServiceActor serviceActor = new ServiceActor(appId, authResponse.getAccessToken());

        GetServerUrlResponse getServerUrlResponse = vk.streaming().getServerUrl(serviceActor).execute();
        StreamingActor streamingActor = new StreamingActor(getServerUrlResponse.getEndpoint(), getServerUrlResponse.getKey());

        addRules(streamingClient, streamingActor);

        KafkaProducer<Long, StreamingCallbackMessage> producer = KafkaService.getProducer();
        streamingClient.stream().get(streamingActor, new StreamingEventHandler() {
            @Override
            public void handle(StreamingCallbackMessage message) {
                log.info(message.toString());
                producer.send(new ProducerRecord<>(topicName, message));
            }
        }).execute();
    }


    private static boolean init(String[] args) throws ParseException {
        CommandLine cliParser = new GnuParser().parse(OPTS, args);

        if (args.length == 0) {
            throw new IllegalArgumentException("No args specified for client to initialize");
        }

        if (cliParser.hasOption("help")) {
            printUsage();
            return false;
        }

        rulesFile = cliParser.getOptionValue(RULES_FILE.getName());
        appId = Integer.parseInt(cliParser.getOptionValue(APP_ID.getName()));
        clientSecret = cliParser.getOptionValue(CLIENT_SECRET.getName());
        topicName = cliParser.getOptionValue(TOPIC.getName(), "default");

        if (rulesFile == null) throw new NullPointerException("File path cannot be null");
        if (appId == null) throw new NullPointerException("Application ID cannot be null");
        if (clientSecret == null) throw new NullPointerException("Client secret cannot be null");
        if (topicName == null) throw new NullPointerException("Topic name cannot be null");

        return true;
    }


    private static void initOptions() {
        OPTS.addOption(RULES_FILE.getName(), true, "File path with rules for searching");
        OPTS.addOption(APP_ID.getName(), true, "VK Application ID");
        OPTS.addOption(TOPIC.getName(), true, "Kafka topic name");
        OPTS.addOption(CLIENT_SECRET.getName(), true, "VK Client Secret");
        OPTS.addOption(HELP.getName(), true, "Print usage");
    }


    private static void printUsage() {
        new HelpFormatter().printHelp("Client", OPTS);
    }

    // Will replace existing rule if the new one has the same tag
    private static void addRules(VkStreamingApiClient streamingClient, StreamingActor streamingActor)
            throws StreamingClientException, IOException, StreamingApiException {

        Properties properties = new Properties();
        properties.load(
                new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(rulesFile),
                                StandardCharsets.UTF_16)));

        for (String tag : properties.stringPropertyNames()) {
            try {
                streamingClient.rules().delete(streamingActor, tag).execute();
            } catch (StreamingApiException e) {
                log.error("Cannot delete a rule. There is no such rule");
            }
            try {
                log.info(tag + "=" + properties.getProperty(tag));
                streamingClient.rules().add(streamingActor, tag, properties.getProperty(tag)).execute();
            } catch (StreamingApiException e) {
                log.error("Cannot create a rule. Such rule already exists");
            }
        }

        log.info(streamingClient.rules().get(streamingActor).execute().toString());
    }
}
