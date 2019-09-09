/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.model;

import java.util.List;

import lombok.Data;

@Data
public class AssertingMethod {

    private String className;
    private String methodName;
    private List<String> parameters;

}
