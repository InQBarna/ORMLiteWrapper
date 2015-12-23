package com.inqbarna.inqorm;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 23/12/15
 */
public interface Yielder {

    long NO_SLEEP_AFTER = -1;

    boolean yieldTransaction(long sleepAfter);
    boolean inTransaction();
}
