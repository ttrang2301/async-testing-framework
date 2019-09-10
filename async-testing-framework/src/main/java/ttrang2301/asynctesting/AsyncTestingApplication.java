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

    private static final class ActiveMqEventConsumer implements Runnable {

        private String connectionUrl;
        private String topicName;

        private ActiveMqEventConsumer(String connectionUrl, String topicName) {
            this.connectionUrl = connectionUrl;
            this.topicName = topicName;
        }

        @Override
        public void run() {
            // Getting JMS connection from the server
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUrl);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Creating session for seding messages
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // Getting the queue 'JCG_QUEUE'
            Destination destination = session.createTopic(topicName);

            // MessageConsumer is used for receiving (consuming) messages
            MessageConsumer consumer = session.createConsumer(destination);

            // Here we receive the message.
            while (true) {
                // TODO
                Message message = consumer.receive();

                // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
                // so we must cast to it to get access to its .getText() method.
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println("Received message '" + textMessage.getText() + "'");
                }
            }

            connection.close();
        }
    }


}
