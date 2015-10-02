package com.inqbarna.inqorm;

import com.j256.ormlite.dao.Dao;

import java.util.Collection;
import java.util.List;

/**
 * Created by David García <david.garcia@inqbarna.com> on 27/1/15.
 */
class RecursiveDataTool implements DataAccessor {
    private DataTool wrapped;

    private int currentRecursiveLevel;

    private RecursiveDataTool() {

    }


    static DataAccessor wrap(DataTool wrapped, int originalRecursion) {
        RecursiveDataTool retVal = new RecursiveDataTool();
        retVal.wrapped = wrapped;
        if (originalRecursion == DataTool.NO_RECURSIVE) {
            throw new IllegalArgumentException("This method should've never been called, no recursion was enabled, correct it!");
        }

        retVal.currentRecursiveLevel = originalRecursion == DataTool.FULL_RECURSIVE ? originalRecursion : originalRecursion - 1;
        return retVal;
    }

    @Override
    public <T> List<T> findMany(DataTool.Finder<T> finder, List<DataTool.OrderInstrucction> ordering) {
        return wrapped.findMany(finder, ordering, currentRecursiveLevel);
    }

    @Override
    public <T, ID> T getById(Class<T> clazz, ID id) {
        return wrapped.getById(clazz, id, currentRecursiveLevel);
    }

    @Override
    public <T, ID> ID getObjectID(T item) {
        return wrapped.getObjectID(item);
    }

    @Override
    public <T> T findItem(DataTool.Finder<T> finder) {
        return wrapped.findItem(finder, currentRecursiveLevel);
    }

    @Override
    public <T> long getCount(DataTool.Finder<T> finder) {
        return wrapped.getCount(finder);
    }

    @Override
    public <T> void deleteItems(DataTool.Deleter<T> deleter) {
        wrapped.deleteItems(deleter);
    }

    @Override
    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T item) {
        return wrapped.createOrUpdate(item, currentRecursiveLevel);
    }

    @Override
    public <T> T createIfNotExists(T item) {
        return wrapped.createIfNotExists(item);
    }

    @Override
    public <T> void createOrUpdateMany(Class<T> clazz, Collection<T> items) {
        wrapped.createOrUpdateMany(clazz, items, currentRecursiveLevel);
    }

    @Override
    public <T> void refreshData(T item) {
        wrapped.refreshData(item, currentRecursiveLevel);
    }

    @Override
    public <T> void refreshAll(Collection<T> items) {
        wrapped.refreshAll(items, currentRecursiveLevel);
    }

    @Override
    public <T> boolean exists(T item) {
        return wrapped.exists(item);
    }

    @Override
    public <T> void update(T item) {
        wrapped.update(item);
    }

    @Override
    public Transaction beginTransaction() {
        return wrapped.beginTransaction();
    }
}
