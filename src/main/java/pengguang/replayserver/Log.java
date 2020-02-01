package pengguang.replayserver;

public class Log {
    public static void d(String tag, String str) {
        System.out.println(str);
    }

    public static void d(String str) {
        d("", str);
    }
}
