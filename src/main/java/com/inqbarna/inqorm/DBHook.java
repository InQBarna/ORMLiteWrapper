package com.inqbarna.inqorm;

/**
 * Created by David on 22/09/14.
 */
public interface DBHook<T> {

    void beforeDBWrite(DataAccessor dataTool, T obj);
    void afterCreated(DataAccessor dataTool, T obj);
    void afterUpdated(DataAccessor dataTool, T obj);
    void afterWriteCommon(DataAccessor dataTool, T obj);

    void fillGapsFromDatabase(DataAccessor dataTool, T obj);
}
