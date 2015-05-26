package org.qty.callback;

import org.phstudy.ehc.v1.domain.Record;

public interface LogProcessor<T> {
    public void process(Record record, T userData) throws Exception;
}
