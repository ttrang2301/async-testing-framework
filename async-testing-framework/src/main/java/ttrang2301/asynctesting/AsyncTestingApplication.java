package ttrang2301.asynctesting;

import org.apache.commons.collections.CollectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.Method;
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
import ttrang2301.asynctesting.model.Campaign;
import ttrang2301.asynctesting.model.CompletionPoint;
import ttrang2301.asynctesting.model.Expectation;
import ttrang2301.asynctesting.model.InvalidMetadataException;
import ttrang2301.asynctesting.model.Precondition;
import ttrang2301.asynctesting.model.TestcaseResult;
import ttrang2301.asynctesting.persistence.TestcaseRepository;

@Slf4j
public class AsyncTestingApplication {

    private Campaign campaign;
    private List<String> eventNames;
    private List<Precondition> preconditions;
    private List<Expectation> expectations;
    private List<TestcaseResult> initialTestcaseResults;

    private TestcaseRepository repository;

    public AsyncTestingApplication(Set<Class<?>> testingClasses,
                                   TestcaseRepository repository) {
        validateTestcaseIdDuplication(testingClasses);

        this.campaign = new Campaign(UUID.randomUUID().toString(),
                "Testing campaign running at " + ZonedDateTime.now(ZoneOffset.UTC));
        this.preconditions = extractPreconditions(testingClasses);
        this.expectations = extractExpectations(testingClasses);
        this.eventNames = extractNamesOfObservedEvents(expectations);
        this.initialTestcaseResults = extractInitialTestcaseResults(this.campaign, testingClasses);

        this.repository = repository;
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
                null);
        application.initializeTestcaseResultDatabase();
        application.createPreconditions();
        application.waitForExpectations();
    }

    private void initializeTestcaseResultDatabase() {
        // TODO
//        repository.insertTestcase(this.campaign.getId());
    }

    private void createPreconditions() {
        // TODO
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

    private static List<String> extractNamesOfObservedEvents(List<Expectation> expectations) {
        if (CollectionUtils.isEmpty(expectations)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(expectations.stream()
                .map(Expectation::getEventName)
                .collect(Collectors.toSet()));
    }

    private static List<Precondition> extractPreconditions(Set<Class<?>> classes) {
        List<Method> preconditionMethods =
                classes.stream().map(clazz -> Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Precondition.class) != null)
                        .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        preconditionMethods.forEach(method -> log.info("=== TestcaseResult: {}", method.getName()));
        // TODO
        return preconditionMethods.stream().map(method -> new Precondition()).collect(Collectors.toList());
    }

    private static List<Expectation> extractExpectations(Set<Class<?>> classes) {
        List<Method> expectationMethods =
                classes.stream()
                        .map(clazz -> Arrays.stream(clazz.getMethods())
                                .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                                .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        validateExpectations(expectationMethods);
        expectationMethods.forEach(method -> log.info("=== Expectation: {}", method.getName()));
        // TODO
        return expectationMethods.stream().map(method -> new Expectation()).collect(Collectors.toList());
    }

    private static List<TestcaseResult> extractInitialTestcaseResults(
            Campaign campaign, Set<Class<?>> testingClasses)
            throws InvalidMetadataException {
        return testingClasses.stream()
                .map(testingClass -> toInitialTestResult(campaign, testingClass))
                .collect(Collectors.toList());
    }

    private static TestcaseResult toInitialTestResult(Campaign campaign, Class<?> testingClass) {
        List<CompletionPoint> completionPoints =  Arrays.stream(testingClass.getMethods())
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
                throw new InvalidMetadataException("Same test ID '" + testId + "' defined in " +
                        "classes " + duplicatedClass.getName() + " and " + testingClass.getName());
            }
            tests.put(testId, testingClass);
        }
    }

}
