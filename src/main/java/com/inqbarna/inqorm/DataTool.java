package com.inqbarna.inqorm;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.support.ConnectionSource;

import java.util.Collection;
import java.util.List;

/**
 * Created by David on 15/09/14.
 */
public abstract class DataTool implements DataAccessor {

    public static final int FULL_RECURSIVE = -1;
    public static final int NO_RECURSIVE = 0;

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

    public interface DeleterWithDependency<T> extends Deleter<T> {

        public List<Deleter<?>> getDependentDeletes();

    }

    public interface Updater<T> {
        public PreparedUpdate<T> getUpdate(OrmLiteSqliteOpenHelper helper);
        public Class<T> getItemType();
    }


    public DataTool(OrmLiteSqliteOpenHelper helper) {
        this.ormHelper = helper;
    }

    ConnectionSource getConnectionSource() {
        if (null != ormHelper) {
            return ormHelper.getConnectionSource();
        } else {
            return null;
        }
    }

    DataTool() {
        // constructor needed for RecursiveDataTool
    }


    @Override
    public <T> T findItem(Finder<T> finder) {
        return findItem(finder, FULL_RECURSIVE);
    }

    public <T> T findItem(Finder<T> finder, int recursionLevel) {
        Class<T> clazz = finder.getReturnType();

        if (finder.isMultiRowQuery()) {
            throw new IllegalArgumentException("Expected single row finder instance");
        }

        PreparedQuery<T> query = finder.getQuery(ormHelper, null);
        T retVal = ormHelper.getRuntimeExceptionDao(clazz).queryForFirst(query);

        if (recursionLevel != NO_RECURSIVE && null != retVal) {
            DBHook<T> hook = (DBHook<T>) getHook(retVal.getClass());
            if (null != hook) {
                hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), retVal);
            }
        }
        return retVal;
    }

    @Override
    public <T> List<T> findMany(Finder<T> finder, List<OrderInstrucction> ordering) {
        return findMany(finder, ordering, FULL_RECURSIVE);
    }

    public <T> List<T> findMany(Finder<T> finder, List<OrderInstrucction> ordering, int recursionLevel) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, ordering);
        List<T> items = ormHelper.getRuntimeExceptionDao(clazz).query(query);

        if (recursionLevel != NO_RECURSIVE && null != items) {
            DBHook<T> hook = getHook(clazz);
            for (T item : items) {
                if (null != hook) {
                    hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), item);
                }
            }
        }
        return items;
    }

    public <T> List<T> findManyNoRecursive(Finder<T> finder, List<OrderInstrucction> ordering) {
        return findMany(finder, ordering, NO_RECURSIVE);
    }

    @Override
    public <T> long getCount(Finder<T> finder) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, null);
        return ormHelper.getRuntimeExceptionDao(clazz).countOf(query);
    }

    @Override
    public <T> void deleteItems(Deleter<T> deleter) {

        if (DeleterWithDependency.class.isAssignableFrom(deleter.getClass())) {
            DeleterWithDependency<T> dependDeleter = (DeleterWithDependency<T>) deleter;
            List<Deleter<?>> innerDeleters = dependDeleter.getDependentDeletes();
            if (null != innerDeleters) {
                for (Deleter<?> innerD : innerDeleters) {
                    deleteItems(innerD);
                }
            }
        }

        Class<T> clazz = deleter.getItemType();
        PreparedDelete<T> delete = deleter.getDelete(ormHelper);
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(clazz);
        dao.delete(delete);
    }

    @Override
    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T item) {

        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>) item.getClass());

        DBHook<T> hook = (DBHook<T>) getHook(item.getClass());
        if (null != hook) {
            hook.beforeDBWrite(this, item);
        }


        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);
        if (null != hook) {
            if (status.isUpdated()) {
                hook.afterUpdated(this, item);
            } else {
                hook.afterCreated(this, item);
            }
            hook.afterWriteCommon(this, item);
        }
        onItemUpdated(status.isUpdated(), item);
        return status;
    }

    @Override
    public <T> T createIfNotExists(T item) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());
        T result = dao.createIfNotExists(item);
        return result;
    }

    @Override
    public <T> void createOrUpdateMany(Class<T> clazz, Collection<T> items) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(clazz);

        DBHook<T> hook = getHook(clazz);
        for (T item : items) {

            if (null != hook) {
                hook.beforeDBWrite(this, item);
            }

            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);

            if (null != hook) {
                if (status.isUpdated()) {
                    hook.afterUpdated(this, item);
                } else {
                    hook.afterCreated(this, item);
                }
                hook.afterWriteCommon(this, item);
            }

            onItemUpdated(status.isUpdated(), item);

        }
    }

    public <T> void bulkUpdate(Updater<T> updater) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(updater.getItemType());
        dao.update(updater.getUpdate(ormHelper));
    }

    protected abstract <T> void onItemUpdated(boolean updated, T item);

    @Override
    public <T> void refreshData(T item) {
        ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass()).refresh(item);

        DBHook<T> hook = (DBHook<T>) getHook(item.getClass());
        if (null != hook) {
            hook.fillGapsFromDatabase(this, item);
        }
    }

    @Override
    public <T> void refreshAll(Collection<T> items) {
        RuntimeExceptionDao<T, ?> dao = null;
        DBHook<T> hook = null;
        boolean hookChecked = false;
        for (T item : items) {
            if (null == dao) {
               dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());
            }

            if (!hookChecked) {
                hookChecked = true;
                hook = (DBHook<T>) getHook(item.getClass());
            }

            dao.refresh(item);

            if (null != hook) {
                hook.fillGapsFromDatabase(this, item);
            }
        }
    }

    @Override
    public Transaction beginTransaction() {
        return new Transaction(this);
    }

    @Override
    public <T> void update(T item) {

        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());


        DBHook<T> hook = (DBHook<T>) getHook(item.getClass());
        if (null != hook) {
            hook.beforeDBWrite(this, item);
        }

        dao.update(item);
        if (null != hook) {
            hook.afterUpdated(this, item);
            hook.afterWriteCommon(this, item);
        }

        onItemUpdated(true, item);
    }

    protected <T> DBHook<T> getHook(Class<T> clazz) {
        return null;
    }

}
