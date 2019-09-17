package ttrang2301.asynctesting.testcases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import ttrang2301.asynctesting.annotation.EnableAsyncTesting;
import ttrang2301.asynctesting.InvalidMetadataException;
import ttrang2301.asynctesting.annotation.AsyncTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@AllArgsConstructor
public class Testcase {

    private String id;

    public static Set<Class<?>> extractTestingClasses(Class<?> mainClass) {
        String primaryBasePackageName = mainClass.getPackage().getName();
        List<String> scannedPackages = new ArrayList<>();
        scannedPackages.add(primaryBasePackageName);
        scannedPackages.addAll(extractTestingPackages(mainClass.getAnnotation(EnableAsyncTesting.class)));
        Set<Class<?>> testingClasses = extractTestingClasses(scannedPackages);
        validateTestingClassConstructors(testingClasses);
        validateTestcaseIdDuplication(testingClasses);
        return testingClasses;
    }

    public static Testcase extractTestcase(Class<?> testingClass) {
        AsyncTest testingClassAnnotation = testingClass.getAnnotation(AsyncTest.class);
        return new Testcase(testingClassAnnotation.name());
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

    private static void validateTestingClassConstructors(Set<Class<?>> testingClasses) {
        for (Class<?> testingClass : testingClasses) {
            boolean validConstructorExisting = false;
            for (Constructor constructor : testingClass.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 0 && Modifier.isPublic(constructor.getModifiers())) {
                    validConstructorExisting = true;
                }
            }
            if (!validConstructorExisting) {
                throw new InvalidMetadataException("Require public with no argument construct in class " + testingClass.getName());
            }
        }
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
}
