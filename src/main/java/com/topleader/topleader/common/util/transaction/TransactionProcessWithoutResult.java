package com.topleader.topleader.common.util.transaction;

/**
 * @author Daniel Slavik
 */
@FunctionalInterface
public interface TransactionProcessWithoutResult {
    void execute();
}
