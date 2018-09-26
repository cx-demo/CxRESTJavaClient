package com.checkmarx.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.checkmarx.api.client.model.CxSastScanExecutionPresentationDtosFinishedScanStatusDto.ValueEnum;
import com.checkmarx.api.client.api.*;

import com.google.gson.Gson;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_ACCEPTED;

public class RestClientExample {

	
	
	private final static Logger LOGGER = Logger.getLogger(RestClientExample.class.getName());
	
	static ApiClient apiClient;

	public static final String basePath = "http://localhost/cxrestapi/";
	
	public static final String OAUTH_TOKENREQUEST_URL = basePath + "auth/identity/connect/token";
	
	static final String HEADER_CXORIGIN = "CxOrigin"; // Link transaction
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
		String password = "";
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
				.addInterceptor(new okhttp3.Interceptor() {

					@Override
					public okhttp3.Response intercept(Chain chain) throws IOException {
						Request original = chain.request();

				        Request request = original.newBuilder()
				            .header(HEADER_CXORIGIN, RestClientExample.class.getName())
				            .method(original.method(), original.body())
				            .build();

				        return chain.proceed(request);
					}})
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
	public void CreateProjectScanThenDownloadReport() throws IOException, InterruptedException{
		println("*** CreateProjectThenConfigureSettings ***");
		
		
		GeneralApi generalApi = apiClient.createService(GeneralApi.class);
		SastApi sastApi = apiClient.createService(SastApi.class); 
		
		
		// 1. Create Project with default configuration
		CxProjectManagementPresentationDtosSaveProjectDto project = new CxProjectManagementPresentationDtosSaveProjectDto();
		project.setName("Test Project");
		// Team is "\CxServer"
		String team = "00000000-1111-1111-b111-989c9070eb11";
		project.setOwningTeam(UUID.fromString(team));
		project.setIsPublic(true);
		
		Response<CxSuperTypesAPIDtosLinkedResource> isProjectCreated = generalApi.projectsManagementPostByproject(project).execute();
		Assert.assertEquals(HTTP_CREATED, isProjectCreated.code());
		
		println(isProjectCreated.body().toString());
		
		// 2. Post remote source settings to GIT
		CxProjectManagementPresentationDtosGitSourceSettingsDto gitSettings = new CxProjectManagementPresentationDtosGitSourceSettingsDto();
		gitSettings.setUrl("https://2282391:5G8EUmSjfR9HZoa1UD8x@gitlab.com/cxdemosg/NodeGoat.git");
		gitSettings.setBranch("refs/heads/master");
		Response<Object> isRemoteSourceSet= generalApi.gitSourceSettingsUpdateGitSettingsByidgitSettings(isProjectCreated.body().getId(), gitSettings).execute();
		Assert.assertEquals(HTTP_NO_CONTENT, isRemoteSourceSet.code());
		
		// 3. Get Engine Configuration
		
		Response<List<CxSastScanSettingsApplicationDtosEngineConfigurationDto>> listEngineConfigs = sastApi.engineConfigurationsGet().execute();
		Assert.assertEquals(HTTP_OK, listEngineConfigs.code());
		
		for(CxSastScanSettingsApplicationDtosEngineConfigurationDto engine: listEngineConfigs.body()) {
			println(engine.toString());
		}
		
		// 4. update scan settings - preset (Optional)
		CxSastScanSettingsPresentationDtosScanSettingsRequestDto scanSettings = new CxSastScanSettingsPresentationDtosScanSettingsRequestDto();
		scanSettings.setProjectId(isProjectCreated.body().getId());
		scanSettings.setPresetId(36l); // Checkmarx Default
		scanSettings.setEngineConfigurationId(1l); // Default Configuration
		
		Response<CxSuperTypesAPIDtosLinkedResource> isUpdated = sastApi.scanSettingsPostByscanSettings(scanSettings).execute();
		Assert.assertEquals(HTTP_OK, isUpdated.code());
		
		// 5. Create Full Scans
		CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO scanRequest = 
				new CxSastScanExecutionApplicationContractsDTOsSastScanRequestWriteDTO();
		scanRequest.setProjectId(isProjectCreated.body().getId());
		scanRequest.setIsPublic(true);
		scanRequest.setForceScan(false);
		scanRequest.setIsPublic(true);
		scanRequest.setIsIncremental(false);
		scanRequest.setComment("test scan");
		Response<CxSuperTypesAPIDtosLinkedResource> createdScanResponse = sastApi.sastScansPostByscan(scanRequest).execute();
		Assert.assertEquals(HTTP_CREATED, createdScanResponse.code());
		
		long scanId = createdScanResponse.body().getId();
		println("Scanned ID: "+String.valueOf(scanId));
				
		
		// 6. Poll Scan Status
		boolean loop = true;
		do {
			
			Response<CxSastScanExecutionPresentationDtosSastScansDto> isScanned = sastApi.sastScansGetByid(scanId).execute();
			Assert.assertEquals(HTTP_OK, isScanned.code());
			
			CxSastScanExecutionPresentationDtosScanStatusDto scanStatus = isScanned.body().getStatus();
			println(scanStatus.toString());
			
			Long status = scanStatus.getId();
			if(status == 7) { // Finished
				loop = false;
				
			}else {
				Thread.sleep(15000l);
			}
		}while(loop);
		
		// 7. Register Scan Report
		
		CxReportsSastScanPresentationDtosSastReportRequestDTO reportRequest = new CxReportsSastScanPresentationDtosSastReportRequestDTO();
		reportRequest.setReportType("PDF"); // PDF
		reportRequest.setScanId(scanId);
		Response<CxReportsSastScanPresentationDtosCreateReportResponseDto> reportCreate = generalApi.reportsPostByreportRequest(reportRequest).execute();
		Assert.assertEquals(HTTP_ACCEPTED, reportCreate.code());
		
		long reportId = reportCreate.body().getReportId();
		
		// 8. Poll for report generation status
		loop = true;
		do {
			Response<CxReportsSastScanPresentationDtosCreateReportStatusDto> generateStatus = generalApi.reportsGetStatusByid(reportId).execute();
			CxReportsSastScanPresentationDtosStatusDto.ValueEnum statusEnum = generateStatus.body().getStatus().getValue();
			
			println(generateStatus.toString());
			
			if(statusEnum == CxReportsSastScanPresentationDtosStatusDto.ValueEnum.INPROCESS) {
				Thread.sleep(15000l);
			}else {
				loop = false;
			}
		}while(loop);
		
		
		// 9. Download report
		Call<ResponseBody> downloadCall = generalApi.reportsGetByid(reportId);
		
		Response<ResponseBody> downloadResp = downloadCall.execute();
		Assert.assertEquals(HTTP_OK, downloadResp.code());
		
		println("server contacted and has file");
		String fileUrl = ".\\target\\"+reportId+".pdf";
        boolean writtenToDisk = writeResponseBodyToDisk(downloadResp.body(), fileUrl);
        println("file download was a success? " + writtenToDisk);
        
        
	}
	
