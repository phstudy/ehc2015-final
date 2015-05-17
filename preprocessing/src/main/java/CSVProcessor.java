public interface CSVProcessor<T> {
    public void process(String[] csv, T userData) throws Exception;
}