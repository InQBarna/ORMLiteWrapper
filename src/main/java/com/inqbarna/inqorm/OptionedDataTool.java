package com.inqbarna.inqorm;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 2/10/15
 */
class OptionedDataTool extends DataTool {
    private DataTool sourceDataTool;
    private Object hookOptions;
    public OptionedDataTool(DataTool sourceDataTool, Object hookOptions) {
        super(sourceDataTool.ormHelper);
        this.sourceDataTool = sourceDataTool;
        this.hookOptions = hookOptions;
    }

    @Override
    protected Object getCurrentHookOptions() {
        return hookOptions;
    }

    @Override
    protected <T> DBHook<T> getHook(Class<T> clazz) {
        return sourceDataTool.getHook(clazz);
    }

    @Override
    protected <T> void onItemUpdated(boolean updated, T item) {
        sourceDataTool.onItemUpdated(updated, item);
    }
}
