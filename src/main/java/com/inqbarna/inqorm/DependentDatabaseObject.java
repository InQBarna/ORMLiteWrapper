package com.inqbarna.inqorm;

import com.inqbarna.inqorm.DataTool;

/**
 * Created by David on 22/09/14.
 */
public interface DependentDatabaseObject {
    void prepareForDatabase(DataTool dataTool, boolean isUpdate);

    void fillGapsFromDatabase(DataTool dataTool);
}
