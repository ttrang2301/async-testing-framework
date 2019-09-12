package ttrang2301.asynctesting;

import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.testcases.TestcaseResult;

import java.util.List;
import java.util.stream.Collectors;

public class InitializeTestcaseService {

    private TestcaseResultRepository testcaseResultRepository;

    public InitializeTestcaseService(TestcaseResultRepository testcaseResultRepository) {
        this.testcaseResultRepository = testcaseResultRepository;
    }

    public void initializeTestcaseResultDatabase(List<TestcaseResult> initialTestcaseResults) {
        testcaseResultRepository.insertTestcaseResults(
                initialTestcaseResults.stream().map(TestcaseResult::toPersistedModel).collect(Collectors.toList()));
    }

}
