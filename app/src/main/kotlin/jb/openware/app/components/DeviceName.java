package in.afi.codekosh.components;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceName {
    private static final String SHARED_PREF_NAME = "device_names";
    private static Context context;

    public static void init(Context context) {
        DeviceName.context = context.getApplicationContext();
    }

    public static Request with(Context context) {
        return new Request(context.getApplicationContext());
    }

    public static String getDeviceName() {
        return getDeviceName(Build.DEVICE, Build.MODEL, capitalize(Build.MODEL));
    }

    public static String getDeviceName(String codename, String fallback) {
        return getDeviceName(codename, codename, fallback);
    }

    public static String getDeviceName(String codename, String model, String fallback) {
        String marketName = getDeviceInfo(context(), codename, model).marketName;
        return marketName == null ? fallback : marketName;
    }

    //@WorkerThread
    public static DeviceInfo getDeviceInfo(Context context) {
        return getDeviceInfo(context.getApplicationContext(), Build.DEVICE, Build.MODEL);
    }

    //@WorkerThread
    public static DeviceInfo getDeviceInfo(Context context, String codename) {
        return getDeviceInfo(context, codename, null);
    }

    //@WorkerThread
    public static DeviceInfo getDeviceInfo(Context context, String codename, String model) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String key = String.format("%s:%s", codename, model);
        String savedJson = prefs.getString(key, null);
        if (savedJson != null) {
            try {
                return new DeviceInfo(new JSONObject(savedJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (DeviceDatabase database = new DeviceDatabase(context)) {
            DeviceInfo info = database.queryToDevice(codename, model);
            if (info != null) {
                JSONObject json = new JSONObject();
                json.put("manufacturer", info.manufacturer);
                json.put("codename", info.codename);
                json.put("model", info.model);
                json.put("market_name", info.marketName);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(key, json.toString());
                editor.apply();
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (codename.equals(Build.DEVICE) && Build.MODEL.equals(model)) {
            return new DeviceInfo(Build.MANUFACTURER, codename, codename, model); // current device
        }

        return new DeviceInfo(null, null, codename, model); // unknown device
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }
        return phrase.toString();
    }

    @SuppressLint("PrivateApi")
    private static Context context() {
        if (context != null) return context;

        // We didn't use to require holding onto the application context so let's cheat a little.
        try {
            return (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception ignored) {
        }

        // Last attempt at hackery
        try {
            return (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null, (Object[]) null);
        } catch (Exception ignored) {
        }

        throw new RuntimeException("DeviceName must be initialized before usage.");
    }

    public interface Callback {
        void onFinished(DeviceInfo info, Exception error);
    }

    public static final class Request {

        final Context context;
        final Handler handler;
        String codename;
        String model;

        private Request(Context ctx) {
            context = ctx;
            handler = new Handler(ctx.getMainLooper());
        }

        public Request setCodename(String codename) {
            this.codename = codename;
            return this;
        }

        public Request setModel(String model) {
            this.model = model;
            return this;
        }

        public void request(Callback callback) {
            if (codename == null && model == null) {
                codename = Build.DEVICE;
                model = Build.MODEL;
            }
            GetDeviceRunnable runnable = new GetDeviceRunnable(callback);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(runnable).start();
            } else {
                runnable.run(); // already running in background thread.
            }
        }

        private final class GetDeviceRunnable implements Runnable {

            final Callback callback;
            DeviceInfo deviceInfo;
            Exception error;

            GetDeviceRunnable(Callback callback) {
                this.callback = callback;
            }

            @Override
            public void run() {
                try {
                    deviceInfo = getDeviceInfo(context, codename, model);
                } catch (Exception e) {
                    error = e;
                }
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onFinished(deviceInfo, error);
                    }
                });
            }
        }
    }

    public static final class DeviceInfo {

        //@Deprecated
        public final String manufacturer;

        public final String marketName;

        public final String codename;

        public final String model;

        public DeviceInfo(String marketName, String codename, String model) {
            this(null, marketName, codename, model);
        }

        public DeviceInfo(String manufacturer, String marketName, String codename, String model) {
            this.manufacturer = manufacturer;
            this.marketName = marketName;
            this.codename = codename;
            this.model = model;
        }

        private DeviceInfo(JSONObject jsonObject) throws JSONException {
            manufacturer = jsonObject.getString("manufacturer");
            marketName = jsonObject.getString("market_name");
            codename = jsonObject.getString("codename");
            model = jsonObject.getString("model");
        }

        public String getName() {
            if (!TextUtils.isEmpty(marketName)) {
                return marketName;
            }
            return capitalize(model);
        }
    }
}
