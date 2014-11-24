package com.inqbarna.inqorm;

import com.inqbarna.inqorm.DataTool;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by David on 24/10/14.
 */
public abstract class ManyByIdFinder<T, ID> implements DataTool.Finder<T> {

    private final Class<T> clazz;
    private final List<ID> ids;

    protected ManyByIdFinder(Class<T> clazz, Collection<? extends ID> ids) {
        this.clazz = clazz;
        this.ids = new ArrayList<>(ids);
    }

    @Override
    public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<DataTool.OrderInstrucction> ordering) {
        try {
            return helper.getRuntimeExceptionDao(clazz).queryBuilder().where().in(getIdColumnName(), ids).prepare();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String getIdColumnName();

    @Override
    public boolean isMultiRowQuery() {
        return true;
    }

    @Override
    public Class<T> getReturnType() {
        return clazz;
    }
}
