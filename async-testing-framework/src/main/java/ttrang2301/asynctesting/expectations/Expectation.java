/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.expectations;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import ttrang2301.asynctesting.InvalidMetadataException;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.testcases.Testcase;

@Data
@AllArgsConstructor
public class Expectation {

    private Method method;

    public String getKey() {
        return this.method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class).key();
    }

    public Testcase getTestcase() {
        return Testcase.extractTestcase(method.getDeclaringClass());
    }

    public String getObservedEventName() {
        return this.method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class).eventName();
    }

    public Class<?> getObservedEventClass() {
        return this.method.getParameters()[0].getType();
    }

    public static Map<String, List<Expectation>> extractExpectationsByEvent(Set<Class<?>> testingClasses) {
        Map<String, List<Expectation>> expectationsByEvent = new HashMap<>();
        for (Class<?> testingClass : testingClasses) {
            List<Method> expectationMethods = Arrays.stream(testingClass.getMethods())
                    .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                    .collect(Collectors.toList());
            for (Method expectationMethod : expectationMethods) {
                Expectation expectation = new Expectation(expectationMethod);
                String eventName = expectation.getObservedEventName();
                List<Expectation> expectations = expectationsByEvent.get(eventName);
                if (expectations == null) {
                    expectations = new ArrayList<>();
                    expectationsByEvent.put(eventName, expectations);
                }
                expectations.add(expectation);
            }
        }
        validateExpectations(
                expectationsByEvent.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        return expectationsByEvent;
    }

    /**
     * Validate:
     * <ul>
     *     <li>Expectation method must contain 1 and only 1 argument.</li>
     *     <li>Expectation method must be public.</li>
     * </ul>
     * @param expectations
     * @throws InvalidMetadataException
     */
    private static void validateExpectations(List<Expectation> expectations) throws InvalidMetadataException {
        for (Expectation expectation : expectations) {
            Method method = expectation.getMethod();
            if (method.getParameterCount() != 1) {
                throw new InvalidMetadataException("Expected 1 and only 1 parameter passed to expectation instead of "
                        + method.getDeclaringClass().getName() + "#" + method.getName()
                        + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")");
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new InvalidMetadataException("Expected public expectation instead of "
                        + method.toGenericString() + " "
                        + method.getDeclaringClass().getName() + "#" + method.getName()
                        + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")");
            }
        }
    }


}
