package asynctesting.hardware.case1;

import com.absolute.qa.automation.core.exception.AutomationException;
import com.absolute.qa.automation.testmanagement.testobjects.UserAndDeviceInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;
import ttrang2301.asynctesting.exception.IgnoredEventException;

@Slf4j
@AsyncTest(name = "DeviceHardware_Report_Group_Test_217")
public class DeviceHardwareTestCase1 extends DeviceHardwareBase {

    private UserAndDeviceInfo getDevice() {
        sampleDevice = new UserAndDeviceInfo();
        sampleDevice.setAccountUid("afdf6dbb-cf70-4739-889e-1802f962cfdf");
        sampleDevice.setEsn("2DFXLJ8C0RAA00SL0041");
        sampleDevice.setDeviceUid("352a883d-b02f-4a07-92bc-5704f4547d50");
        sampleDevice.setOsType(UserAndDeviceInfo.OsType.Windows);
        sampleDevice.setAgentStatus(UserAndDeviceInfo.AgentStatus.Active);
        sampleDevice.setGenerateToken(false);
        return sampleDevice;
    }

    @Override
    protected String getPayloadPath() {
        return "/data-files/hdp/windows/HDP_Windows.json";
    }

    @Precondition
    public void prepareCondition() throws AutomationException {
        sampleDevice = getDevice();
        log.info("DEMO_HACKATHON: Enter @Precondition prepareCondition method." + "\n" +
                "Starting to send payload for deviceUid: " + sampleDevice.getDeviceUid());
        uploadPayload(sampleDevice);

    }

    @Expectation(key = "verify.DeviceHardwareChangeMessage", eventName = "hw-canonical-inbound")
    public void assertDeviceHardwareMessageOut(DeviceHardwareChangeDTO event) throws JsonProcessingException {
        if (!event.getCurrentDeviceHardware().getDeviceUid().equals(sampleDevice.getDeviceUid())) {
            throw new IgnoredEventException();
        }

        log.info("DEMO_HACKATHON: Enter @Expectation method. " + "\n" +
                "Listening from 'hw-canonical-inbound' topic. " + "\n" +
                "Value of DeviceHardwareChangeDTO argument: " + "\n" +
                toPrettyJson(event));
        // do additional assert logic here
    }

    @Expectation(key = "verify.DeviceReportChangeMessage", eventName = "dg-hwreportdatachanged-inbound")
    public void assertDeviceReportMessageOut(ReportDataChangedDTO event) throws JsonProcessingException {
        if (!event.getCurrentDeviceHardware().getDeviceUid().equals(sampleDevice.getDeviceUid())) {
            throw new IgnoredEventException();
        }
        log.info("DEMO_HACKATHON: Enter @Expectation method. " + "\n" +
                "Listening from 'dg-hwreportdatachanged-inbound' topic. " + "\n" +
                "Value of ReportDataChangedDTO argument: " + "\n" +
                toPrettyJson(event));
        // do additional assert logic here
    }


    @Expectation(key = "verify.DeviceGroupChangeMessage", eventName = "dg-groupschanged-inbound")
    public void assertDeviceGrouptMessageOut(DeviceGroupChangedDTO event) throws JsonProcessingException {
        if (!event.getDeviceId().equals(sampleDevice.getDeviceUid())) {
            throw new IgnoredEventException();
        }
        log.info("DEMO_HACKATHON: Enter @Expectation method. " + "\n" +
                "Listening from 'dg-groupschanged-inbound' topic. " + "\n" +
                "Value of DeviceGroupChangedDTO argument: " + "\n" +
                toPrettyJson(event));

        // smart group that device-name contains 'hackathon': 74c8c688-f552-4a6e-b359-27f4a112f516
        // do additional assert logic here
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class ReportDataChangedDTO {
        private String deviceId;
        private String accountId;
        private List<String> changedProperties;
        private DeviceHardwareDTO currentDeviceHardware;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeviceGroupChangedDTO {
        private String deviceId;
        private String accountId;
        private List<String> groupIds;
    }
}

