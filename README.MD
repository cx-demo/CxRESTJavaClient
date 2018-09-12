# Sample Java REST Client for CxSAST
* Author: Pedric Kng
* Updated: 13 Sept 2018
* Compatibility: CxSAST 8.8.0

A sample Java REST Client for CxSAST based on [Swagger](http://swagger.io) code generator

***

## Note

[Swagger UI Doc][2] is provided as reference, please refer to relevant [Checkmarx Knowledge Center][1] article for usage flows.

## Usage

1. Download the Cx Swagger JSON Descriptor using the steps below;

  a. To retrieve the Swagger JSON Descriptor file, follow the below steps;  
  b. Navigate to your Swagger docs endpoint http://localhost/cxrestapi/help/swagger/ui/index using Chrome  
  c. Open the developer tab, navigate to the Network tab and filter by XHR tab  
  d. Look for a request that ends with v1 http://localhost/cxrestapi/help/swagger/docs/v1  
  e. Copy and paste the content in the response into a new JSON file  

2. Modify the downloaded Swagger.json; See [Example of modified Swagger.json for CxSAST v8.8.0](src/main/resources/cx-swagger.json);  

  a. modify the Java [Enum ](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html) Types for the following Dtos,
  ** this is required as Swagger is unable to properly translate Enum type with additional property.

  - Cx.Sast.ScanExecution.Presentation.Dtos.SastScansDto#finishedScanStatus
  - Cx.Sast.ScanExecution.Presentation.Dtos.SastScansDto#scanType
  - Cx.Sast.EngineServers.ApplicationContracts.DTOs.EngineServerResponsDto#status  

  E.g.,
  - Before Modification
``` JSON
"definitions": {
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
  }
```

  - After Modification
``` JSON
"definitions": {
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
  }
```

  The following models has been futher modified
  - GET /sast/presets  
  ** Missing definitions in Swagger.json
``` JSON
"paths": {
"/sast/presets": {
			"get": {
				"tags": ["SAST"],
				"summary": "Get details of all presets",
				"operationId": "Presets_GetAllPresets",
				"consumes": ["application/json;v=1.0"],
				"produces": ["application/json;v=1.0"],
				"responses": {
					"200": {
						"description": "OK",
						"schema": {
							"type": "array",
							"items": {
								"$ref": "#/definitions/Cx.Sast.ScanPresets.Presentation.Dtos.SastPresetsDto"
							}
						}
					}
				}
			}
		},
    ...
}
"definitions": {
    "Cx.Sast.ScanPresets.Presentation.Dtos.SastPresetsDto": {
			"type": "object",
			"properties": {
				"id": {
					"format": "int64",
					"type": "integer"
				},
				"name": {
					"type": "string"
				},
				"ownerName": {
					"type": "string"
				},
				"link": {
					"$ref": "#/definitions/Cx.SuperTypes.API.Dtos.LinkDtoBase"
				}
			}
		},
    ...
}
```
  - GET /sast/presets  
  &#60;TODO&#62;

  - GET /reports/sastScan/{id}/status  
  ** Missing definitions in Swagger.json
``` JSON
"paths": {
"/reports/sastScan/{id}/status": {
			"get": {
				"tags": ["GENERAL"],
				"summary": "Gets the status of a specific report in the queue",
				"operationId": "Reports_GetStatusByid",
				"consumes": ["application/json;v=1.0"],
				"produces": ["application/json;v=1.0"],
				"parameters": [{
						"name": "id",
						"in": "path",
						"description": "Unique Id of the specific report",
						"required": true,
						"type": "integer",
						"format": "int64"
					}
				],
				"responses": {
					"200": {
						"description": "Ok",
						"schema": {
							"$ref": "#/definitions/Cx.Reports.SastScan.Presentation.Dtos.CreateReportStatusDto"
						},
						"examples": {
							"link": {
								"rel": "content",
								"uri": "/reports/sastScan/1"
							},
							"contentType": "application/pdf",
							"status": {
								"id": 2,
								"value": "Created"
							}
						}
					},
					"404": {
						"description": "Not found"
					}
				}
			}
		},
    ...
}
"definitions": {
  "Cx.Reports.SastScan.Presentation.Dtos.CreateReportStatusDto": {
  			"type": "object",
  			"properties": {
  				"link": {
  					"$ref": "#/definitions/Cx.SuperTypes.API.Dtos.LinkDtoBase",
  					"readOnly": true
  				},
  				"contentType": {
  					"type": "string"
  				},
  				"status": {
  					"$ref": "#/definitions/Cx.Reports.SastScan.Presentation.Dtos.StatusDto",
  					"readOnly": true
  				}
  			}
  		},
  		"Cx.Reports.SastScan.Presentation.Dtos.StatusDto": {
  			"type": "object",
  			"properties": {
  				"id": {
  					"format": "int64",
  					"type": "integer"
  				},
  				"value": {
  					"enum": ["Deleted", "InProcess", "Created", "Failed"],
  					"type": "string"
  				}
  			}
  		},
      ...
    }
```

  - GET /reports/sastScan/{id}  
  ** Switch to file download
``` JSON
"paths": {
"/reports/sastScan/{id}": {
			"get": {
				"tags": ["GENERAL"],
				"summary": "Gets the report when ready",
				"operationId": "Reports_GetByid",
				"consumes": ["application/json;v=1.0"],
				"produces": ["application/rtf;v=1.0", "application/xml;v=1.0", "application/pdf;v=1.0", "application/csv;v=1.0"],
				"parameters": [{
						"name": "id",
						"in": "path",
						"description": "Unique Id of the specific report",
						"required": true,
						"type": "integer",
						"format": "int64"
					}
				],
				"responses": {
					"200": {
						"description": "Ok",
						"schema": {
							"type": "file"
						}
					},
					"404": {
						"description": "Not Found"
					},
					"204": {
						"description": "No Content",
						"schema": {
							"type": "object"
						}
					}
				}
			}
		},
  }
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
Checkmarx CxSAST REST API [[1]]  
Checkmarx CxSAST Swagger UI [[2]]  
Swagger Codegen Maven Plugin [[3]]  
Swagger Code Generator [[4]]  

[1]:https://checkmarx.atlassian.net/wiki/spaces/KC/pages/131039271/REST+API "Checkmarx CxSAST REST API"
[2]:https://checkmarx.atlassian.net/wiki/spaces/KC/pages/248578259/Swagger+Examples "Checkmarx CxSAST Swagger UI"
[3]:https://github.com/swagger-api/swagger-codegen/tree/master/modules/swagger-codegen-maven-plugin "Swagger Codegen Maven Plugin"
[4]:https://github.com/swagger-api/swagger-codegen "Swagger Code Generator"
