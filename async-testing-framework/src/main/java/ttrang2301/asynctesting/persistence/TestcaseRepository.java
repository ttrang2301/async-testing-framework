/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.persistence;

import java.util.List;

import ttrang2301.asynctesting.model.CompletionPoint;

public interface TestcaseRepository {

    void insertTestcase(String campaignId, String testcaseId,
                        List<CompletionPoint> completionPoints, Testcase.Status status);

}
