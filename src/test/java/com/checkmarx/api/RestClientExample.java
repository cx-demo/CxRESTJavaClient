package com.checkmarx.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.checkmarx.api.client.invoker.*;
import com.checkmarx.api.client.model.*;
import com.checkmarx.api.client.api.*;

import com.google.gson.Gson;

import com.squareup.okhttp.*;
import com.squareup.okhttp.Interceptor.Chain;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import static java.net.HttpURLConnection.HTTP_OK;;

public class RestClientExample {

	static boolean isDebug = true;

	static ApiClient apiClient;

	public static final String basePath = "http://localhost/cxrestapi";
	public static final String oauth2LoginUrl = basePath + "/auth/identity/connect/token";
	static final String HEADER_AUTHORIZATION = "Authorization";
	
	static {
		println("REST API Example compatible with 8.8");
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		println("*** beforeClass ***");

		OkHttpClient okHttpClient = new com.squareup.okhttp.OkHttpClient();

		// Token authentication, valid for 24 hours only
		
		RequestBody formBody = new FormEncodingBuilder()
				.add("username", "administrator")
				.add("password", "Cx!123456")
				.add("grant_type", "password")
				.add("scope", "sast_rest_api")
				.add("client_id", "resource_owner_client")
				.add("client_secret", "014DF517-39D1-4453-B7B3-9930C563627C")
				.build();

		Request request = new Request.Builder()
				.url(oauth2LoginUrl)
				.post(formBody)
				.build();
		
		Response response = okHttpClient.newCall(request).execute();

		Gson gson = new Gson();
		if (response.code() == HTTP_OK) { // OK
			final AuthTokenOK token = gson.fromJson(response.body().string(), AuthTokenOK.class);
			okHttpClient.interceptors().add(new Interceptor() {

				public Response intercept(Chain chain) throws IOException {
					println("*** AuthRequestInterceptor > intercept");
										
					// Add access token
					Request.Builder requestBuilder = chain.request().newBuilder();
					requestBuilder.addHeader(HEADER_AUTHORIZATION, "Bearer "+token.access_token);
					return chain.proceed(requestBuilder.build());
				}
			});
			
			
			/* if(isDebug) {
				okHttpClient.interceptors().add(new Interceptor(){

					public Response intercept(Chain chain) throws IOException {
						println("*** AuthResponseInterceptor > intercept");
						Response originalResponse = chain.proceed(chain.request());
						println(originalResponse.body().string());
						return originalResponse;
					}
				});
			}*/
			
			
		} else { // Error
			AuthTokenFailed error = gson.fromJson(response.body().string(), AuthTokenFailed.class);
		}

		Assert.assertEquals(HTTP_OK, response.code());
		
		apiClient = new ApiClient();
		apiClient.setBasePath(basePath);
		apiClient.setDebugging(isDebug);
		apiClient.selectHeaderContentType(new String[] { "application/json;v=1.0" });
		apiClient.setHttpClient(okHttpClient);

		
		
		
	}

	class AuthTokenOK {
		private String access_token;
		private int expires_in;
		private String token_type;
	}

	class AuthTokenFailed {
		private String error;
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

	@Test
	public void GENERAL_ReturnAllProjectsThenPrintAll() throws ApiException{
		println("*** GENERAL_ReturnAllProjectsThenPrintAll ***");
		
		GeneralApi generalApi = new GeneralApi();
		generalApi.setApiClient(apiClient);

		List<CxProjectManagementPresentationDtosProjectBaseDto> response 
			= generalApi.projectsManagementGetByprojectNameteamId(null, null);
		
		for(CxProjectManagementPresentationDtosProjectBaseDto project: response) {
			println("*** Name " + project.getName() + " ***");
		}
		
	}
	
	
	
	@Test
	public void SAST_PostNewScanThenDoNothing() throws ApiException {
		println("*** SAST_PostNewScanThenDoNothing ***");

		SastApi sastApi = new SastApi(); 
		sastApi.setApiClient(apiClient);
		
		CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO scanRequest = 
				new CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO();
		
		scanRequest.setProjectId(1L);
		scanRequest.setIsPublic(true);
		scanRequest.setForceScan(false);
		scanRequest.setIsPublic(true);
		CxSuperTypesAPIDtosLinkedResource response = sastApi.sastScansPostByscan(scanRequest);
		println(String.valueOf(response.getId()));
		
	}

	@Ignore
	@Test
	public void SAST_GetAllEngineDetailsThenPrintAll() throws ApiException {
		println("*** SAST_GetAllEngineDetailsThenPrintAll ***");

		SastApi sastApi = new SastApi();
		sastApi.setApiClient(apiClient);

		List<CxSastEngineServersApplicationContractsDTOsEngineServerResponsDto> engineServers = sastApi.engineServersV1Get();
		println("#Engine: "+engineServers.size());
		for (CxSastEngineServersApplicationContractsDTOsEngineServerResponsDto engine : engineServers) {
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

	private static final void println(String line) {
		System.out.println(line);
	}

}
