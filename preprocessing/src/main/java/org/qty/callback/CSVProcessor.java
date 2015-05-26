package org.qty.callback;

public interface CSVProcessor<T> {
    public void process(String[] csv, T userData) throws Exception;
}