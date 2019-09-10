package ttrang2301.asynctesting;

import ttrang2301.asynctesting.expectations.EventConsumer;
import ttrang2301.asynctesting.persistence.EventConsumerRepository;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.testcases.TestcaseResult;

import java.util.List;
import java.util.stream.Collectors;

public class InitializeTestcaseService {

    private TestcaseResultRepository testcaseResultRepository;
    private EventConsumerRepository eventConsumerRepository;

    public InitializeTestcaseService(TestcaseResultRepository testcaseResultRepository, EventConsumerRepository eventConsumerRepository) {
        this.testcaseResultRepository = testcaseResultRepository;
        this.eventConsumerRepository = eventConsumerRepository;
    }

    public void initializeTestcaseResultDatabase(List<EventConsumer> eventConsumers,
                                                 List<TestcaseResult> initialTestcaseResults) {
        testcaseResultRepository.insertTestcaseResults(
                initialTestcaseResults.stream().map(TestcaseResult::toPersistedModel).collect(Collectors.toList()));
        eventConsumerRepository.insertEventConsumers(
                eventConsumers.stream().map(EventConsumer::toPersistedModel).collect(Collectors.toList()));
    }

}
