/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package asynctesting.hardware.case1;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.testmanagement.TestCaseBase;
import com.absolute.qa.automation.testmanagement.TestManager;
import com.absolute.qa.automation.testmanagement.testobjects.UserAndDeviceInfo;

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
    private static final File SAMPLE_PAYLOAD_FILES = new File(RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_Windows.json");
    protected static UserAndDeviceInfo sampleDevice;

    private static final String NG_DEVICE_OS_HEADER = "X-NG-device-os";
    private static final String NG_METADATA_HEADER = "X-NG-metadata";
    private static final String HARDWARE_RAW_INVENTORY_URL = "/device/hardware/rawinventory";


    static {
        TEMPORARY_ZIP_FILE_PATHS.put(UserAndDeviceInfo.OsType.Windows, RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_%s.zip");
    }

    protected File createTemporaryZipFile(String esn) {
        File result = new File(String.format(TEMPORARY_ZIP_FILE_PATHS.get(UserAndDeviceInfo.OsType.Windows), esn));
        temporaryFiles.add(result);
        return result;
    }

    protected void deleteAllTemporaryFiles() {
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

    protected File getSamplePayload() {
        return SAMPLE_PAYLOAD_FILES;
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

        String deviceToken = "VJYH2+Rd5+RZA7HSDpMwwA==.1sFgn9QZpxODlwYQX7Vm6cdK2H2EU9rEwIu8BFfv341sGZmqzoLN4YGsTU/4/bX7B4Etyt65HugcnlUXAqHZmgSxow881cwzSmjoCWSfAbIjYXlB30dBJBVZgcUjcDA04fd1SgL7p6BTZt2+keOGGUMWbk3FGdATjt3mt12qhNulhROP0vSkltmAwOZUmc4IZpho7hhqsFnkFHnW0r2sfi3J6mefGqtYsuzd48xgBLfnTYV4j2bilQ7woVfvLg6ICazFAkMD9uFKyuEodskDSA==.OYwwf1png2RQ6+nU63nuNiwx3x8=";

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
}
