/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting;

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

public abstract class ActiveMqEventConsumer implements Runnable {

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
        // Getting JMS connection from the server
//        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUrl);
//        Connection connection = connectionFactory.createConnection();
//        connection.start();
//
//        // Creating session for seding messages
//        Session session = connection.createSession(false,
//                Session.AUTO_ACKNOWLEDGE);
//
//        // Getting the queue 'JCG_QUEUE'
//        Destination destination = session.createTopic(topicName);
//
//        // MessageConsumer is used for receiving (consuming) messages
//        MessageConsumer consumer = session.createConsumer(destination);
//
//        // Here we receive the message.
//        while (true) {
//            // TODO
//            Message message = consumer.receive();
//
//            // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
//            // so we must cast to it to get access to its .getText() method.
//            if (message instanceof TextMessage) {
//                TextMessage textMessage = (TextMessage) message;
//                Object event = textMessage.getBody(eventClass);
//                try {
//                    Object testingObject = initializeTestingObject(this.campaign, this.testcaseId,
//                            this.expectation.getMethod().getDeclaringClass());
//                    assertExpectationOfTestcase(testingObject, this.expectation.getMethod());
//                } catch (Exception e) {
//                    // TODO
//                }
//            }
//        }
//
//        connection.close();
    }

    private Object initializeTestingObject(Campaign campaign, String testcaseId, Class<?> testingClass) {
        Object testingObject;
        try {
            testingObject = testingClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Exception occurs constructing instance of " + testingClass, e);
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            testcaseResultRepository.updateStatus(
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

    private void assertExpectationOfTestcase(Object testingObject, Method expectationMethod) {
        try {
            expectationMethod.invoke(testingObject);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Exception occurs invoking precondition "
                    + expectationMethod.getDeclaringClass().getName() + "#"
                    + expectationMethod.getName()
                    + "(" + Arrays.stream(expectationMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")" ,
                    e);
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            throw new RuntimeException("Cannot invoke precondition "
                    + expectationMethod.getDeclaringClass().getName() + "#"
                    + expectationMethod.getName()
                    + "(" + Arrays.stream(expectationMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")" ,
                    e);
        }
    }
}
