package callback;

import domain.Record;

public interface LogProcessor<T> {
    public void process(Record record, T userData) throws Exception;
}
