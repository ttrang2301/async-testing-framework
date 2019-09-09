/**
 * Copyright (c) 2019 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package ttrang2301.asynctesting.persistence;

public class Testcase {

    public enum  Status {
        INITIALIZED("Initialized"), PRECONDITIONS_READY("Ready"), SUCCESSFUL("Successful"), FAILED("Failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    
}
