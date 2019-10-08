package ttrang2301.asynctesting;

import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.expectations.ActiveMqEventConsumer;
import ttrang2301.asynctesting.expectations.Expectation;
import ttrang2301.asynctesting.expectations.PeriodicallyExpectationScanningService;
import ttrang2301.asynctesting.persistence.TestcaseResultMongoRepository;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.preconditions.CreatePreconditionService;
import ttrang2301.asynctesting.preconditions.Precondition;
import ttrang2301.asynctesting.testcases.Campaign;
import ttrang2301.asynctesting.testcases.InitializeTestcaseService;
import ttrang2301.asynctesting.testcases.Testcase;
import ttrang2301.asynctesting.testcases.TestcaseResult;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class AsyncTestingApplication {

    private Campaign campaign;
    private Map<String, List<Precondition>> preconditionsByTestcase;
    private Map<String, List<Expectation>> expectationsByEvent;
    private List<TestcaseResult> initialTestcaseResults;

    private CreatePreconditionService createPreconditionService;
    private InitializeTestcaseService initializeTestcaseService;

    private String connectionUrl;
    private TestcaseResultRepository testcaseResultRepository;

    private AsyncTestingApplication(Class<?> mainClass,
                                    CreatePreconditionService createPreconditionService,
                                    InitializeTestcaseService initializeTestcaseService,
                                    // TODO Abstract EventConsumer + Dependency Injection
                                    String connectionUrl,
                                    TestcaseResultRepository testcaseResultRepository) {
        Set<Class<?>> testingClasses = Testcase.extractTestingClasses(mainClass);
        this.campaign = new Campaign(UUID.randomUUID().toString(),
                "Testing campaign running at " + ZonedDateTime.now(ZoneOffset.UTC));
        this.preconditionsByTestcase = Precondition.extractPreconditionsByTestcases(testingClasses);
        this.expectationsByEvent = Expectation.extractExpectationsByEvent(testingClasses);
        this.initialTestcaseResults = TestcaseResult.extractInitialTestcaseResults(this.campaign, testingClasses);

        this.createPreconditionService = createPreconditionService;
        this.initializeTestcaseService = initializeTestcaseService;

        this.connectionUrl = connectionUrl;
        this.testcaseResultRepository = testcaseResultRepository;
    }

    public static void run(Class<?> mainClass, String[] args) {
        TestcaseResultMongoRepository repository =
                new TestcaseResultMongoRepository();
        AsyncTestingApplication application = new AsyncTestingApplication(
                mainClass,
                // TODO dependency injection
                new CreatePreconditionService(repository), new InitializeTestcaseService(repository),
                "tcp://localhost:61616", repository);
        application.initializeTestcaseResultDatabase();
        application.createPreconditions();
        application.subscribeToSystemEvents();
        application.startExpectationScanningService();
    }

    private void initializeTestcaseResultDatabase() {
        initializeTestcaseService.initializeTestcaseResultDatabase(this.initialTestcaseResults);
    }

    private void createPreconditions() {
        createPreconditionService.createPreconditions(this.campaign, this.preconditionsByTestcase);
    }

    private void startExpectationScanningService() {
        PeriodicallyExpectationScanningService scanningService =
                new PeriodicallyExpectationScanningService(campaign.getId(), testcaseResultRepository);
        thread(scanningService, false);
    }

    private void subscribeToSystemEvents() {
        for (Map.Entry<String, List<Expectation>> eventConsumer : this.expectationsByEvent.entrySet()) {
            ActiveMqEventConsumer activeMqEventConsumer = new ActiveMqEventConsumer(connectionUrl,
                    eventConsumer.getKey(), this.campaign, eventConsumer.getValue(),
                    this.testcaseResultRepository);
            thread(activeMqEventConsumer, false);
        }
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

}
