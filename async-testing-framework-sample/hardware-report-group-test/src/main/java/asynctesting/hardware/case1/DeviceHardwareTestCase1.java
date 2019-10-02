package asynctesting.hardware.case1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
import ttrang2301.asynctesting.exception.IgnoredEventException;

@Slf4j
@AsyncTest(name = "DeviceHardware_Report_Group_Test_5")
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
        log.info("DEMO_HACKATHON: Enter @Precondition prepareCondition method.");
        sampleDevice = getDevice();
        uploadPayload(sampleDevice);

    }

    @Expectation(key = "verifyDeviceHardwareChangeMessage", eventName = "hw-canonical-inbound")
    public void assertDeviceHardwareMessageOut(DeviceHardwareChangeDTO event) throws JsonProcessingException {
        if (!event.getCurrentDeviceHardware().getDeviceUid().equals(sampleDevice.getDeviceUid())) {
            throw new IgnoredEventException();
        }

        String prettyJsonString = toPrettyJson(event);
        log.info("DEMO_HACKATHON: Enter @Expectation method. " + "\n" +
                "Listening from 'hw-canonical-inbound' topic. " + "\n" +
                "Value of DeviceHardwareChangeDTO argument: " + "\n" +
                prettyJsonString);
    }

    @Expectation(key = "verifyDeviceReportChangeMessage", eventName = "dg-hwreportdatachanged-inbound")
    public void assertDeviceReportMessageOut(ReportDataChangedDTO event) throws JsonProcessingException {
        if (!event.getCurrentDeviceHardware().getDeviceUid().equals(sampleDevice.getDeviceUid())) {
            throw new IgnoredEventException();
        }
        String prettyJsonString = toPrettyJson(event);
        log.info("DEMO_HACKATHON: Enter @Expectation method. " + "\n" +
                "Listening from 'dg-hwreportdatachanged-inbound' topic. " + "\n" +
                "Value of ReportDataChangedDTO argument: " + "\n" +
                prettyJsonString);

    }

    private String toPrettyJson(Object event) throws JsonProcessingException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse((new ObjectMapper()).writeValueAsString(event));
        return gson.toJson(jsonElement);
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
}

