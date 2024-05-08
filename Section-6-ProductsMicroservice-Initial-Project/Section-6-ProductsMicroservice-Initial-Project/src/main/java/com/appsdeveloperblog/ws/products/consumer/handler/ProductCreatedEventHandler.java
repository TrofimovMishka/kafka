package com.appsdeveloperblog.ws.products.consumer.handler;

import com.appsdeveloperblog.ws.products.producer.KafkaProducerConfig;
import com.appsdeveloperblog.ws.products.producer.service.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = KafkaProducerConfig.EVENTS_TOPIC)
@Slf4j
public class ProductCreatedEventHandler {

    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        log.info("Handling received event {}", productCreatedEvent);
    }



}
