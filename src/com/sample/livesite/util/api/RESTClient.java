package com.sample.livesite.util.api;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Client;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

public class RESTClient {

	private static final transient Log LOGGER = LogFactory.getLog(RESTClient.class);
	
	private Client client = ClientBuilder.newClient();
	
	private String protocol = "http";  
	private String host = "localhost";  
	private String port = "1876";
		
	public RESTClient(String protocol, String host, String port) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}
	
	public String get(String path, Map<String, String> params) {
		String result = "";
		String url = this.protocol + "://" + this.host + ":" + this.port;
		
    	LOGGER.debug("[RESTClient] url : " + url);
    	LOGGER.debug("[RESTClient] path : " + path);
		WebTarget target = client.target(url).path(path); 
	    
    	for(String param : params.keySet()) {
    		LOGGER.debug("[RESTClient] param : " + param + " - " + params.get(param));
    		target = target.queryParam(param, params.get(param));
    	}		
		
		try {
		    result = target.request().get(String.class);
		    LOGGER.debug(result);
		} catch (BadRequestException e) {
			LOGGER.error("[RESTClient] response=" + e.getResponse().readEntity(String.class), e);
		    throw e;
		}
		return result;
	}
	
	public InputStream getInputStream(String path, Map<String, String> params) {
		Response response = null;
		InputStream ret = null;
		String url = this.protocol + "://" + this.host + ":" + this.port;
		
    	LOGGER.debug("[RESTClient] url : " + url);
    	LOGGER.debug("[RESTClient] path : " + path);
		WebTarget target = client.target(url).path(path); 
	    
    	for(String param : params.keySet()) {
    		LOGGER.debug("[RESTClient] param : " + param + " - " + params.get(param));
    		target = target.queryParam(param, params.get(param));
    	}		
		
		try {
			response = target.request().get();
			ret = response.readEntity(InputStream.class);
		    LOGGER.debug(response.toString());
		} catch (BadRequestException e) {
			LOGGER.error("[RESTClient] response=" + e.getResponse().readEntity(String.class), e);
		    throw e;
		}
		return ret;
	}
	
	public String post(String path, Map<String, String> params) {
		String result = "";
		String url = this.protocol + "://" + this.host + ":" + this.port;
		
    	LOGGER.debug("[RESTClient] url : " + url);
    	LOGGER.debug("[RESTClient] path : " + path);
		WebTarget target = client.target(url).path(path); 

		Form form = new Form();
		
    	for(String param : params.keySet()) {
    		LOGGER.debug("[RESTClient] param : " + param + " - " + params.get(param));
    		form.param(param, params.get(param));
    	}		
		
		Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		
	    
		try {
		    result = target.request().post(entity, String.class);
		    LOGGER.debug(result);
		} catch (BadRequestException e) {
			LOGGER.error("[RESTClient] response=" + e.getResponse().readEntity(String.class), e);
		    throw e;
		}
		return result;
	}
}
