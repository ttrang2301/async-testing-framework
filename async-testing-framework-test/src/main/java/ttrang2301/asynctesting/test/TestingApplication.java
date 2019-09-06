package ttrang2301.asynctesting.test;


import ttrang2301.asynctesting.AsyncTestingApplication;
import ttrang2301.asynctesting.EnableAsyncTesting;
import ttrang2301.asynctesting.othertests.case1.OtherTestCase1;

@EnableAsyncTesting(
        basePackages = {"ttrang2301.asynctesting.anothertest"},
        basePackageClasses = {OtherTestCase1.class})
public class TestingApplication {

    public static void main(String[] args) {
        AsyncTestingApplication.run(TestingApplication.class, args);
    }

}
