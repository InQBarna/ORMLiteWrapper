package com.inqbarna.inqorm;

/**
 * Created by David on 31/10/14.
 */
public class FindByTargetColumn<T, ID> extends FinderByColumn<T, ID> {

    private final String columnName;

    public FindByTargetColumn(Class<T> clazz, ID id, String columnName) {
        super(clazz, id);
        this.columnName = columnName;
    }

    @Override
    protected String targetColumnName() {
        return columnName;
    }
}
