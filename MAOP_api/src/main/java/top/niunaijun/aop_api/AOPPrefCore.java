package top.niunaijun.aop_api;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AOPPrefCore {

    public static Map<?, ?> prefAll(String name) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getAll();
    }

    public static boolean prefBoolean(String name, String field, boolean def) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(field, def);
    }

    public static float prefFloat(String name, String field, float def) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(field, def);
    }

    public static int prefInt(String name, String field, int def) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(field, def);
    }

    public static long prefLong(String name, String field, long def) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(field, def);
    }

    public static String prefString(String name, String field, String def) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(field, def);
    }

    public static Set<String> prefStringSet(String name, String field) {
        if (AOPCore.getContext() == null)
            throw new ExceptionInInitializerError("Please call @MAOPInit before using");

        SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(field, new HashSet<String>());
    }

    public static Object getStorage(String name, String clazz) {
        try {
            Class<?> cla = Class.forName(clazz);
            if (TextUtils.isEmpty(name)) {
                name = cla.getName();
            }
            String storage = prefString("storage", name, "");
            return new Gson().fromJson(storage, cla);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setStorage(Object obj, String name, String clazz) {
        try {
            Class<?> cla = Class.forName(clazz);
            if (TextUtils.isEmpty(name)) {
                name = cla.getName();
            }
            SharedPreferences sharedPreferences = AOPCore.getContext().getSharedPreferences("storage", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(name, new Gson().toJson(obj)).apply();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
