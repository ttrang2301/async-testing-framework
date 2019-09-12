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

@Data
@AllArgsConstructor
public class Expectation {

    private String testcaseId;
    private String key;
    private Method method;

    public static Map<String, List<Expectation>> extractExpectationsByTestcase(Set<Class<?>> testingClasses) {
        Map<String, List<Expectation>> expectationsByEvent = new HashMap<>();
        for (Class<?> testingClass : testingClasses) {
            String testcaseId = testingClass.getAnnotation(AsyncTest.class).name();
            List<Method> expectationMethods = Arrays.stream(testingClass.getMethods())
                    .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                    .collect(Collectors.toList());
            for (Method expectationMethod : expectationMethods) {
                ttrang2301.asynctesting.annotation.Expectation expectationMethodAnnotation =
                        expectationMethod.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class);
                String eventName = expectationMethodAnnotation.eventName();
                List<Expectation> expectations = expectationsByEvent.get(eventName);
                if (expectations == null) {
                    expectations = new ArrayList<>();
                    expectationsByEvent.put(eventName, expectations);
                }
                expectations.add(new Expectation(testcaseId, expectationMethodAnnotation.key(), expectationMethod));
            }
        }
        validateExpectations(expectationsByEvent.values().stream()
                .map(expectations -> expectations.stream().map(Expectation::getMethod).collect(Collectors.toList()))
                .flatMap(List::stream)
                .collect(Collectors.toList()));
        return expectationsByEvent;
    }

    private static void validateExpectations(List<Method> expectationMethods) throws InvalidMetadataException {
        for (Method method : expectationMethods) {
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
