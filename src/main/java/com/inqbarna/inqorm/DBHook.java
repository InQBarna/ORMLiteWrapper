package com.inqbarna.inqorm;

/**
 * Created by David on 22/09/14.
 */
public interface DBHook<T> {

    void beforeDBWrite(DataAccessor dataTool, T obj, Object hookOptions);
    void afterCreated(DataAccessor dataTool, T obj, Object hookOptions);
    void afterUpdated(DataAccessor dataTool, T obj, Object hookOptions);
    void afterWriteCommon(DataAccessor dataTool, T obj, Object hookOptions);

    void fillGapsFromDatabase(DataAccessor dataTool, T obj, Object hookOptions);
}
