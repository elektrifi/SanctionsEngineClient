package com.elektrifi.sanctions.test.client;

import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.Logger;

import com.elektrifi.util.ApplicationConfig;
import com.elektrifi.util.Stopwatch;
import com.elektrifi.sanctions.beans.UserBean;
import com.elektrifi.sanctions.test.client.TestClientAllScreening.GzipDecompressingEntity;
import com.google.gson.Gson;

public class TestClientLogin {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(TestClientLogin.class);
		
	public static void main(String[] args) {

		// Read in properties (location of HMT file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();		
		String loginServiceUrl = config.getProperty("LoginServiceUrl");

		logger.info("\n--------");
		logger.info("LoginServiceUrl read in as " + loginServiceUrl);		
		
		UserBean userBean = new UserBean();		
		userBean.setUserName("demoUser");
		userBean.setPassword("lk23_yo91#");
		
		Gson gson = new Gson();
		String requestJson = gson.toJson(userBean);
		
		// Prepare to send the JSON to the RESTful service		   	
		DefaultHttpClient client = new DefaultHttpClient();

		// Add GZIP for request and response
		try {
			client.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(
					final HttpRequest request,
	        		final HttpContext context) throws HttpException, IOException {
	               	if (!request.containsHeader("Accept-Encoding")) {
	               		request.addHeader("Accept-Encoding", "gzip");
	               	}
	        	}
			});			
	            
	        client.addResponseInterceptor(new HttpResponseInterceptor() {
	        	public void process(
	    			final HttpResponse response,
	            	final HttpContext context) throws HttpException, IOException {
	            		HttpEntity entity = response.getEntity();
	            		Header ceheader = entity.getContentEncoding();
	            	if (ceheader != null) {
	            		HeaderElement[] codecs = ceheader.getElements();
	            		for (int i = 0; i < codecs.length; i++) {
	            			if (codecs[i].getName().equalsIgnoreCase("gzip")) {
	            				response.setEntity(
                                    new GzipDecompressingEntity(response.getEntity()));
	            				return;
	            			}
	            		}
	            	}
	            }
	        });	
	        	            
	        // POST	    
	        logger.debug("==============");	    
	        logger.debug("POST LOGIN");
	        logger.debug("==============");
				
	        HttpPost post = new HttpPost(loginServiceUrl);
	        StringEntity entity = null;	   
	        try {
	        	entity = new StringEntity(requestJson.toString());
	        } catch (UnsupportedEncodingException uee) {
	           logger.fatal( "Caught UnsupportedEncodingException: " );
	           uee.printStackTrace();
	        }	    
	        entity.setContentType("application/json");
	        post.setEntity(entity);	
	        HttpClientParams.setRedirecting(post.getParams(),false);
	        HttpResponse response = null; 
	        try {
	           // Start timer
	           Stopwatch stopwatch = new Stopwatch().start();
	           // Send the JSON to the RESTful login service
	           response = client.execute(post);
	           stopwatch = new Stopwatch().stop();
	           logger.info("--------------------");
	           logger.info("Elapsed time for response is " + stopwatch.getElapsedTimeMs() + " ms.");	    	
	           logger.debug("--------------------");
	           logger.info("Response Status Line: " + response.getStatusLine());
	           logger.debug("Response Content Encoding: " + response.getLastHeader("Content-Encoding"));
	           logger.debug(response.getLastHeader("Content-Length"));
	           logger.info("--------------------");
	           stopwatch.reset();
	        } catch (IOException ioe) {
	        	logger.fatal( "Caught IOException: " );
	        	ioe.printStackTrace();				
	        } 
			
	        if (entity != null) {
	           try {
	        	   String content = EntityUtils.toString(entity);
	        	   logger.debug("Content: " + content);
	        	   logger.info("Uncompressed size: "+content.length());
	           } catch (IOException ioe) {
	        	   logger.error("Caught IOException: ");
	        	   ioe.printStackTrace();
	           }
	        }
			
	        if (response.getStatusLine().getStatusCode() != 201) {
	           throw new RuntimeException("Operation failed: Response code is " +
	        		   response.getStatusLine().getStatusCode());
	        }
			
	        String location = response.getLastHeader("Location").getValue();
	        logger.info("Session object created at: " + location);        
	        logger.info("\n\n");
	    
	        client.getConnectionManager().shutdown();
    
		} finally {
			client.getConnectionManager().shutdown();
		}						
	}  
}