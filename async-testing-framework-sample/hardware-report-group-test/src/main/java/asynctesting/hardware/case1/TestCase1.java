package asynctesting.hardware.case1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

@Slf4j
@AsyncTest(name = "DeviceHardwareTest1")
public class TestCase1 {

    @Precondition
    public void prepareCondition() {
        log.info("DEMO_HACKATHON: Enter @Precondition prepareCondition method.");
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

