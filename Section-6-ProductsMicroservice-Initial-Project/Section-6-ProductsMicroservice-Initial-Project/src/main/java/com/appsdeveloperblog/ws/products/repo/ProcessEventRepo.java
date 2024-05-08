package com.appsdeveloperblog.ws.products.repo;

import com.appsdeveloperblog.ws.products.entity.ProcessEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessEventRepo extends JpaRepository<ProcessEventEntity, Long> {
    ProcessEventEntity findByMessageId(String messageId);
}
