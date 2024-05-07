package com.appsdeveloperblog.ws.products.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor // Needed for deserialization propose
@AllArgsConstructor
public class ErrorMessage {

	private Date timestamp;
	private String message;
	private String details;
}
