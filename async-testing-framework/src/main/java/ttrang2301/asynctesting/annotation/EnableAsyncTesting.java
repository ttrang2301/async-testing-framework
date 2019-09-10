package ttrang2301.asynctesting.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableAsyncTesting {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
