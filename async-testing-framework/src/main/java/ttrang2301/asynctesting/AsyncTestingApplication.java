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
        List<String> scannedPackages = new ArrayList<>();
        scannedPackages.add(primaryBasePackageName);
        scannedPackages.addAll(extractTestingPackages(mainClass.getAnnotation(EnableAsyncTesting.class)));
        Set<Class<?>> classes = extractTestingClasses(scannedPackages);
        extractPreconditions(classes);
        extractExpectations(classes);

    }

    private static List<String> extractTestingPackages(EnableAsyncTesting annotation) {
        List<String> scannedPackages = new ArrayList<>(Arrays.asList(annotation.basePackages()));
        scannedPackages.addAll(
                Arrays.stream(annotation.basePackageClasses())
                        .map(clazz -> clazz.getPackage().getName()).collect(Collectors.toList()));
        return scannedPackages;
    }

    private static Set<Class<?>> extractTestingClasses(List<String> scannedPackages) {
        Set<Set<Class<?>>> sets = scannedPackages.stream()
                .map(scannedPackage -> new Reflections(scannedPackage).getTypesAnnotatedWith(AsyncTest.class))
                .collect(Collectors.toSet());
        Set<Class<?>> classes = new HashSet<>();
        sets.forEach(classes::addAll);
        classes.forEach(testingClass -> log.info("=== Test class: {}", testingClass.getName()));
        return classes;
    }

    private static void extractPreconditions(Set<Class<?>> classes) {
        List<Method> preconditionMethods =
                classes.stream()
                        .map(clazz -> Arrays.stream(clazz.getMethods())
                                .filter(method -> method.getAnnotation(Precondition.class) != null)
                                .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        preconditionMethods.forEach(method -> log.info("=== Precondition: {}", method.getName()));
    }

    private static void extractExpectations(Set<Class<?>> classes) {
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
