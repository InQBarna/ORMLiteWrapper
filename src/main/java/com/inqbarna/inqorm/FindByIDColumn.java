package com.inqbarna.inqorm;

/**
 * Created by David on 31/10/14.
 */
public class FindByIDColumn<T, ID> extends FinderById<T, ID> {

    private final String columnName;

    public FindByIDColumn(Class<T> clazz, ID id, String columnName) {
        super(clazz, id);
        this.columnName = columnName;
    }

    @Override
    protected String idColumnName() {
        return columnName;
    }
}
