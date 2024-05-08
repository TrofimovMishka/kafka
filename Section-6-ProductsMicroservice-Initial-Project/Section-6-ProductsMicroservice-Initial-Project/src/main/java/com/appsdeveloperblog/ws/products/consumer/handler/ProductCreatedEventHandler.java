package com.appsdeveloperblog.ws.products.consumer.handler;

import com.appsdeveloperblog.ws.products.entity.ProcessEventEntity;
import com.appsdeveloperblog.ws.products.exception.NotRetryableException;
import com.appsdeveloperblog.ws.products.exception.RetryableException;
import com.appsdeveloperblog.ws.products.producer.KafkaProducerConfig;
import com.appsdeveloperblog.ws.products.producer.service.ProductCreatedEvent;
import com.appsdeveloperblog.ws.products.repo.ProcessEventRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(topics = KafkaProducerConfig.EVENTS_TOPIC, groupId = "product-created-events")
@Slf4j
public class ProductCreatedEventHandler {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ProcessEventRepo repo;

    @Transactional
    @KafkaHandler // messageId, messageKey read from headers
    public void handle(@Payload ProductCreatedEvent productCreatedEvent,
                       @Header("messageId") String messageId,
//                       @Header(value = "messageId", required = false) String messageId, - to make this header optional, by default those headers required
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        log.info("Handling received event {}", productCreatedEvent);

        // Check if this message already saved in DB - means occur twice:

        ProcessEventEntity alreadyProceededMessage = repo.findByMessageId(messageId);
        if (alreadyProceededMessage != null){
            log.warn("Find duplicates message id:  {}", alreadyProceededMessage.getId());
            return;
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8082", HttpMethod.GET, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Received response {}", response.getBody());
            }
            // throw RetryableException if have make sense - maybe connection issue occur
        } catch (ResourceAccessException e) {
            log.error(e.getMessage());
            throw  new RetryableException(e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new NotRetryableException(e);
        }

        try {
            // Save message ID to DB: if this method throw exception - transaction will rollback
            repo.save(ProcessEventEntity
                    .builder()
                    .messageId(messageId)
                    .productId(productCreatedEvent.getProductId())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new NotRetryableException(e);
        }
    }
}
