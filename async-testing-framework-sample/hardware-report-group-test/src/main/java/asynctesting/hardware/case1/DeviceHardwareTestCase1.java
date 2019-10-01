package asynctesting.hardware.case1;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.core.utils.FileUtil;
import com.absolute.qa.automation.testmanagement.testobjects.UserAndDeviceInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

@Slf4j
@AsyncTest(name = "DeviceHardware_Report_Group_Test_1")
public class DeviceHardwareTestCase1 extends DeviceHardwareBase {

    @Precondition
    public void prepareCondition() throws AutomationException {
        log.info("DEMO_HACKATHON: Enter @Precondition prepareCondition method.");
        prepareTestCaseData();
        File temporaryZipFile = createTemporaryZipFile(sampleDevice.getEsn());
        FileUtil.zipSingleFile(getSamplePayload(),temporaryZipFile);
        hdpServiceAPI.uploadDeviceHdpData(sampleDevice,temporaryZipFile);
        deleteAllTemporaryFiles();
    }

    @Expectation(key = "verifyDeviceHardwareChangeMessage", eventName = "hw-canonical-inbound")
    public void assertDeviceHardwareMessageOut(DeviceHardwareChangeDTO event) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(event);

        log.info("DEMO_HACKATHON: Enter @Expectation assertDeviceHardwareMessageOut method. " +
                "Listening from 'hw-canonical-inbound' topic. " +
                "Value of DeviceHardwareChangeDTO argument: " +
                json);


    }

    protected UserAndDeviceInfo.OsType getOSType() {
        return UserAndDeviceInfo.OsType.Windows;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeviceHardwareChangeDTO {
        private DeviceHardwareDTO currentDeviceHardware;
        private List<DeltaChangeDTO> delta;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeviceHardwareDTO {
        private String deviceUid;
        private String accountUid;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeltaChangeDTO {
        private String key;
        private String currentValue;
        private String previousValue;
        private String dateTime;
    }
}

