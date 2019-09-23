package ttrang2301.asynctesting.persistence;

import java.util.List;

public class TestcaseResultMongoRepository implements TestcaseResultRepository {

    @Override
    public void insertTestcaseResults(List<TestcaseResult> initialTestcaseResults) {
        // TODO insertTestcaseResults(List<TestcaseResult> initialTestcaseResults)
    }

    @Override
    public void updateStatus(String campaignId, String testcaseId, TestcaseResult.Status status) {
        // TODO updateStatus(String campaignId, String testcaseId, TestcaseResult.Status status)
    }

    @Override
    public void updateExpectationStatus(String campaignId, String testcaseId,
                                        String expectationKey, TestcaseResult.Expectation.Status status) {
        // TODO updateExpectationStatus(String campaignId, String testcaseId, String expectationKey, TestcaseResult.Expectation.Status status)
    }
}
