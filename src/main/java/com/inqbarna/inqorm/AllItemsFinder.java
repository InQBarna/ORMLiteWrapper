package com.inqbarna.inqorm;

import com.inqbarna.inqorm.DataTool;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by David on 15/09/14.
 */
public class AllItemsFinder<T> implements DataTool.Finder<T> {
    private Class<T> clazz;
    public AllItemsFinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<DataTool.OrderInstrucction> ordering) {
        RuntimeExceptionDao<T, Long> dao = helper.getRuntimeExceptionDao(clazz);
        QueryBuilder<T, Long> builder = dao.queryBuilder();

        if (null != ordering) {
            for (DataTool.OrderInstrucction oi : ordering) {
                builder.orderBy(oi.fieldName, oi.ascending);
            }
        }

        try {
            return builder.prepare();
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
