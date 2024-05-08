package com.appsdeveloperblog.ws.products.consumer;

import com.appsdeveloperblog.ws.products.exception.NotRetryableException;
import com.appsdeveloperblog.ws.products.exception.RetryableException;
import com.appsdeveloperblog.ws.products.producer.KafkaProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    // Use bean configuration OR configuration from application.yml, #Bean override config from .yml

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9094,localhost:9096");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        //Cosing deserialization errors use - to avoid looping with error deserialization message:
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        config.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaProducerConfig.EVENTS_TOPIC);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.appsdeveloperblog.ws.products"); // # It determines the packages allowed for deserializing JSON messages, ensuring only objects from known source are proceed
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "product-created-events");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory,
                                                                                                 KafkaTemplate<String, Object> kafkaTemplate) {
        // Config for dead letter topic
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
                // *** wait time interval and attempts for retry handle message
                new FixedBackOff(5000, 3));

        // Handle NotRetryableException; This method allows list of exception classes
        // error handler will catch this exception and will handle to way send this message to DLT
        errorHandler.addNotRetryableExceptions(NotRetryableException.class, HttpServerErrorException.class);

        // This method allow handle RetryableException's, and handle to a way retry consume this message again. Must be defined wait time interval: GO TO ***
        errorHandler.addRetryableExceptions(RetryableException.class);

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // Config for dead letter topic
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    // Config for dead letter topic - this template will send messages to DLT with .DLT extension
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // Config for dead letter topic
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        // OR add from application.yml OR from Environment object OR Uce @ConfigurationProperties mechanism to retrieve properties
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9094,localhost:9096");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

}
