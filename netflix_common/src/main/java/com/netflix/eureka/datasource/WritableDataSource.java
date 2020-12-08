package com.netflix.eureka.datasource;

public interface WritableDataSource<T> {
	/**
     * Write the {@code value} to the data source.
     *
     * @param value value to write
     * @throws Exception IO or other error occurs
     */
    void write(T value) throws Exception;

    /**
     * Close the data source.
     *
     * @throws Exception IO or other error occurs
     */
    void close() throws Exception;
}
