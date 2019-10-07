/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.persistence;

import java.util.List;

public interface TestcaseResultRepository {

    void insertTestcaseResults(List<TestcaseResult> initialTestcaseResults);

    void updateStatus(String campaignId, String testcaseId, TestcaseResult.Status toPersistedModel);
}
