/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package asynctesting.hardware.case1;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.mt.api.devicehardware.DeviceHardwareServiceApi;
import com.absolute.qa.automation.mt.api.devicehardware.DeviceHardwareServiceTestCaseCommon;
import com.absolute.qa.automation.mt.common.RestAssuredCommonProperties;
import com.absolute.qa.automation.testmanagement.TestCaseBase;
import com.absolute.qa.automation.testmanagement.TestManager;
import com.absolute.qa.automation.testmanagement.testobjects.TestPlanDataHolder;
import com.absolute.qa.automation.testmanagement.testobjects.UserAndDeviceInfo;
import org.testng.TestException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public abstract class DeviceHardwareBase extends TestCaseBase {
    protected DeviceHardwareServiceApi hdpServiceAPI;
    protected DeviceHardwareServiceTestCaseCommon deviceHardwareServiceTestCaseCommon;
    private List<File> temporaryFiles = new ArrayList();
    private static final EnumMap<UserAndDeviceInfo.OsType, String> TEMPORARY_ZIP_FILE_PATHS = new EnumMap(UserAndDeviceInfo.OsType.class);
    private static final EnumMap<UserAndDeviceInfo.OsType, File> SAMPLE_PAYLOAD_FILES = new EnumMap(UserAndDeviceInfo.OsType.class);
    protected static final String RESOURCE_FOLDER_PATH = TestManager.testManagerProperties.getProperty("resourceFolderPath");
    protected UserAndDeviceInfo sampleDevice;
    protected String cookie;
    protected static TestPlanDataHolder testPlanData;
    protected static RestAssuredCommonProperties commonProperties = RestAssuredCommonProperties.getInstance();
    private static final String TEST_PLAN_FILE_NAME = "TestPlan_DDS_HDP_MT.xml";

    @Override
    protected void prepareTestSuite() throws AutomationException {
        testPlanData = TestPlanDataHolder.getInstance();
        testPlanData.initializeTestData(new File[]{new File(testPlanFilePath)});
        commonProperties.setSpecMap(testPlanData.getEnvironment());

        SAMPLE_PAYLOAD_FILES.put(UserAndDeviceInfo.OsType.Windows, new File(RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_Windows.json"));
        TEMPORARY_ZIP_FILE_PATHS.put(UserAndDeviceInfo.OsType.Windows, RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_%s.zip");
    }

    protected File createTemporaryZipFile(String esn) {
        File result = new File(String.format(TEMPORARY_ZIP_FILE_PATHS.get(getOSType()), esn));
        temporaryFiles.add(result);
        return result;
    }

    protected void prepareTestCaseData() throws AutomationException {
        super.executeBeforeClass();
        hdpServiceAPI = DeviceHardwareServiceApi.getInstance();
        cookie = hdpServiceAPI.getDefaultUserCookie();
        sampleDevice = getSampleDeviceFromAccount(0, 0);
    }

    protected UserAndDeviceInfo getSampleDeviceFromAccount(int accountIndex, int deviceIndex) {
        List<UserAndDeviceInfo> accountDevices = deviceHardwareServiceTestCaseCommon.createListDevicesAndAccounts(testPlanData.getEnvironment().getAccountList().get(accountIndex));
        UserAndDeviceInfo sampleDevice = deviceHardwareServiceTestCaseCommon.filterDevicesByOsType(accountDevices, getOSType()).get(deviceIndex);
        sampleDevice.setGenerateToken(false);
        return sampleDevice;
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
        return SAMPLE_PAYLOAD_FILES.get(getOSType());
    }

    @Override
    protected void setTestPlanFileName() {
        this.testPlanFileName = TEST_PLAN_FILE_NAME;
    }

    protected abstract UserAndDeviceInfo.OsType getOSType();
}
