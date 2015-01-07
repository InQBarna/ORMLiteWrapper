package com.inqbarna.inqorm;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;

import java.util.Collection;
import java.util.List;

/**
 * Created by David on 15/09/14.
 */
public abstract class DataTool {

    protected OrmLiteSqliteOpenHelper ormHelper;

    public static final class OrderInstrucction {
        public final String fieldName;
        public final boolean ascending;

        private OrderInstrucction(String fieldName, boolean ascending) {
            this.fieldName = fieldName;
            this.ascending = ascending;
        }

        public static final OrderInstrucction ascending(String fieldName) {
            return new OrderInstrucction(fieldName, true);
        }

        public static final OrderInstrucction descending(String fieldName) {
            return new OrderInstrucction(fieldName, false);
        }
    }

    public interface Finder<T> {
        public PreparedQuery<T> getQuery(OrmLiteSqliteOpenHelper helper, List<OrderInstrucction> ordering);

        public boolean isMultiRowQuery();

        public Class<T> getReturnType();
    }

    public interface Deleter<T> {
        public PreparedDelete<T> getDelete(OrmLiteSqliteOpenHelper helper);

        public Class<T> getItemType();
    }


    public DataTool(OrmLiteSqliteOpenHelper helper) {
        this.ormHelper = helper;
    }


    public <T> T findItem(Finder<T> finder) {
        Class<T> clazz = finder.getReturnType();

        if (finder.isMultiRowQuery()) {
            throw new IllegalArgumentException("Expected single row finder instance");
        }

        PreparedQuery<T> query = finder.getQuery(ormHelper, null);
        T retVal = ormHelper.getRuntimeExceptionDao(clazz).queryForFirst(query);

        if (retVal instanceof DependentDatabaseObject) {
            ((DependentDatabaseObject) retVal).fillGapsFromDatabase(this);
        }
        return retVal;
    }

    public <T> List<T> findMany(Finder<T> finder, List<OrderInstrucction> ordering) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, ordering);
        List<T> items = ormHelper.getRuntimeExceptionDao(clazz).query(query);

        for (T item : items) {
            if (item instanceof DependentDatabaseObject) {
                ((DependentDatabaseObject) item).fillGapsFromDatabase(this);
            }
        }
        return items;
    }

    public <T> List<T> findManyNoRecursive(Finder<T> finder, List<OrderInstrucction> ordering) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, ordering);
        return ormHelper.getRuntimeExceptionDao(clazz).query(query);
    }

    public <T> long getCount(Finder<T> finder) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, null);
        return ormHelper.getRuntimeExceptionDao(clazz).countOf(query);
    }

    public <T> void deleteItems(Deleter<T> deleter) {
        Class<T> clazz = deleter.getItemType();
        PreparedDelete<T> delete = deleter.getDelete(ormHelper);
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(clazz);
        dao.delete(delete);
    }

    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T item) {

        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());

        DependentDatabaseObject dobj = null;
        if (item instanceof DependentDatabaseObject) {
            dobj = (DependentDatabaseObject) item;
            dobj.beforeDBWrite(this);
        }


        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);
        if (null != dobj) {
            if (status.isUpdated()) {
                dobj.afterUpdated(this);
            } else {
                dobj.afterCreated(this);
            }
            dobj.afterWriteCommon(this);
        }
        onItemUpdated(status.isUpdated(), item);
        return status;
    }

    public <T> void createOrUpdateMany(Class<T> clazz, Collection<T> items) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(clazz);

        for (T item : items) {

            DependentDatabaseObject dobj = null;
            if (item instanceof DependentDatabaseObject) {
                dobj = (DependentDatabaseObject) item;
                dobj.beforeDBWrite(this);
            }

            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);

            if (null != dobj) {
                if (status.isUpdated()) {
                    dobj.afterUpdated(this);
                } else {
                    dobj.afterCreated(this);
                }
                dobj.afterWriteCommon(this);
            }

            onItemUpdated(status.isUpdated(), item);

        }
    }

    protected abstract <T> void onItemUpdated(boolean updated, T item);

    public <T> void refreshData(T item) {
        ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass()).refresh(item);

        if (item instanceof DependentDatabaseObject) {
            ((DependentDatabaseObject)item).fillGapsFromDatabase(this);
        }
    }

    public <T> void update(T item) {

        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());

        DependentDatabaseObject dobj = null;

        if (item instanceof DependentDatabaseObject) {
            dobj = (DependentDatabaseObject)item;
            dobj.beforeDBWrite(this);
        }

        dao.update(item);
        if (null != dobj) {
            dobj.afterUpdated(this);
            dobj.afterWriteCommon(this);
        }

        onItemUpdated(true, item);
    }

}
