package com.topleader.topleader;

import org.springframework.test.context.TestContext;


/**
 * Created by Jakub krhovják on 2/20/20.
 */
public class ResetDatabaseAfterTestMethodListener extends AbstractResetDatabaseListener {

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        cleanupDatabase();
    }

}
