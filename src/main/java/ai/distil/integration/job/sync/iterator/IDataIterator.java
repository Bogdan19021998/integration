package ai.distil.integration.job.sync.iterator;

import ai.distil.integration.job.sync.holder.DataSourceDataHolder;

import java.util.Iterator;

public interface IDataIterator<T> extends Iterator<T>, AutoCloseable {

    @Override
    default void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean hasNext();

    @Override
    T next();

    DataSourceDataHolder getDataSource();

}
