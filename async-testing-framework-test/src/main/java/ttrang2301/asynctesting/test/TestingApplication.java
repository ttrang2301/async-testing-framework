package ttrang2301.asynctesting.test;


import ttrang2301.asynctesting.AsyncTestingApplication;
import ttrang2301.asynctesting.EnableAsyncTesting;

@EnableAsyncTesting
public class TestingApplication {

    public static void main(String[] args) {
        AsyncTestingApplication.run(TestingApplication.class, args);
    }

}
