package com.inqbarna.inqorm;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by David on 22/09/14.
 */
public abstract class FinderByColumn<T, ID> implements DataTool.Finder<T> {

    private final Class<T> clazz;
    private final ID id;

    public FinderByColumn(Class<T> clazz, ID id) {
        this.clazz = clazz;
        this.id = id;
    }

    @Override
    public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<DataTool.OrderInstrucction> ordering) {
        RuntimeExceptionDao<T, ?> dao = helper.getRuntimeExceptionDao(clazz);

        QueryBuilder<T, ?> builder = dao.queryBuilder();
        try {
            builder.where().eq(targetColumnName(), id);
            return builder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String targetColumnName();

    @Override
    public boolean isMultiRowQuery() {
        return false;
    }

    @Override
    public Class<T> getReturnType() {
        return clazz;
    }
}
