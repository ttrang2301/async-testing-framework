package ttrang2301.asynctesting.persistence;

import org.junit.Test;

public class TestcaseResultMongoRepositoryTest {

    // TODO optimize unit test
    @Test
    public void test() {
        new TestcaseResultMongoRepository().updateExpectationStatus(
                "campaign 2", "test 3", "expect3", TestcaseResult.CompletionPoint.Status.UNKNOWN);
    }

}
