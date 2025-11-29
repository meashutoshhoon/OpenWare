package in.afi.codekosh.components;

import android.util.Log;

public class FileLog {
    public static void a(StackTraceElement e) {
        Log.e("java", e.toString());
    }

    public static void b(StackTraceElement e) {
        Log.e("rex", e.toString());
    }

    public static void c(String e) {
        Log.e("hepo", e);
    }

    public static void d(Exception e) {
        Log.e("sda", e.toString());
    }

    public static void e(String toString) {
        Log.e("jaha", toString);
    }

    public static void f(Throwable e) {
        Log.e("reso", e.toString());
    }

    public static void g(Throwable e, boolean b) {
    }
}
