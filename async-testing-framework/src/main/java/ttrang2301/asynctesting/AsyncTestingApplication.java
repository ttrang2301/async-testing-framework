package ttrang2301.asynctesting;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AsyncTestingApplication {

    public static void run(Class mainClass, String[] args) {
        String basePackageName = mainClass.getPackage().getName();
        log.info("Start testing application from package " + basePackageName);
        Reflections reflections = new Reflections(basePackageName);
        reflections.getTypesAnnotatedWith(EnableAsyncTesting.class).stream()
                .findFirst()
                .ifPresent(AsyncTestingApplication::scanAnnotatedPackages);
    }

    private static void scanAnnotatedPackages(Class<?> annotatedClass) {
        EnableAsyncTesting annotation = annotatedClass.getAnnotation(EnableAsyncTesting.class);
        List<String> packages = new ArrayList<>(Arrays.asList(annotation.basePackages()));
        packages.addAll(
                Arrays.asList(annotation.basePackageClasses()).stream()
                        .map(clazz -> clazz.getPackage().getName()).collect(Collectors.toList()));
        Reflections reflections = new Reflections(packages);
        Set<Class<?>> testClasses = reflections.getTypesAnnotatedWith(AsyncTest.class);
    }


}
