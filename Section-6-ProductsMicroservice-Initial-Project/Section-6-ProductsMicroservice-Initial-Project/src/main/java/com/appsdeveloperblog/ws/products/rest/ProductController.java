package com.appsdeveloperblog.ws.products.rest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.ws.products.service.ProductService;

@RestController
@RequestMapping("/products") //http://localhost:<port>/products
public class ProductController {
	
	ProductService productService;
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}
	
	@PostMapping
	public ResponseEntity<Object> createProduct(@RequestBody CreateProductRestModel product) {
		
		String productId;
		try { 
			productId = productService.createProduct(product);
		} catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorMessage(new Date(), e.getMessage(),"/products"));
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(productId);
	}

}

/*
For test run /test/http/test.http
Than check:
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic product-created-events-topic --from-beginning --property "print.key=true"
Picked up _JAVA_OPTIONS: -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true
b2548ddf-87a7-464d-b07a-dc2091d0d404	{"productId":"b2548ddf-87a7-464d-b07a-dc2091d0d404","title":"New Product","price":12990,"quantity":12332}
67f3a93f-35b5-45ec-86c5-df5e5c7006d3	{"productId":"67f3a93f-35b5-45ec-86c5-df5e5c7006d3","title":"New Product 2","price":1293,"quantity":1232}
 */
