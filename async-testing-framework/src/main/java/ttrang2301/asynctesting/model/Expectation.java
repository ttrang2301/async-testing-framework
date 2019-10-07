/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.model;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Expectation {

    private Method method;
//    private CompletionPoint completionPoint;
//
//    public Expectation(String eventName, String testId, AssertingMethod assertingMethod, CompletionPoint completionPoint) {
//        this.eventName = eventName;
//        this.assertingMethod = assertingMethod;
//        this.testId = testId;
//        this.completionPoint = completionPoint;
//    }

//    public Expectation(String eventName, String assertingMethod, String testId, CompletionPoint completionPoint) {
//        this.eventName = eventName;
//        this.assertingMethod = assertingMethod;
//        this.testId = testId;
//        this.completionPoint = completionPoint;
//    }
}
