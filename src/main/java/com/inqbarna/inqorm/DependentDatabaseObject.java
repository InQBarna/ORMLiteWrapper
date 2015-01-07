package com.inqbarna.inqorm;

import com.inqbarna.inqorm.DataTool;

/**
 * Created by David on 22/09/14.
 */
public interface DependentDatabaseObject {

    void beforeDBWrite(DataTool dataTool);
    void afterCreated(DataTool dataTool);
    void afterUpdated(DataTool dataTool);
    void afterWriteCommon(DataTool dataTool);

    void fillGapsFromDatabase(DataTool dataTool);
}
