package ttrang2301.asynctesting.preconditions;

import org.apache.commons.collections.CollectionUtils;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;
import ttrang2301.asynctesting.preconditions.Precondition;
import ttrang2301.asynctesting.testcases.Campaign;
import ttrang2301.asynctesting.testcases.TestcaseResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreatePreconditionService {

    private TestcaseResultRepository testcaseResultRepository;

    public CreatePreconditionService(TestcaseResultRepository testcaseResultRepository) {
        this.testcaseResultRepository = testcaseResultRepository;
    }

    public void createPreconditions(Campaign campaign, Map<String, List<Precondition>> preconditionsByTestcase) {
        for (Map.Entry<String, List<Precondition>> entry : preconditionsByTestcase.entrySet()) {
            List<Precondition> testcasePreconditions = entry.getValue();
            if (CollectionUtils.isEmpty(testcasePreconditions)) {
                continue;
            }
            String testcaseId = entry.getKey();
            try {
                Object testingObject = initializeTestingObject(campaign, testcaseId, testcasePreconditions);
                for (Precondition precondition : testcasePreconditions) {
                    createPreconditionOfTestcase(testingObject, precondition.getMethod());
                }
            } catch (Exception e) {
                testcaseResultRepository.updateStatus(
                        campaign.getId(),
                        testcaseId,
                        TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.FAILED));
                // TODO Further enhancement: When a testcase fail for any reason, the test suite should be going on
                throw e;
            }
            testcaseResultRepository.updateStatus(
                    campaign.getId(),
                    testcaseId,
                    TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.PRECONDITIONS_READY));
        }
    }

    private Object initializeTestingObject(Campaign campaign, String testcaseId, List<Precondition> testcasePreconditions) {
        Class<?> testingClass = testcasePreconditions.get(0).getMethod().getDeclaringClass();
        Object testingObject;
        try {
            testingObject = testingClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Exception occurs constructing instance of " + testingClass, e);
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            testcaseResultRepository.updateStatus(
                    campaign.getId(),
                    testcaseId,
                    TestcaseResult.Status.toPersistedModel(TestcaseResult.Status.FAILED));
            throw new RuntimeException(
                    "Cannot construct instance of " + testingClass
                            + " because there is no public no-argument constructor",
                    e);
        }
        return testingObject;
    }

    private void createPreconditionOfTestcase(Object testingObject, Method preconditionMethod) {
        try {
            preconditionMethod.invoke(testingObject);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Exception occurs invoking precondition "
                    + preconditionMethod.getDeclaringClass().getName() + "#"
                    + preconditionMethod.getName()
                    + "(" + Arrays.stream(preconditionMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")" ,
                    e);
        } catch (IllegalAccessException e) {
            // This must not happen because it should be validated when extracting metadata from source code.
            throw new RuntimeException("Cannot invoke precondition "
                    + preconditionMethod.getDeclaringClass().getName() + "#"
                    + preconditionMethod.getName()
                    + "(" + Arrays.stream(preconditionMethod.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")) + ")" ,
                    e);
        }
    }
}
