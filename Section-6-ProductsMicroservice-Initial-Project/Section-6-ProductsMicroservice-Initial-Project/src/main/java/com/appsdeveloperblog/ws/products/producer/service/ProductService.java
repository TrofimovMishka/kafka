package com.appsdeveloperblog.ws.products.producer.service;

import com.appsdeveloperblog.ws.products.rest.CreateProductRestModel;

public interface ProductService {
	
	String createProduct(CreateProductRestModel productRestModel) throws Exception ;

}
