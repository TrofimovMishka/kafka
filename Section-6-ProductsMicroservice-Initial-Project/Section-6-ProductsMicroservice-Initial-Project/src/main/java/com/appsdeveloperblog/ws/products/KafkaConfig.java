package com.appsdeveloperblog.ws.products;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String EVENTS_TOPIC = "product-created-events-topic";

    //When spring run - the new topic will be created on servers from .yml
    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name(EVENTS_TOPIC)
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas","2"))
                .build();
    }
}

/*
in terminal check: ./bin/kafka-topics.sh --describe --bootstrap-server localhost:9092 --topic product-created-events-topic
you must see this topic description
 */