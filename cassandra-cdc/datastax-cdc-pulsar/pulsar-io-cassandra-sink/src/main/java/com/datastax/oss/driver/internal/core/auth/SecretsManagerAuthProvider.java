/**
 * 
 */
package com.datastax.oss.driver.internal.core.auth;

import java.security.InvalidParameterException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.datastax.dse.driver.api.core.config.DseDriverOption;
import com.datastax.dse.driver.internal.core.auth.AuthUtils;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.config.DriverOption;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rawaaksh
 *
 */
@Slf4j
public class SecretsManagerAuthProvider extends PlainTextAuthProvider{
	private final DriverExecutionProfile config;

	public SecretsManagerAuthProvider(DriverContext context) {
		super(context);
		this.config = context.getConfig().getDefaultProfile();
	}
	
	@Override
	protected Credentials getCredentials(EndPoint endPoint, String serverAuthenticator) {
		// TODO Auto-generated method stub
		 AuthUtils.validateConfigPresent(
			        config,
			        PlainTextAuthProvider.class.getName(),
			        endPoint,
			        REGION_OPTION,
			        SECRET_NAME_OPTION);

			    String authorizationId = config.getString(DseDriverOption.AUTH_PROVIDER_AUTHORIZATION_ID, "");
			    assert authorizationId != null; // per the default above
			    String secretName=config.getString(SECRET_NAME_OPTION);
			    String region=config.getString(REGION_OPTION);
			    JsonNode node=getSecret(secretName,region );
			    log.info("Got credentials from secrets manager successfully, region:{},secretName:{}",region,secretName);
			    return new Credentials(
			        node.get("ServiceUserName").asText().toCharArray(),
			        node.get("ServicePassword").asText().toCharArray(),
			        authorizationId.toCharArray());
	}
	
	 private final static DriverOption REGION_OPTION = new DriverOption() {
         public String getPath() {
             return "advanced.auth-provider.aws-region";
         }
     };

     private final static DriverOption SECRET_NAME_OPTION = new DriverOption() {
         public String getPath() {
             return "advanced.auth-provider.aws-secret";
         }
     };
     
     public final static DriverOption SECRET_MANAGER_ID_OPTION = new DriverOption() {
         public String getPath() {
             return "basic.aws-secret-manager.id";
         }
     };
     public final static DriverOption SECRET_MANAGER_REGION_OPTION = new DriverOption() {
         public String getPath() {
             return "basic.aws-secret-manager.region";
         }
     };

	// Use this code snippet in your app.
	// If you need more information about configurations or implementing the sample code, visit the AWS docs:
	// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-samples.html#prerequisites

	public static JsonNode getSecret( String secretName,String region) {
		log.info("secretName:{},region:{}",secretName,region);
		ObjectMapper objectMapper=new ObjectMapper();

	    // Create a Secrets Manager client
	    AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
	    								.withCredentials(InstanceProfileCredentialsProvider.getInstance())
	                                    .withRegion(region)
	                                    .build();
	    
	    // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
	    // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
	    // We rethrow the exception by default.
	    
	    String secret, decodedBinarySecret;
	    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
	                    .withSecretId(secretName);
	    GetSecretValueResult getSecretValueResult = null;
	    JsonNode secretNode=null;
	    try {
	        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
	        
		    // Decrypts secret using the associated KMS key.
		    // Depending on whether the secret is a string or binary, one of these fields will be populated.
		    if (getSecretValueResult.getSecretString() != null) {
		        secret = getSecretValueResult.getSecretString();
		        secretNode=objectMapper.readTree(secret);
		        
		    }
		    else {
		        decodedBinarySecret = new String(java.util.Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
		        secretNode=objectMapper.readTree(decodedBinarySecret);
		    }
	    } catch (DecryptionFailureException e) {
	        // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InternalServiceErrorException e) {
	        // An error occurred on the server side.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidParameterException e) {
	        // You provided an invalid value for a parameter.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidRequestException e) {
	        // You provided a parameter value that is not valid for the current state of the resource.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (ResourceNotFoundException e) {
	        // We can't find the resource that you asked for.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	    

	    return secretNode;
	} 

}
