/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.preconditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import ttrang2301.asynctesting.InvalidMetadataException;
import ttrang2301.asynctesting.annotation.AsyncTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Precondition {

    private Method method;

    public static Map<String, List<Precondition>> extractPreconditionsByTestcases(Set<Class<?>> testingClasses) {
        Map<String, List<Precondition>> preconditionsByTestcase = testingClasses.stream().collect(Collectors.toMap(
                testingClass -> testingClass.getAnnotation(AsyncTest.class).name(),
                testingClass -> Arrays.stream(testingClass.getDeclaredMethods())
                        .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Precondition.class) != null)
                        .map(Precondition::new)
                        .collect(Collectors.toList())
        ));
        validatePreconditions(preconditionsByTestcase.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        return preconditionsByTestcase;
    }

    private static void validatePreconditions(List<Precondition> preconditions) {
        for (Precondition precondition : preconditions) {
            Method method = precondition.getMethod();
            if (method.getParameterCount() != 0) {
                throw new InvalidMetadataException("Expected 0 parameter passed to precondition instead of "
                        + method.getDeclaringClass().getName() + "#" + method.getName()
                        + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")");
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new InvalidMetadataException("Expected public precondition instead of "
                        + method.toGenericString() + " "
                        + method.getDeclaringClass().getName() + "#" + method.getName()
                        + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")");
            }
        }
    }

}
