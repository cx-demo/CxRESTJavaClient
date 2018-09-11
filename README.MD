# Sample Java REST Client for CxSAST
* Author: Pedric Kng
* Updated: 11 Sept 2018
* Compatibility: CxSAST 8.8.0

A sample Java REST Client for CxSAST based on [Swagger](http://swagger.io) code generator

***

## Usage

1. Download the Cx Swagger JSON Descriptor using the steps below, [Swagger.json for CxSAST v8.8.0](src/main/resources/cx-swagger.json);  

  a. To retrieve the Swagger JSON Descriptor file, follow the below steps;  
  b. Navigate to your Swagger docs endpoint http://localhost/cxrestapi/help/swagger/ui/index using Chrome  
  c. Open the developer tab, navigate to the Network tab and filter by XHR tab
  d. Look for a request that ends with v1 http://localhost/cxrestapi/help/swagger/docs/v1  
  e. Copy and paste the content in the response into a new JSON file

2. Modify the Swagger.json for Java [Enum ](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html) Types, ** this is required as Swagger is unable to properly translate Enum type with additional property.

  Based on CxSAST 8.8.0, modify the property for the following Dtos;
  - Cx.Sast.ScanExecution.Presentation.Dtos.SastScansDto#finishedScanStatus
  - Cx.Sast.ScanExecution.Presentation.Dtos.SastScansDto#scanType
  - Cx.Sast.EngineServers.ApplicationContracts.DTOs.EngineServerResponsDto#status

Before Modification
``` JSON
    ...
    "Cx.Sast.EngineServers.ApplicationContracts.DTOs.EngineServerResponsDto": {
			"type": "object",
			"properties": {
				"id": {
					"format": "int64",
					"type": "integer"
				},
				"name": {
					"type": "string"
				},
				"uri": {
					"type": "string"
				},
				"minLoc": {
					"format": "int32",
					"type": "integer"
				},
				"maxLoc": {
					"format": "int32",
					"type": "integer"
				},
				"maxScans": {
					"format": "int32",
					"type": "integer"
				},
				"cxVersion": {
					"type": "string"
				},
				"status": {
					"enum": ["Offline", "Blocked", "ScanningAndBlocked", "Scanning", "Idle"],
					"type": "string"
				},
				"link": {
					"$ref": "#/definitions/Cx.SuperTypes.API.Dtos.LinkDtoBase"
				}
			}
		},
    ...
```

After Modification
``` JSON
    ...
    "Cx.Sast.EngineServers.ApplicationContracts.DTOs.EngineServerResponsDto": {
			"type": "object",
			"properties": {
				"id": {
					"format": "int64",
					"type": "integer"
				},
				"name": {
					"type": "string"
				},
				"uri": {
					"type": "string"
				},
				"minLoc": {
					"format": "int32",
					"type": "integer"
				},
				"maxLoc": {
					"format": "int32",
					"type": "integer"
				},
				"maxScans": {
					"format": "int32",
					"type": "integer"
				},
				"cxVersion": {
					"type": "string"
				},
				"status": {
					"$ref": "#/definitions/Cx.Sast.EngineServers.Presentation.Dtos.StatusDto"
				},
				"link": {
					"$ref": "#/definitions/Cx.SuperTypes.API.Dtos.LinkDtoBase"
				}
			}
		},
    "Cx.Sast.EngineServers.Presentation.Dtos.StatusDto": {
			"type": "object",
			"properties": {
				"id": {
					"format": "int64",
					"type": "integer"
				},
				"value": {
					"enum": ["Offline", "Blocked", "ScanningAndBlocked", "Scanning", "Idle"],
					"type": "string"
				}
			}
		},
    ...
```


3. Configure the `inputSpec` of the swagger-codegen-maven-plugin section to point to the modified Swagger JSON file.

```xml
<plugin>
  <groupId>io.swagger</groupId>
  <artifactId>swagger-codegen-maven-plugin</artifactId>
  <version>${swagger-codegen-maven-plugin-version}</version>
  <configuration>
    <inputSpec>cx-swagger.json</inputSpec>
    ...
    </configuration>
</plugin>
```

4. Execute `mvn clean generate-sources`

5. The generated REST API Client can be found in `target/generated-sources/main/java`

6. Sample usage of CxSAST REST API usage can be found in [RestClientExample](src/test/java/com/checkmarx/api/RestClientExample.java)


## References

Checkmarx CxSAST Swagger UI [[1]]  
Swagger Codegen Maven Plugin [[2]]  
Swagger Code Generator [[3]]  

[1]:https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814121835/Swagger+Examples+v8.8.0+and+up "Checkmarx CxSAST Swagger UI"
[2]:https://github.com/swagger-api/swagger-codegen/tree/master/modules/swagger-codegen-maven-plugin "Swagger Codegen Maven Plugin"
[3]:https://github.com/swagger-api/swagger-codegen "Swagger Code Generator"
