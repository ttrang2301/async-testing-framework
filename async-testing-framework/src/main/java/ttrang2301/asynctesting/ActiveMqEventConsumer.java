/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import ttrang2301.asynctesting.expectations.Expectation;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.testcases.Campaign;
import ttrang2301.asynctesting.testcases.TestcaseResult;

@Slf4j
public class ActiveMqEventConsumer implements Runnable {

    private String connectionUrl;
    private String topicName;

    private Campaign campaign;
    private String testcaseId;
    private Class<?> eventClass;
    private Expectation expectation;

    private TestcaseResultRepository testcaseResultRepository;

    protected ActiveMqEventConsumer(String connectionUrl, String topicName, Campaign campaign,
                                    String testcaseId, Class<?> eventClass,
                                    Expectation expectation, TestcaseResultRepository testcaseResultRepository) {
        this.connectionUrl = connectionUrl;
        this.topicName = topicName;
        this.campaign = campaign;
        this.testcaseId = testcaseId;
        this.eventClass = eventClass;
        this.expectation = expectation;
        // TODO Dependency Injection
        this.testcaseResultRepository = testcaseResultRepository;
    }

    @Override
    public void run() {
        MessageConsumer consumer;
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUrl);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            consumer = session.createConsumer(destination);
        } catch (Exception e) {
            log.error("Executing testcase {}/{} failed", this.campaign.getId(), this.testcaseId, e);
            return;
        }
        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                Object event = textMessage.getBody(eventClass);
                try {
                    Object testingObject = initializeTestingObject(this.campaign, this.testcaseId,
                            this.expectation.getMethod().getDeclaringClass());
                    assertExpectationOfTestcase(testingObject, this.expectation.getMethod(), event);
                } catch (Exception e) {
                    // TODO
                }
                this.testcaseResultRepository.updateExpectationStatus(
                        this.campaign.getId(), this.testcaseId, this.expectation.getKey(),
                        ttrang2301.asynctesting.persistence.TestcaseResult.Expectation.Status.SUCCESSFUL);
            }
        }
    }

    private Object initializeTestingObject(Campaign campaign, String testcaseId, Class<?> testingClass) {
        Object testingObject;
        try {
            testingObject = testingClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Exception occurs constructing instance of " + testingClass, e);
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            this.testcaseResultRepository.updateStatus(
                    campaign.getId(),
                    testcaseId,
                    TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.FAILED));
            throw new RuntimeException(
                    "Cannot construct instance of " + testingClass
                            + " because there is no public no-argument constructor",
                    e);
        }
        return testingObject;
    }

    private void assertExpectationOfTestcase(Object testingObject, Method expectationMethod, Object event) {
        try {
            expectationMethod.invoke(testingObject, event);
        } catch (InvocationTargetException e) {
            // For any exception occurring during asserting, the expectation is considered not-met.
            // TODO Treat the assertion fail exception different with runtime exception
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            throw new RuntimeException("Cannot invoke precondition "
                    + expectationMethod.getDeclaringClass().getName() + "#"
                    + expectationMethod.getName()
                    + "(" + Arrays.stream(expectationMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")",
                    e);
        }
    }
}
