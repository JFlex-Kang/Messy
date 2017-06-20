package devicewills.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import devicewills.objects.ContactInfo;

/**
 * Created by 9929k on 2017-04-12.
 */

public class PreferencesUtil {

    public static void setPreferences(Context context, String key, String value) {
        SharedPreferences p = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getPreferences(Context context, String key) {
        SharedPreferences p = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        p = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        return p.getString(key, "");
    }

    public static void delPreferences(Context context, String key) {
        SharedPreferences p = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.clear();
        editor.commit();
    }

    public static void setIntPreferences(Context context, String key, int value) {
        SharedPreferences p = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntPreferences(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        int myIntValue = sp.getInt(key, -1);
        return myIntValue;
    }

    public static void setBoolPreferences(Context context, String key, Boolean val) {
        SharedPreferences p = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static boolean getBoolPreferneces(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        boolean myBoolValue = sp.getBoolean(key, false);
        return myBoolValue;
    }

    public static void saveArrayListPreferences(Context context, List<ContactInfo> list) {
        SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(list);

        editor.putString("arr_contacts", jsonFavorites);

        editor.commit();
    }

    public static void addArrPreference(Context context, ContactInfo info) {
        List<ContactInfo> list = getArrayListPreferendes(context);
        if (list == null) {
            list = new ArrayList<ContactInfo>();
        }
        list.add(info);
        saveArrayListPreferences(context, list);
    }

    public static ArrayList<ContactInfo> getArrayListPreferendes(Context context) {
        SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        List<ContactInfo> list;

        if (sp.contains("arr_contacts")) {
            String json = sp.getString("arr_contacts", null);
            Gson gson = new Gson();
            ContactInfo[] items = gson.fromJson(json, ContactInfo[].class);

            list = Arrays.asList(items);
            list = new ArrayList<ContactInfo>(list);
        } else {
            return null;
        }
        return (ArrayList<ContactInfo>) list;
    }

    public static void delArrPreferences(Context context) {
        SharedPreferences p = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.remove("arr_contacts");
        editor.commit();
    }

}
