package ttrang2301.asynctesting.persistence;

public class TestcaseResult {

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

}
