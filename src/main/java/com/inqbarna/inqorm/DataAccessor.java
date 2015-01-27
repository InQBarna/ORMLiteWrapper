package com.inqbarna.inqorm;

import com.j256.ormlite.dao.Dao;

import java.util.Collection;
import java.util.List;

/**
 * Created by David García <david.garcia@inqbarna.com> on 27/1/15.
 */
public interface DataAccessor {
    <T> T findItem(DataTool.Finder<T> finder);

    <T> List<T> findMany(DataTool.Finder<T> finder, List<DataTool.OrderInstrucction> ordering);

    <T> long getCount(DataTool.Finder<T> finder);

    <T> void deleteItems(DataTool.Deleter<T> deleter);

    <T> Dao.CreateOrUpdateStatus createOrUpdate(T item);

    <T> void createOrUpdateMany(Class<T> clazz, Collection<T> items);

    <T> void refreshData(T item);

    <T> void update(T item);
}
