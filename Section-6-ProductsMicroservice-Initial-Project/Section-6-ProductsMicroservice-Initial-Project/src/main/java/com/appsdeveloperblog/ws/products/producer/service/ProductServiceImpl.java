package com.appsdeveloperblog.ws.products.producer.service;

import com.appsdeveloperblog.ws.products.producer.KafkaProducerConfig;
import com.appsdeveloperblog.ws.products.rest.CreateProductRestModel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductRestModel productRestModel) throws Exception {

        String productId = UUID.randomUUID().toString();

        // If Business logic required Persist Product Details into database table before publishing an Event

        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
                .productId(productId)
                .title(productRestModel.getTitle())
                .price(productRestModel.getPrice())
                .quantity(productRestModel.getQuantity())
                .build();

        LOGGER.info("Before publishing a ProductCreatedEvent");

        //Blocking request - will wait for response: // productId is a message key:
//        SendResult<String, ProductCreatedEvent> result =
//                kafkaTemplate.send(KafkaProducerConfig.EVENTS_TOPIC, productId, productCreatedEvent).get();

        // Send message ID as a header example:
        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(KafkaProducerConfig.EVENTS_TOPIC, productId, productCreatedEvent);
        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        SendResult<String, ProductCreatedEvent> result =
                kafkaTemplate.send(record).get();

//        kafkaTemplate.send(topic-name, message-key, message)
        /* Asynchronously:
                CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(KafkaConfig.EVENTS_TOPIC, productId, productCreatedEvent);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error(exception.getMessage());
            }else {
                LOGGER.info("After publishing a ProductCreatedEvent successfully");
            }
        });
         */
// That metadata from result response:
        LOGGER.info("Partition: " + result.getRecordMetadata().partition());
        LOGGER.info("Topic: " + result.getRecordMetadata().topic());
        LOGGER.info("Offset: " + result.getRecordMetadata().offset());

        LOGGER.info("***** Returning product id");

        return productId;
    }

}
