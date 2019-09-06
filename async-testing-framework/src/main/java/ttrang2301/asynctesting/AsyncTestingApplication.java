package ttrang2301.asynctesting;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTestingApplication {

    public static void run(Class<?> mainClass, String[] args) {
        String primaryBasePackageName = mainClass.getPackage().getName();
        log.info("Start testing application from package " + primaryBasePackageName);
        EnableAsyncTesting annotation = mainClass.getAnnotation(EnableAsyncTesting.class);

        // Collect should-be-scanned packages
        List<String> scannedPackages = new ArrayList<>(Arrays.asList(annotation.basePackages()));
        scannedPackages.add(primaryBasePackageName);
        scannedPackages.addAll(
                Arrays.stream(annotation.basePackageClasses())
                        .map(clazz -> clazz.getPackage().getName()).collect(Collectors.toList()));

        // Collect testing classes
        Set<Set<Class<?>>> sets = scannedPackages.stream()
                .map(scannedPackage -> new Reflections(scannedPackage).getTypesAnnotatedWith(AsyncTest.class))
                .collect(Collectors.toSet());
        Set<Class<?>> classes = new HashSet<>();
        sets.forEach(classes::addAll);
        classes.forEach(testingClass -> log.info("=== Test class: {}", testingClass.getName()));

        // Collect preconditions
        List<Method> preconditionMethods =
                classes.stream()
                        .map(clazz -> Arrays.stream(clazz.getMethods())
                                .filter(method -> method.getAnnotation(Precondition.class) != null)
                                .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        preconditionMethods.forEach(method -> log.info("=== Precondition: {}", method.getName()));

        // Collect expectations
        List<Method> expectationMethods =
                classes.stream()
                        .map(clazz -> Arrays.stream(clazz.getMethods())
                                .filter(method -> method.getAnnotation(Expectation.class) != null)
                                .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        expectationMethods.forEach(method -> log.info("=== Expectation: {}", method.getName()));

    }


}
