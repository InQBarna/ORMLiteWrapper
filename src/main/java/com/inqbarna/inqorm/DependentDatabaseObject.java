package com.inqbarna.inqorm;

import com.j256.ormlite.stmt.PreparedDelete;

import java.util.List;

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
