package com.dnake.widget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.storage.StorageManager;

@SuppressLint("SdCardPath")
public class Storage {
	public static String sdcard = "/mnt/sdcard";
	public static String extsd = "/mnt/extsd";

	public static void load() {
		if (VERSION.SDK_INT >= 19) { //android4.4 SD卡挂载点特殊指定
			extsd = "/storage/extsd";
		}
	}

	public static Boolean isMount(Context ctx, String point) {
		StorageManager sm = (StorageManager)ctx.getSystemService(Context.STORAGE_SERVICE);
		try {
			Method mGetState = sm.getClass().getMethod("getVolumeState", String.class);
	        try {
				String s = (String)mGetState.invoke(sm, point);
				if (s.equals("mounted"))
					return true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}
}
