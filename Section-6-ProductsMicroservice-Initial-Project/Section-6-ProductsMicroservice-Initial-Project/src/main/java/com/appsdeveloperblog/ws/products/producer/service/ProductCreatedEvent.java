package com.appsdeveloperblog.ws.products.producer.service;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor // Needed for deserialization propose
@AllArgsConstructor
@Builder
@ToString
public class ProductCreatedEvent {
	
	private String productId;
	private String title;
	private BigDecimal price;
	private Integer quantity;
}
