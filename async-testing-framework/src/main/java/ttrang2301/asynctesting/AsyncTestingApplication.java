package ttrang2301.asynctesting;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AsyncTestingApplication {

    public static void run(Class<?> mainClass, String[] args) {
        String primaryBasePackageName = mainClass.getPackage().getName();
        log.info("Start testing application from package " + primaryBasePackageName);
        EnableAsyncTesting annotation = mainClass.getAnnotation(EnableAsyncTesting.class);
        List<String> scannedPackages = new ArrayList<>(Arrays.asList(annotation.basePackages()));
        scannedPackages.add(primaryBasePackageName);
        scannedPackages.addAll(
                Arrays.stream(annotation.basePackageClasses())
                        .map(clazz -> clazz.getPackage().getName()).collect(Collectors.toList()));
        Set<Set<Class<?>>> sets = scannedPackages.stream()
                .map(scannedPackage -> new Reflections(scannedPackage).getTypesAnnotatedWith(AsyncTest.class))
                .collect(Collectors.toSet());
        Set<Class<?>> classes = new HashSet<>();
        sets.forEach(classes::addAll);
        classes.forEach(testingClass -> log.info("=== Test class: {}", testingClass.getName()));
    }


}
