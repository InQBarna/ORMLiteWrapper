package com.inqbarna.inqorm;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by David on 15/09/14.
 */
public abstract class DataTool implements DataAccessor {

    public static final int FULL_RECURSIVE = -1;
    public static final int NO_RECURSIVE = 0;
    private static boolean DEBUG = false;

    public static final void enableDebug(boolean enable) {
        DEBUG = enable;
    }

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


    protected void onHookRelease(DBHook<?> hook) {

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
    public <T, ID> T getById(Class<T> clazz, ID id) {
        return getById(clazz, id, NO_RECURSIVE);
    }

    public <T, ID> T getById(Class<T> clazz, ID id, int recursionLevel) {
        RuntimeExceptionDao<T, ID> dao = ormHelper.getRuntimeExceptionDao(clazz);
        T item = dao.queryForId(id);
        if (recursionLevel != NO_RECURSIVE && null != item) {
            DBHook<T> hook = getHook(clazz);
            if (null != hook) {
                hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), item, getCurrentHookOptions());
                onHookRelease(hook);
            }
        }
        if (DEBUG) {
            DBLogger.debug("Requested item of class " + clazz.getSimpleName() + " with id " + id + ", got: " + item);
        }
        return item;
    }

    @Override
    public <T, ID> ID getObjectID(T item) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>) item.getClass());
        return (ID) idOfItem(dao, item);
    }

    private <T, ID> ID idOfItem(RuntimeExceptionDao<T, ID> dao, T item) {
        return dao.extractId(item);
    }

    @Override
    public <T> T findItem(Finder<T> finder) {
        return findItem(finder, NO_RECURSIVE);
    }

    public <T> T findItem(Finder<T> finder, int recursionLevel) {
        Class<T> clazz = finder.getReturnType();

        if (finder.isMultiRowQuery()) {
            throw new IllegalArgumentException("Expected single row finder instance");
        }

        PreparedQuery<T> query = finder.getQuery(ormHelper, null);
        T retVal = ormHelper.getRuntimeExceptionDao(clazz).queryForFirst(query);

        if (DEBUG) {
            String statement = getStatementNoThrow(query);
            DBLogger.debug("[findItem]: " + statement);
            DBLogger.debug("[findItem]: Got: " + retVal);
        }

        if (recursionLevel != NO_RECURSIVE && null != retVal) {
            DBHook<T> hook = (DBHook<T>) getHook(retVal.getClass());
            if (null != hook) {
                hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), retVal, getCurrentHookOptions());
                onHookRelease(hook);
            }
        }
        return retVal;
    }

    private <T> String getStatementNoThrow(PreparedStmt<T> query) {
        try {
            return query.getStatement();
        } catch (SQLException e) {
            DBLogger.error("Error getting statement", e);
            return "";
        }
    }

    @Override
    public <T> List<T> findMany(Finder<T> finder, List<OrderInstrucction> ordering) {
        return findMany(finder, ordering, NO_RECURSIVE);
    }

    public <T> List<T> findMany(Finder<T> finder, List<OrderInstrucction> ordering, int recursionLevel) {
        Class<T> clazz = finder.getReturnType();
        PreparedQuery<T> query = finder.getQuery(ormHelper, ordering);
        List<T> items = ormHelper.getRuntimeExceptionDao(clazz).query(query);

        if (DEBUG) {
            DBLogger.debug("[findMany]: " + getStatementNoThrow(query));
            DBLogger.debug("[findMany]: Got: " + items);
        }

        if (recursionLevel != NO_RECURSIVE && null != items) {
            DBHook<T> hook = getHook(clazz);
            for (T item : items) {
                if (null != hook) {
                    hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), item, getCurrentHookOptions());
                }
            }
            if (null != hook) {
                onHookRelease(hook);
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
        int numDeleted = dao.delete(delete);
        if (DEBUG) {
            DBLogger.debug("[deleteItems]: " + getStatementNoThrow(delete));
            DBLogger.debug("[deleteItems]: deletedCount = " + numDeleted);
        }
    }

    public DataTool withHookOptions(Object hookOptions) {
        return new OptionedDataTool(this, hookOptions);
    }

    @Override
    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T item) {
        return createOrUpdate(item, 1);
    }

    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T item, int recurionLevel) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>) item.getClass());

        DBHook<T> hook = (DBHook<T>) getHook(item.getClass());
        if (recurionLevel != NO_RECURSIVE && null != hook) {
            hook.beforeDBWrite(RecursiveDataTool.wrap(this, recurionLevel), item, getCurrentHookOptions());
        }


        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);
        if (recurionLevel != NO_RECURSIVE && null != hook) {
            if (status.isUpdated()) {
                hook.afterUpdated(RecursiveDataTool.wrap(this, recurionLevel), item, getCurrentHookOptions());
            } else {
                hook.afterCreated(RecursiveDataTool.wrap(this, recurionLevel), item, getCurrentHookOptions());
            }
            hook.afterWriteCommon(RecursiveDataTool.wrap(this, recurionLevel), item, getCurrentHookOptions());
        }
        if (null != hook) {
            onHookRelease(hook);
        }
        onItemUpdated(status.isUpdated(), item);
        return status;
    }

    @Override
    public <T> T createIfNotExists(T item) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>) item.getClass());
        T result = dao.createIfNotExists(item);
        return result;
    }

    @Override
    public <T> void createOrUpdateMany(Class<T> clazz, final Collection<T> items) {
        createOrUpdateMany(clazz, items, 1);
    }

    public <T> void createOrUpdateMany(Class<T> clazz, final Collection<T> items, final int recursionLevel) {
        final RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(clazz);

        final DBHook<T> hook = getHook(clazz);
        dao.callBatchTasks(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (T item : items) {

                            if (recursionLevel != NO_RECURSIVE && null != hook) {
                                hook.beforeDBWrite(RecursiveDataTool.wrap(DataTool.this, recursionLevel), item, getCurrentHookOptions());
                            }

                            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(item);

                            if (recursionLevel != NO_RECURSIVE && null != hook) {
                                if (status.isUpdated()) {
                                    hook.afterUpdated(RecursiveDataTool.wrap(DataTool.this, recursionLevel), item, getCurrentHookOptions());
                                } else {
                                    hook.afterCreated(RecursiveDataTool.wrap(DataTool.this, recursionLevel), item, getCurrentHookOptions());
                                }
                                hook.afterWriteCommon(RecursiveDataTool.wrap(DataTool.this, recursionLevel), item, getCurrentHookOptions());
                            }

                            onItemUpdated(status.isUpdated(), item);

                        }
                        if (null != hook) {
                            onHookRelease(hook);
                        }
                        return null;
                    }
                });
    }

    @Override
    public Yielder yielder() {
        return new Yielder() {

            private final SQLiteDatabase sqLiteDatabase = ormHelper.getWritableDatabase();

            @Override
            public boolean yieldTransaction(long sleepAfter) {
                return sqLiteDatabase.yieldIfContendedSafely(sleepAfter);
            }

            @Override
            public boolean inTransaction() {
                return sqLiteDatabase.inTransaction();
            }
        };
    }

    public <T> void bulkUpdate(Updater<T> updater) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao(updater.getItemType());
        dao.update(updater.getUpdate(ormHelper));
    }

    protected abstract <T> void onItemUpdated(boolean updated, T item);

    @Override
    public <T> void refreshData(T item) {
        refreshData(item, NO_RECURSIVE);
    }

    public <T> void refreshData(T item, int recursionLevel) {
        ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass()).refresh(item);

        if (recursionLevel != NO_RECURSIVE) {
            DBHook<T> hook = (DBHook<T>) getHook(item.getClass());
            if (null != hook) {
                hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), item, getCurrentHookOptions());
                onHookRelease(hook);
            }
        }
    }

    @Override
    public <T> void refreshAll(Collection<T> items) {
        refreshAll(items, NO_RECURSIVE);
    }

    public <T> void refreshAll(Collection<T> items, int recursionLevel) {
        RuntimeExceptionDao<T, ?> dao = null;
        DBHook<T> hook = null;
        boolean hookChecked = false;
        for (T item : items) {
            if (null == dao) {
                dao = ormHelper.getRuntimeExceptionDao((Class<T>)item.getClass());
            }

            if (recursionLevel != NO_RECURSIVE && !hookChecked) {
                hookChecked = true;
                hook = (DBHook<T>) getHook(item.getClass());
            }

            dao.refresh(item);

            if (null != hook) {
                hook.fillGapsFromDatabase(RecursiveDataTool.wrap(this, recursionLevel), item, getCurrentHookOptions());
            }
        }
        if (null != hook) {
            onHookRelease(hook);
        }
    }

    @Override
    public <T> boolean exists(T item) {
        RuntimeExceptionDao<T, ?> dao = ormHelper.getRuntimeExceptionDao((Class<T>) item.getClass());
        return idChecker(dao, item);
    }

    private <T, ID> boolean idChecker(RuntimeExceptionDao<T, ID> dao, T item) {
        ID id = dao.extractId(item);
        return dao.idExists(id);
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
            hook.beforeDBWrite(this, item, getCurrentHookOptions());
        }

        dao.update(item);
        if (null != hook) {
            hook.afterUpdated(this, item, getCurrentHookOptions());
            hook.afterWriteCommon(this, item, getCurrentHookOptions());
            onHookRelease(hook);
        }

        onItemUpdated(true, item);
    }

    protected Object getCurrentHookOptions() {
        return null;
    }

    protected <T> DBHook<T> getHook(Class<T> clazz) {
        return null;
    }

}
