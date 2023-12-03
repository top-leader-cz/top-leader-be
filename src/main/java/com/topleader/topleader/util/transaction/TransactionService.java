/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util.transaction;

import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author Daniel Slavik
 */
@Service
@NoArgsConstructor
public class TransactionService implements ApplicationContextAware {
    private ApplicationContext context;

    public void execute(final TransactionProcessWithoutResult transaction) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.context.getBean(PlatformTransactionManager.class));
        transactionTemplate.setPropagationBehavior(3);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                transaction.execute();
            }
        });
    }

    public <T> T execute(TransactionProcess<T> transaction) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.context.getBean(PlatformTransactionManager.class));
        transactionTemplate.setPropagationBehavior(3);
        return transactionTemplate.execute((transactionStatus) -> transaction.execute());
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
