public class Launcher {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "JOINVIEW".equals(args[0])) {
            JoinViewOrder.main(args);
        } else {
            Application.main(args);
        }
    }
}
