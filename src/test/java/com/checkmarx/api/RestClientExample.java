package com.checkmarx.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.checkmarx.api.client.invoker.*;
import com.checkmarx.api.client.invoker.auth.OAuth;
import com.checkmarx.api.client.model.*;
import com.checkmarx.api.client.api.*;

import com.google.gson.Gson;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_CREATED;

public class RestClientExample {

	private final static Logger LOGGER = Logger.getLogger(RestClientExample.class.getName());
	
	static boolean isDebug = true;

	static ApiClient apiClient;

	public static final String basePath = "http://localhost/cxrestapi/";
	
	public static final String OAUTH_TOKENREQUEST_URL = basePath + "auth/identity/connect/token";
	
	static final String HEADER_AUTHORIZATION = "Authorization";
	static OAuthToken token;
	
	static {
		println("REST API Example compatible with 8.8");
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		println("*** beforeClass ***");

		// Token authentication
		String authName = "oauth2";
		String username = "administrator";
		String password = "password";
		String OAUTH_CLIENT_ID = "resource_owner_client";
		String OAUTH_CLIENT_SECRET = "014DF517-39D1-4453-B7B3-9930C563627C";
		String scope = "sast_rest_api";
		
		
		TokenRequestBuilder tokenRequestBuilder = (new OAuthClientRequest.TokenRequestBuilder(OAUTH_TOKENREQUEST_URL))
				.setClientId(OAUTH_CLIENT_ID)
	            .setClientSecret(OAUTH_CLIENT_SECRET)
	            .setUsername(username)
	            .setPassword(password)
				.setGrantType(GrantType.PASSWORD)
				.setScope(scope);
		OAuth oauthInterceptor = new OAuth(tokenRequestBuilder);
		
		
		//apiClient = new ApiClient(authName, client_id, client_secret, username, password);
		apiClient = new ApiClient();
		apiClient.addAuthorization(authName, oauthInterceptor);
		apiClient.getAdapterBuilder().baseUrl(basePath);
		
		Builder builder = apiClient.getOkBuilder()
				.addInterceptor(new okhttp3.logging.HttpLoggingInterceptor(
						new HttpLoggingInterceptor.Logger() {
							
							@Override
							public void log(String message) {
								LOGGER.info(message);
							}
						}
				).setLevel(HttpLoggingInterceptor.Level.BODY));
		apiClient.configureFromOkclient(builder.build());
		
	}
	
	@AfterClass
	public static void afterClass() {
		println("*** afterClass ***");
	}

	@Before
	public void before() {
		println("*** before ***");
	}

	@After
	public void after() {
		println("*** after ***");
	}

	@Ignore
	@Test
	public void GENERAL_ReturnAllProjectsThenPrintAll() throws IOException {
		println("*** GENERAL_ReturnAllProjectsThenPrintAll ***");
		
		GeneralApi generalApi = apiClient.createService(GeneralApi.class);
		Response<List<CxProjectManagementPresentationDtosProjectBaseDto>> response 
			= generalApi.projectsManagementGetByprojectNameteamId(null, null).execute();
		
		Assert.assertEquals(HTTP_OK, response.code());
		
		for(CxProjectManagementPresentationDtosProjectBaseDto project: response.body()) {
			println("*** Name " + project.getName() + " ***");
		}
		
	}
	
	@Ignore
	@Test
	public void SAST_PostNewScanThenDoNothing() throws IOException, InterruptedException  {
		println("*** SAST_PostNewScanThenDoNothing ***");

		SastApi sastApi = apiClient.createService(SastApi.class); 
		
		// Create Scan
		CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO scanRequest = 
				new CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO();
		scanRequest.setProjectId(1L);
		scanRequest.setIsPublic(true);
		scanRequest.setForceScan(false);
		scanRequest.setIsPublic(true);
		Response<CxSuperTypesAPIDtosLinkedResource> createdScanResponse = sastApi.sastScansPostByscan(scanRequest).execute();
		Assert.assertEquals(HTTP_CREATED, createdScanResponse.code());
		
		long scanId = createdScanResponse.body().getId();
		println("Scanned ID: "+String.valueOf(scanId));
		
		// Get Scan status
		sastApi.sastScansGetByid(scanId)
				.enqueue(new Callback<CxSastScanExecutionPresentationDtosSastScansDto>() {

					@Override
					public void onResponse(Call<CxSastScanExecutionPresentationDtosSastScansDto> call,
							Response<CxSastScanExecutionPresentationDtosSastScansDto> response) {
						// TODO Auto-generated method stub
						println("*** onResponse ***");
						Assert.assertEquals(HTTP_OK, response.code());
						
						CxSastScanExecutionPresentationDtosSastScansDto scanExecResponse = response.body();
						
						
						println("Scanned completed");
					}

					@Override
					public void onFailure(Call<CxSastScanExecutionPresentationDtosSastScansDto> call, Throwable t) {
						// TODO Auto-generated method stub
						println("*** onFailure ***");
						t.printStackTrace();
					}
					
				});
		
		
		
		println("*** End of SAST_PostNewScanThenDoNothing ***");
	}
	
	
	@Test
	public void SAST_GetAllEngineDetailsThenPrintAll() throws IOException {
		println("*** SAST_GetAllEngineDetailsThenPrintAll ***");

		SastApi sastApi = apiClient.createService(SastApi.class); 

		Response<List<CxSastEngineServersApplicationContractsDTOsEngineServerResponsDto>> response = sastApi.engineServersV1Get().execute();
		Assert.assertEquals(HTTP_OK, response.code());
		
		println("#Engine: "+response.body().size());
		for (CxSastEngineServersApplicationContractsDTOsEngineServerResponsDto engine : response.body()) {
			println("*** Engine " + engine.getId() + " ***");
			println("name: " + engine.getName());
			println("uri: " + engine.getUri());
			println("minLoc: " + engine.getMinLoc());
			println("maxLoc: " + engine.getMaxLoc());
			println("isAlive: " + engine.getLink());
			println("maxScans: " + engine.getMaxScans());
			println("status: " + engine.getStatus());
			println("cxVersion: " + engine.getCxVersion());
		}
	}

	private static final void println(String message) {
		LOGGER.info(message);
	}

}
