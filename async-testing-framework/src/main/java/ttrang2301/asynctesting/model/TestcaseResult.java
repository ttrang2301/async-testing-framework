/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestcaseResult {

    private String campaignId;
    private String testId;
    private List<CompletionPoint> completionPoints;
    private Status status;

    public static final ttrang2301.asynctesting.persistence.TestcaseResult toPersistedModel(TestcaseResult model) {
        // TODO
        return null;
    }

    public enum  Status {
        INITIALIZED("Initialized"), PRECONDITIONS_READY("Ready"), SUCCESSFUL("Successful"), FAILED("Failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public static ttrang2301.asynctesting.persistence.TestcaseResult.Status toPersistedModel(Status preconditionsReady) {
            // TODO
            return null;
        }

        public String getValue() {
            return value;
        }
    }

}
