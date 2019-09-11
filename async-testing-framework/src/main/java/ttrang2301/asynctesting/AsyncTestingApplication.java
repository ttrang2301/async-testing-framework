package ttrang2301.asynctesting;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import ttrang2301.asynctesting.expectations.EventConsumer;
import ttrang2301.asynctesting.expectations.Expectation;
import ttrang2301.asynctesting.preconditions.*;
import ttrang2301.asynctesting.testcases.Campaign;
import ttrang2301.asynctesting.testcases.Testcase;
import ttrang2301.asynctesting.testcases.TestcaseResult;

import javax.jms.*;

@Slf4j
public class AsyncTestingApplication {

    private Campaign campaign;
    private Set<String> eventNames;
    private Map<String, List<Precondition>> preconditionsByTestcase;
    private List<EventConsumer> eventConsumers;
    private List<TestcaseResult> initialTestcaseResults;

    private CreatePreconditionService createPreconditionService;
    private InitializeTestcaseService initializeTestcaseService;

    private AsyncTestingApplication(Class<?> mainClass,
                                    CreatePreconditionService createPreconditionService,
                                    InitializeTestcaseService initializeTestcaseService) {
        Set<Class<?>> testingClasses = Testcase.extractTestingClasses(mainClass);
        this.campaign = new Campaign(UUID.randomUUID().toString(),
                "Testing campaign running at " + ZonedDateTime.now(ZoneOffset.UTC));
        this.preconditionsByTestcase = Precondition.extractPreconditionsByTestcases(testingClasses);
        this.eventConsumers = EventConsumer.extractExpectations(testingClasses);
        this.eventNames = EventConsumer.extractNamesOfObservedEvents(eventConsumers);
        this.initialTestcaseResults = TestcaseResult.extractInitialTestcaseResults(this.campaign, testingClasses);

        this.createPreconditionService = createPreconditionService;
        this.initializeTestcaseService = initializeTestcaseService;
    }

    public static void run(Class<?> mainClass, String[] args) {
        AsyncTestingApplication application = new AsyncTestingApplication(
                mainClass,
                // TODO dependency injection
                null, null);
        application.initializeTestcaseResultDatabase();
        application.createPreconditions();
        application.waitForExpectations();
    }

    private void initializeTestcaseResultDatabase() {
        initializeTestcaseService.initializeTestcaseResultDatabase(this.eventConsumers, this.initialTestcaseResults);
    }

    private void createPreconditions() {
        createPreconditionService.createPreconditions(this.campaign, this.preconditionsByTestcase);
    }

    private void waitForExpectations() {
        // TODO
        for (String eventName : this.eventNames) {
            thread(new ActiveMqEventConsumer(connectionUrl, eventName), false);
        }
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

}
