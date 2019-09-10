package ttrang2301.asynctesting.expectations;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import ttrang2301.asynctesting.InvalidMetadataException;
import ttrang2301.asynctesting.annotation.AsyncTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class EventConsumer {

    private String eventName;
    private Expectation expectation;
    private String testcaseId;

    public static final ttrang2301.asynctesting.persistence.EventConsumer toPersistedModel(EventConsumer model) {
        // TODO
        return null;
    }

    public static List<EventConsumer> extractExpectations(Set<Class<?>> classes) {
        List<EventConsumer> eventConsumers =
                classes.stream()
                        .map(clazz -> {
                            List<Method> expectationMethods = Arrays.stream(clazz.getDeclaredMethods())
                                    .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                                    .collect(Collectors.toList());
                            return expectationMethods.stream()
                                    .map(expectationMethod -> new EventConsumer(
                                            expectationMethod.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class).eventName(),
                                            new Expectation(expectationMethod),
                                            clazz.getAnnotation(AsyncTest.class).name()))
                                    .collect(Collectors.toList());
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        validateExpectations(eventConsumers.stream().map(eventConsumer -> eventConsumer.getExpectation().getMethod()).collect(Collectors.toList()));
        return eventConsumers;
    }

    public static Set<String> extractNamesOfObservedEvents(List<EventConsumer> eventConsumers) {
        if (CollectionUtils.isEmpty(eventConsumers)) {
            return Collections.emptySet();
        }
        return eventConsumers.stream()
                .map(eventConsumer -> eventConsumer
                        .getExpectation()
                        .getMethod()
                        .getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class)
                        .eventName())
                .collect(Collectors.toSet());
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
