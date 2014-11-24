package com.inqbarna.inqorm;

import com.inqbarna.inqorm.DataTool;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by David on 22/09/14.
 */
public abstract class FinderById<T, ID> implements DataTool.Finder<T> {

    private final Class<T> clazz;
    private final ID id;

    public FinderById(Class<T> clazz, ID id) {
        this.clazz = clazz;
        this.id = id;
    }

    @Override
    public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<DataTool.OrderInstrucction> ordering) {
        RuntimeExceptionDao<T, Long> dao = helper.getRuntimeExceptionDao(clazz);

        QueryBuilder<T, Long> builder = dao.queryBuilder();
        try {
            builder.where().eq(idColumnName(), id);
            return builder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String idColumnName();

    @Override
    public boolean isMultiRowQuery() {
        return false;
    }

    @Override
    public Class<T> getReturnType() {
        return clazz;
    }
}
