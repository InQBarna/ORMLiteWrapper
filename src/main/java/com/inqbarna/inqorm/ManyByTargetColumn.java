package com.inqbarna.inqorm;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Miriam Dominguez on 04/10/16.
 */

public class ManyByTargetColumn<T, ID> implements DataTool.Finder<T> {
    private final Class<T> clazz;
    private final List<ID> ids;
    private final String columnName;

    public ManyByTargetColumn(Class<T> clazz, Collection<? extends ID> ids, String columnName) {
        this.clazz = clazz;
        this.ids = new ArrayList<>(ids);
        this.columnName = columnName;
    }

    @Override
    public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<DataTool.OrderInstrucction> ordering) {
        try {
            return helper.getRuntimeExceptionDao(clazz).queryBuilder().where().in(columnName, ids).prepare();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMultiRowQuery() {
        return true;
    }

    @Override
    public Class<T> getReturnType() {
        return clazz;
    }
}
