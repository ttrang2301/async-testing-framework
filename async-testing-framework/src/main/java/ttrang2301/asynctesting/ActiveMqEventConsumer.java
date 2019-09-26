/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.*;

import ttrang2301.asynctesting.expectations.Expectation;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.testcases.Campaign;
import ttrang2301.asynctesting.testcases.Testcase;
import ttrang2301.asynctesting.testcases.TestcaseResult;

@Slf4j
public class ActiveMqEventConsumer implements Runnable {

    private String connectionUrl;
    private String topicName;

    private Campaign campaign;
    private List<Expectation> expectations;

    private TestcaseResultRepository testcaseResultRepository;

    protected ActiveMqEventConsumer(String connectionUrl, String topicName,
                                    Campaign campaign,
                                    List<Expectation> expectations,
                                    TestcaseResultRepository testcaseResultRepository) {
        this.connectionUrl = connectionUrl;
        this.topicName = topicName;
        this.campaign = campaign;
        this.expectations = expectations;
        // TODO Dependency Injection
        this.testcaseResultRepository = testcaseResultRepository;
    }

    @Override
    public void run() {
        try {
            Connection connection = new ActiveMQConnectionFactory(connectionUrl).createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            Queue queueA = session.createQueue("Consumer.YS.VirtualTopic." + topicName);
            VirtualMessageListener listenerA1 = new VirtualMessageListener(campaign, expectations, testcaseResultRepository);
            MessageConsumer consumerA1 = session.createConsumer(queueA);
            consumerA1.setMessageListener(listenerA1);
        } catch (Exception e) {
            log.error("Campaign {} encounters issue observing topic {} ", this.campaign.getId(), this.topicName, e);
        }
    }

    private static final class VirtualMessageListener implements MessageListener {

        private Campaign campaign;
        private List<Expectation> expectations;

        private TestcaseResultRepository testcaseResultRepository;

        private ObjectMapper objectMapper = new ObjectMapper();

        public VirtualMessageListener(Campaign campaign, List<Expectation> expectations, TestcaseResultRepository testcaseResultRepository) {
            this.campaign = campaign;
            this.expectations = expectations;
            this.testcaseResultRepository = testcaseResultRepository;
        }

        @Override
        public void onMessage(Message message) {
            if (!(message instanceof TextMessage)) {
                return;
            }
            TextMessage textMessage = (TextMessage) message;
            for (Expectation expectation : expectations) {
                Class<?> testingClass = expectation.getMethod().getDeclaringClass();
                Object testingObject = initializeTestingObject(testingClass);
                Object event = null;
                Class<?> observedEventClass = expectation.getObservedEventClass();
                try {
                    event = objectMapper.readValue(textMessage.getText(), observedEventClass);
                } catch (Exception e) {
                    log.warn("Campaign {} encounters issue deserializing event {} ", campaign.getId(), observedEventClass.getName(), e);
                }
                assertExpectationOfTestcase(testingObject, expectation, event);
            }
        }

        private Object initializeTestingObject(Class<?> testingClass) {
            Object testingObject;
            try {
                testingObject = testingClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("Exception occurs constructing instance of " + testingClass, e);
            } catch (IllegalAccessException e) {
                // This must not happen because it should be validated when extracting metadata from source code.
                testcaseResultRepository.updateStatus(
                        campaign.getId(),
                        Testcase.extractTestcase(testingClass).getId(),
                        TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.FAILED));
                throw new RuntimeException(
                        "Cannot construct instance of " + testingClass
                                + " because there is no public no-argument constructor",
                        e);
            }
            return testingObject;
        }

        private void assertExpectationOfTestcase(Object testingObject,  Expectation expectation, Object event) {
            try {
                expectation.getMethod().invoke(testingObject, event);
            } catch (InvocationTargetException e) {
                // For any exception occurring during asserting, the completionPoints is considered not-met.
                // TODO Treat the assertion fail exception different with runtime exception
            } catch (IllegalAccessException e) {
                // This must not happen because it should be validated when extracting metadata from source code.
                throw new RuntimeException("Cannot invoke precondition "
                        + expectation.getMethod().getDeclaringClass().getName() + "#"
                        + expectation.getMethod().getName()
                        + "(" + Arrays.stream(expectation.getMethod().getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")",
                        e);
            }

            testcaseResultRepository.updateExpectationStatus(
                    this.campaign.getId(), expectation.getTestcase().getId(), expectation.getKey(),
                    ttrang2301.asynctesting.persistence.TestcaseResult.CompletionPoint.Status.SUCCESSFUL);
        }
    }
}
