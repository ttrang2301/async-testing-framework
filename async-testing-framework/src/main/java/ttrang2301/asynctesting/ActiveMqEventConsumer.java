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
    private Class<?> eventClass;
    private List<Expectation> expectations;

    private TestcaseResultRepository testcaseResultRepository;

    protected ActiveMqEventConsumer(String connectionUrl, String topicName, Campaign campaign,
                                    Class<?> eventClass, List<Expectation> expectations,
                                    TestcaseResultRepository testcaseResultRepository) {
        this.connectionUrl = connectionUrl;
        this.topicName = topicName;
        this.campaign = campaign;
        this.eventClass = eventClass;
        this.expectations = expectations;
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
            log.error("Campaign {} encounters issue observing event {} ", this.campaign.getId(), this.eventClass.getName(), e);
            return;
        }
        while (true) {
            consumeMessage(consumer);
        }
    }

    private void consumeMessage(MessageConsumer consumer) {
        Message message = null;
        try {
            message = consumer.receive();
        } catch (JMSException e) {
            log.error("Campaign {} encounters issue observing event {} ", this.campaign.getId(), this.eventClass.getName(), e);
        }
        if (!(message instanceof TextMessage)) {
            return;
        }
        TextMessage textMessage = (TextMessage) message;
        Object event = null;
        try {
            event = textMessage.getBody(eventClass);
        } catch (JMSException e) {
            log.warn("Campaign {} encounters issue deserializing event {} ", this.campaign.getId(), this.eventClass.getName(), e);
        }
        for (Expectation expectation : this.expectations) {
            Class<?> testingClass = expectation.getMethod().getDeclaringClass();
            Object testingObject = initializeTestingObject(testingClass);
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
            this.testcaseResultRepository.updateStatus(
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
            // For any exception occurring during asserting, the expectations is considered not-met.
            // TODO Treat the assertion fail exception different with runtime exception
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            throw new RuntimeException("Cannot invoke precondition "
                    + expectation.getMethod().getDeclaringClass().getName() + "#"
                    + expectation.getMethod().getName()
                    + "(" + Arrays.stream(expectation.getMethod().getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")",
                    e);
        }
        this.testcaseResultRepository.updateExpectationStatus(
                this.campaign.getId(), expectation.getTestcase().getId(), expectation.getKey(),
                ttrang2301.asynctesting.persistence.TestcaseResult.Expectation.Status.SUCCESSFUL);
    }
}
