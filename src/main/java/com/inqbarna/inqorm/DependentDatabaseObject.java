package com.inqbarna.inqorm;

/**
 * Created by David on 22/09/14.
 */
public interface DependentDatabaseObject {

    void beforeDBWrite(DataAccessor dataTool);
    void afterCreated(DataAccessor dataTool);
    void afterUpdated(DataAccessor dataTool);
    void afterWriteCommon(DataAccessor dataTool);

    void fillGapsFromDatabase(DataAccessor dataTool);
}
