/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package asynctesting.hardware.case1;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.mt.api.devicehardware.DeviceHardwareServiceApi;
import com.absolute.qa.automation.testmanagement.TestCaseBase;
import com.absolute.qa.automation.testmanagement.TestManager;
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
    private List<File> temporaryFiles = new ArrayList();
    private static final EnumMap<UserAndDeviceInfo.OsType, String> TEMPORARY_ZIP_FILE_PATHS = new EnumMap(UserAndDeviceInfo.OsType.class);
    private static final EnumMap<UserAndDeviceInfo.OsType, File> SAMPLE_PAYLOAD_FILES = new EnumMap(UserAndDeviceInfo.OsType.class);
    protected static final String RESOURCE_FOLDER_PATH = TestManager.testManagerProperties.getProperty("resourceFolderPath");
    protected UserAndDeviceInfo sampleDevice;

    @Override
    protected void prepareTestSuite() {
        SAMPLE_PAYLOAD_FILES.put(UserAndDeviceInfo.OsType.Windows, new File(RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_Windows.json"));
        TEMPORARY_ZIP_FILE_PATHS.put(UserAndDeviceInfo.OsType.Windows, RESOURCE_FOLDER_PATH + "/data-files/hdp/windows/HDP_%s.zip");
    }

    protected File createTemporaryZipFile(String esn) {
        File result = new File(String.format(TEMPORARY_ZIP_FILE_PATHS.get(getOSType()), esn));
        temporaryFiles.add(result);
        return result;
    }

    protected void prepareTestCaseData() throws AutomationException {
        //super.executeBeforeClass();
        hdpServiceAPI = DeviceHardwareServiceApi.getInstance();
        sampleDevice = getSampleDeviceFromAccount();
    }

    protected UserAndDeviceInfo getSampleDeviceFromAccount() {
        sampleDevice.setAccountUid("bd3273f2-f06f-40ec-bfbf-a9aa8eca51f8");
        sampleDevice.setEsn("2C7JPS50T4AA1V010001");
        sampleDevice.setDeviceUid("a4eb327f-7a22-4214-a10b-5659dc4651b9");
        sampleDevice.setOsType(UserAndDeviceInfo.OsType.Windows);
        sampleDevice.setAgentStatus(UserAndDeviceInfo.AgentStatus.Active);
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

    protected abstract UserAndDeviceInfo.OsType getOSType();
}
