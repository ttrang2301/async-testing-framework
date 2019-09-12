package ttrang2301.asynctesting;

import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.expectations.Expectation;
import ttrang2301.asynctesting.preconditions.Precondition;
import ttrang2301.asynctesting.testcases.Campaign;
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
    private Map<String, List<Expectation>> expectationsByTestcase;
    private List<TestcaseResult> initialTestcaseResults;

    private CreatePreconditionService createPreconditionService;
    private InitializeTestcaseService initializeTestcaseService;

    private String connectionUrl;

    private AsyncTestingApplication(Class<?> mainClass,
                                    CreatePreconditionService createPreconditionService,
                                    InitializeTestcaseService initializeTestcaseService,
                                    // TODO Abstract EventConsumer + Dependency Injection
                                    String connectionUrl) {
        Set<Class<?>> testingClasses = Testcase.extractTestingClasses(mainClass);
        this.campaign = new Campaign(UUID.randomUUID().toString(),
                "Testing campaign running at " + ZonedDateTime.now(ZoneOffset.UTC));
        this.preconditionsByTestcase = Precondition.extractPreconditionsByTestcases(testingClasses);
        this.expectationsByTestcase = Expectation.extractExpectationsByTestcase(testingClasses);
        this.initialTestcaseResults = TestcaseResult.extractInitialTestcaseResults(this.campaign, testingClasses);

        this.createPreconditionService = createPreconditionService;
        this.initializeTestcaseService = initializeTestcaseService;

        this.connectionUrl = connectionUrl;
    }

    public static void run(Class<?> mainClass, String[] args) {
        AsyncTestingApplication application = new AsyncTestingApplication(
                mainClass,
                // TODO dependency injection
                null, null, "tcp://localhost:61616");
        application.initializeTestcaseResultDatabase();
        application.createPreconditions();
        application.waitForExpectations();
    }

    private void initializeTestcaseResultDatabase() {
        initializeTestcaseService.initializeTestcaseResultDatabase(this.initialTestcaseResults);
    }

    private void createPreconditions() {
        createPreconditionService.createPreconditions(this.campaign, this.preconditionsByTestcase);
    }

    private void waitForExpectations() {
        // TODO
        for (String eventName : this.expectationsByTestcase.keySet()) {
            thread(new ActiveMqEventConsumer(connectionUrl, eventName), false);
        }
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

}
