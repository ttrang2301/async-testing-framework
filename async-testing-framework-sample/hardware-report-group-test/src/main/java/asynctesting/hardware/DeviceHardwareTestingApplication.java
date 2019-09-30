package asynctesting.hardware;

import ttrang2301.asynctesting.AsyncTestingApplication;
import ttrang2301.asynctesting.annotation.EnableAsyncTesting;

@EnableAsyncTesting
public class DeviceHardwareTestingApplication {
    public static void main(String[] args) {
        AsyncTestingApplication.run(DeviceHardwareTestingApplication.class, args);
    }
}
