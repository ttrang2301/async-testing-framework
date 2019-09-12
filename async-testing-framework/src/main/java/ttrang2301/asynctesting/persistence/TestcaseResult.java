package ttrang2301.asynctesting.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestcaseResult {

    private String campaignId;
    private String testcaseId;
    private List<Expectation> expectations;
    private Status status;

    public enum  Status {
        INITIALIZED("Initialized"), PRECONDITIONS_READY("Ready"), SUCCESSFUL("Successful"), FAILED("Failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Data
    @AllArgsConstructor
    public static final class Expectation {

        private String key;
        private Status status;

        public enum Status {
            SUCCESSFUL("Successful"), UNKNOWN("Unknown");

            private String value;

            Status(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

}
