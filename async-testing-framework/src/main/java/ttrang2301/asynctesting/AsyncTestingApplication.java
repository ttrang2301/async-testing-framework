package ttrang2301.asynctesting;

import org.apache.commons.collections.CollectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.model.*;
import ttrang2301.asynctesting.persistence.EventConsumerRepository;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;

@Slf4j
public class AsyncTestingApplication {

    private Campaign campaign;
    private Set<String> eventNames;
    private Map<String, List<Precondition>> preconditionsByTestcase;
    private List<EventConsumer> eventConsumers;
    private List<TestcaseResult> initialTestcaseResults;

    private TestcaseResultRepository testcaseResultRepository;
    private EventConsumerRepository eventConsumerRepository;

    public AsyncTestingApplication(Set<Class<?>> testingClasses,
                                   TestcaseResultRepository testcaseResultRepository,
                                   EventConsumerRepository eventConsumerRepository) {
        validateTestcaseIdDuplication(testingClasses);

        this.campaign = new Campaign(UUID.randomUUID().toString(),
                "Testing campaign running at " + ZonedDateTime.now(ZoneOffset.UTC));
        this.preconditionsByTestcase = extractPreconditionsByTestcases(testingClasses);
        this.eventConsumers = extractExpectations(testingClasses);
        this.eventNames = extractNamesOfObservedEvents(eventConsumers);
        this.initialTestcaseResults = extractInitialTestcaseResults(this.campaign, testingClasses);

        this.testcaseResultRepository = testcaseResultRepository;
        this.eventConsumerRepository = eventConsumerRepository;
    }

    public static void run(Class<?> mainClass, String[] args) {
        String primaryBasePackageName = mainClass.getPackage().getName();
        List<String> scannedPackages = new ArrayList<>();
        scannedPackages.add(primaryBasePackageName);
        scannedPackages.addAll(extractTestingPackages(mainClass.getAnnotation(EnableAsyncTesting.class)));
        Set<Class<?>> testingClasses = extractTestingClasses(scannedPackages);
        AsyncTestingApplication application = new AsyncTestingApplication(
                testingClasses,
                // TODO
                null, null);
        application.initializeTestcaseResultDatabase();
        application.createPreconditions();
        application.waitForExpectations();
    }

    private void initializeTestcaseResultDatabase() {
        testcaseResultRepository.insertTestcaseResults(
                this.initialTestcaseResults.stream().map(TestcaseResult::toPersistedModel).collect(Collectors.toList()     ));
        eventConsumerRepository.insertEventConsumers(
                this.eventConsumers.stream().map(EventConsumer::toPersistedModel).collect(Collectors.toList()));
    }

    private void createPreconditions() {
        for (Map.Entry<String, List<Precondition>> testcasePreconditions : this.preconditionsByTestcase.entrySet()) {
            for (Precondition precondition : testcasePreconditions.getValue()) {
                Object testingObject = null;
                try {
                    testingObject = precondition.getMethod().getDeclaringClass().newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException("Cannot construct instance of " + precondition.getMethod().getDeclaringClass(), e);
                } catch (IllegalAccessException e) {
                    // This must not happen because it should be validated when extracting metadata from source code.
                    // TODO validate
                    throw new RuntimeException(
                            "Cannot construct instance of " + precondition.getMethod().getDeclaringClass()
                            + " because there is no public no-argument constructor",
                            e);
                }
                try {
                    precondition.getMethod().invoke(testingObject);
                } catch (InvocationTargetException e) {
                    // TODO
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // This must not happen because it should be validated when extracting metadata from source code.
                    throw new RuntimeException("Cannot invoke precondition "
                            + precondition.getMethod().getDeclaringClass().getName() + "#"
                            + precondition.getMethod().getName()
                            + "(" + Arrays.stream(precondition.getMethod().getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")" ,
                            e);
                }
            }
            testcaseResultRepository.updateStatus(
                    this.campaign.getId(),
                    testcasePreconditions.getKey(),
                    TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.PRECONDITIONS_READY));
        }
    }

    private void waitForExpectations() {
        // TODO
    }

    private static List<String> extractTestingPackages(EnableAsyncTesting annotation) {
        List<String> scannedPackages = new ArrayList<>(Arrays.asList(annotation.basePackages()));
        scannedPackages.addAll(
                Arrays.stream(annotation.basePackageClasses())
                        .map(clazz -> clazz.getPackage().getName())
                        .collect(Collectors.toList()));
        return scannedPackages;
    }

    private static Set<Class<?>> extractTestingClasses(List<String> scannedPackages) {
        Set<Set<Class<?>>> sets =
                scannedPackages.stream().map(scannedPackage -> new Reflections(scannedPackage).getTypesAnnotatedWith(AsyncTest.class)).collect(Collectors.toSet());
        Set<Class<?>> classes = new HashSet<>();
        sets.forEach(classes::addAll);
        classes.forEach(testingClass -> log.info("=== Test class: {}", testingClass.getName()));
        return classes;
    }

    private static Set<String> extractNamesOfObservedEvents(List<EventConsumer> eventConsumers) {
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

    private static Map<String, List<Precondition>> extractPreconditionsByTestcases(Set<Class<?>> testingClasses) {
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

    private static List<EventConsumer> extractExpectations(Set<Class<?>> classes) {
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

    private static List<TestcaseResult> extractInitialTestcaseResults(
            Campaign campaign, Set<Class<?>> testingClasses)
            throws InvalidMetadataException {
        return testingClasses.stream()
                .map(testingClass -> toInitialTestResult(campaign, testingClass))
                .collect(Collectors.toList());
    }

    private static TestcaseResult toInitialTestResult(Campaign campaign, Class<?> testingClass) {
        List<CompletionPoint> completionPoints =  Arrays.stream(testingClass.getDeclaredMethods())
                .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                .map(method -> method.getParameters()[0])
                .map(Parameter::getType)
                .map(eventClass -> new CompletionPoint(eventClass.getName(), false))
            .collect(Collectors.toList());
        return new TestcaseResult(
                campaign.getId(),
                testingClass.getAnnotation(AsyncTest.class).name(), completionPoints,
                TestcaseResult.Status.INITIALIZED);
    }

    private static void validateTestcaseIdDuplication(Set<Class<?>> testingClasses) throws InvalidMetadataException {
        Map<String, Class<?>> tests = new HashMap<>();
        for (Class<?> testingClass : testingClasses) {
            String testId = testingClass.getAnnotation(AsyncTest.class).name();
            Class<?> duplicatedClass = tests.get(testId);
            if (duplicatedClass != null) {
                throw new InvalidMetadataException("Same sample ID '" + testId + "' defined in " +
                        "classes " + duplicatedClass.getName() + " and " + testingClass.getName());
            }
            tests.put(testId, testingClass);
        }
    }

}
