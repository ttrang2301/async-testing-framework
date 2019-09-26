/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.testcases;

import lombok.AllArgsConstructor;
import lombok.Data;
import ttrang2301.asynctesting.persistence.TestcaseResult;

@Data
@AllArgsConstructor
public class CompletionPoint {

    private String name;
    private boolean result;

    public static final TestcaseResult.CompletionPoint toPersistedModel(CompletionPoint completionPoint) {
        return new TestcaseResult.CompletionPoint(
                completionPoint.getName(),
                completionPoint.isResult()
                        ? TestcaseResult.CompletionPoint.Status.SUCCESSFUL
                        : TestcaseResult.CompletionPoint.Status.UNKNOWN
        );
    }

}