	private boolean writeResponseBodyToDisk(ResponseBody body, String fileUrl) {  
	    try {
	        // todo change the file location/name according to your needs
	        File futureStudioIconFile = new File(fileUrl);
	        println("file absolute path: "+ futureStudioIconFile.getAbsolutePath());
	        
	        InputStream inputStream = null;
	        OutputStream outputStream = null;

	        try {
	            byte[] fileReader = new byte[4096];

	            long fileSize = body.contentLength();
	            long fileSizeDownloaded = 0;

	            inputStream = body.byteStream();
	            outputStream = new FileOutputStream(futureStudioIconFile);

	            while (true) {
	                int read = inputStream.read(fileReader);

	                if (read == -1) {
	                    break;
	                }

	                outputStream.write(fileReader, 0, read);

	                fileSizeDownloaded += read;

	                println("file download: " + fileSizeDownloaded + " of " + fileSize);
	            }

	            outputStream.flush();

	            return true;
	        } catch (IOException e) {
	            return false;
	        } finally {
	            if (inputStream != null) {
	                inputStream.close();
	            }

	            if (outputStream != null) {
	                outputStream.close();
	            }
	        }
	    } catch (IOException e) {
	        return false;
	    }
	}
	
	@Ignore
	@Test
	public void GetTeamsThenPrint() throws IOException{
		println("*** GetTeamsThenPrint ***");
		GeneralApi generalApi = apiClient.createService(GeneralApi.class);
		
		Response<List<CxCrossCuttingAccessControlAuthorizationDTOsTeamDTO>> response = generalApi.teamsGet().execute();
		Assert.assertEquals(HTTP_OK, response.code());
		
		for(CxCrossCuttingAccessControlAuthorizationDTOsTeamDTO team: response.body()) {
			println(team.toString());
		}
	}
	
	@Ignore
	@Test
	public void ListAllPresetsThenPrint() throws IOException{
		println("*** ListAllPresetsThenPrint ***");
		
		SastApi sastApi = apiClient.createService(SastApi.class);
		
		Response<List<CxSastScanPresetsPresentationDtosSastPresetsDto>> response = sastApi.presetsGetAllPresets().execute();
		Assert.assertEquals(HTTP_OK, response.code());
		
		for(CxSastScanPresetsPresentationDtosSastPresetsDto preset: response.body()) {
			println(preset.toString());
		}
		
	}
	
	@Ignore
	@Test
	public void GetAllProjectsThenPrintAll() throws IOException {
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
