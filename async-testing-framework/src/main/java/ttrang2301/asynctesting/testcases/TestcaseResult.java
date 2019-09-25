/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.testcases;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import ttrang2301.asynctesting.InvalidMetadataException;
import ttrang2301.asynctesting.annotation.AsyncTest;

@Data
@AllArgsConstructor
public class TestcaseResult {

    private String campaignId;
    private String testId;
    private List<CompletionPoint> completionPoints;
    private Status status;

    public static final ttrang2301.asynctesting.persistence.TestcaseResult toPersistedModel(TestcaseResult model) {
        return new ttrang2301.asynctesting.persistence.TestcaseResult(
                model.getCampaignId(), model.getTestId(),
                model.getCompletionPoints().stream().map(CompletionPoint::toPersistedModel).collect(Collectors.toList()),
                TestcaseResult.Status.toPersistedModel(model.getStatus())
        );
    }

    public static List<TestcaseResult> extractInitialTestcaseResults(
            Campaign campaign, Set<Class<?>> testingClasses)
            throws InvalidMetadataException {
        return testingClasses.stream()
                .map(testingClass -> toInitialTestResult(campaign, testingClass))
                .collect(Collectors.toList());
    }

    private static TestcaseResult toInitialTestResult(Campaign campaign, Class<?> testingClass) {
        List<CompletionPoint> completionPoints =  Arrays.stream(testingClass.getDeclaredMethods())
                .filter(method -> method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class) != null)
                .map(method ->
                        new CompletionPoint(
                                method.getAnnotation(ttrang2301.asynctesting.annotation.Expectation.class).key(),
                                false))
                .collect(Collectors.toList());
        return new TestcaseResult(
                campaign.getId(),
                testingClass.getAnnotation(AsyncTest.class).name(), completionPoints,
                TestcaseResult.Status.INITIALIZED);
    }

    public enum  Status {
        INITIALIZED("Initialized"), PRECONDITIONS_READY("Ready"), SUCCESSFUL("Successful"), FAILED("Failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public static ttrang2301.asynctesting.persistence.TestcaseResult.Status toPersistedModel(Status testcaseResultStatus) {
            switch (testcaseResultStatus) {
                case INITIALIZED:
                    return ttrang2301.asynctesting.persistence.TestcaseResult.Status.INITIALIZED;
                case PRECONDITIONS_READY:
                    return ttrang2301.asynctesting.persistence.TestcaseResult.Status.PRECONDITIONS_READY;
                case SUCCESSFUL:
                    return ttrang2301.asynctesting.persistence.TestcaseResult.Status.SUCCESSFUL;
                case FAILED:
                    return ttrang2301.asynctesting.persistence.TestcaseResult.Status.FAILED;
                    default:
                        throw new RuntimeException("Not supported Status " + testcaseResultStatus);
            }
        }

        public String getValue() {
            return value;
        }
    }

}
