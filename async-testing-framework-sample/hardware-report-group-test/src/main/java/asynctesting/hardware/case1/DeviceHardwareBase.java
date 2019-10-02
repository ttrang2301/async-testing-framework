/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package asynctesting.hardware.case1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.core.utils.FileUtil;
import com.absolute.qa.automation.testmanagement.TestCaseBase;
import com.absolute.qa.automation.testmanagement.TestManager;
import com.absolute.qa.automation.testmanagement.testobjects.UserAndDeviceInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import org.testng.TestException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class DeviceHardwareBase extends TestCaseBase {
    private List<File> temporaryFiles = new ArrayList<>();
    private static final EnumMap<UserAndDeviceInfo.OsType, String> TEMPORARY_ZIP_FILE_PATHS = new EnumMap(UserAndDeviceInfo.OsType.class);
    protected static final String RESOURCE_FOLDER_PATH = TestManager.testManagerProperties.getProperty("resourceFolderPath");
    private final File samplePayloadFile = new File(RESOURCE_FOLDER_PATH + getPayloadPath());
    protected static UserAndDeviceInfo sampleDevice;

    private static final String NG_DEVICE_OS_HEADER = "X-NG-device-os";
    private static final String NG_METADATA_HEADER = "X-NG-metadata";
    private static final String HARDWARE_RAW_INVENTORY_URL = "/device/hardware/rawinventory";


    static {
        TEMPORARY_ZIP_FILE_PATHS.put(UserAndDeviceInfo.OsType.Windows, RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_%s.zip");
    }

    protected abstract String getPayloadPath();


    protected void uploadPayload(UserAndDeviceInfo sampleDevice) throws AutomationException {
        File temporaryZipFile = createTemporaryZipFile(sampleDevice.getEsn());
        FileUtil.zipSingleFile(getSamplePayload(),temporaryZipFile);
        uploadDeviceHdpData(sampleDevice,temporaryZipFile);
        deleteAllTemporaryFiles();
    }

    private File createTemporaryZipFile(String esn) {
        File result = new File(String.format(TEMPORARY_ZIP_FILE_PATHS.get(UserAndDeviceInfo.OsType.Windows), esn));
        temporaryFiles.add(result);
        return result;
    }

    private void deleteAllTemporaryFiles() {
        for (File temporaryFile : temporaryFiles) {
            deleteFileIfExist(temporaryFile);
        }
        temporaryFiles.clear();
    }

    private void deleteFileIfExist(File temporaryFile) {
        if (temporaryFile.exists()) {
            try {
                Files.delete(temporaryFile.toPath());
            } catch (IOException e) {
                throw new TestException(e);
            }
        }
    }

    private File getSamplePayload() {
        return samplePayloadFile;
    }

    public void uploadDeviceHdpData(final UserAndDeviceInfo deviceInfo, File file) throws AutomationException {
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put(NG_DEVICE_OS_HEADER, "windows");
        extraHeaders.put(NG_METADATA_HEADER, "{ \"scanType\" : \"full\" }");

        uploadFileToDeviceEndpoint(deviceInfo, file, HARDWARE_RAW_INVENTORY_URL, extraHeaders, HttpStatus.SC_OK);
    }

    protected void uploadFileToDeviceEndpoint(UserAndDeviceInfo deviceInfo, File file,
                                              String endpointUrl, Map<String, String> headers, int statusCode)
    {

        String deviceToken = "2TCuKUkb4LL4pONdlHejIA==.6a3rBr1dfrFvqH0yW0bniXZcPKaLfywZ0r8Swg0dWTP5yXtuFIT5hGWV5twf5D5o6cGGDuF2w4L8WU1xhgJGi6B/FEPpwWgxZ7CMz2aGWkPxuAv/5F6hYyByRmHfxwIX+DtiKafCNUay6qLcv0dE/WB1Bn6aMLUdKux5LN4QcJ0OhFMM4whgQKmxeAYxRD4eFpLdseNXBISFmGLAygTBDvf5GvntXSxBZXM0NkjcDoKl5g+4vKZCMDKhYCrD+cjHTihudtSrT00yr3PEamg5Yw==.HAGtDL6puiRzd03vDkJjTcb2wBc=";

        RequestSpecification spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("https://dv2corp3-f5.absolute.com")
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
        RequestSpecification requestSpecification = given()
                .spec(spec)
                .header("Content-Type", "application/octet-stream")
                .header("DEVICE-TOKEN", deviceToken);

        requestSpecification = addHeaders(requestSpecification, headers);

        requestSpecification.body(file)
                .when()
                .post(endpointUrl)
                .then()
                .statusCode(statusCode);
    }

    private RequestSpecification addHeaders(RequestSpecification requestSpecification, Map<String, String> headers)
    {
        if(headers != null && headers.size() > 0)
        {
            requestSpecification.headers(headers);
        }

        return requestSpecification;
    }

    protected String toPrettyJson(Object event) throws JsonProcessingException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse((new ObjectMapper()).writeValueAsString(event));
        return gson.toJson(jsonElement);
    }
}
