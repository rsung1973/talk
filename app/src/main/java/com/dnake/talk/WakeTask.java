package com.dnake.talk;

import com.dnake.v700.login;
import com.dnake.v700.utils;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@SuppressWarnings("deprecation")
@SuppressLint("Wakelock")
public class WakeTask {
	private static long wTs = System.currentTimeMillis();
	private static int timeout = 30*1000;

	private static WakeLock wLock = null;
	private static PowerManager pm = null;
	private static KeyguardManager mKM = null;
	private static KeyguardLock kLock = null;

	private static Thread wThread = null;

	public static void onCreate(Context ctx) {
		wTs = System.currentTimeMillis();
		pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
		mKM = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
		kLock = mKM.newKeyguardLock("CQC");

		ProcessThread pt = new ProcessThread();
		wThread = new Thread(pt);
		wThread.start();
	}

	public static void onDestroy() {
		if (wLock != null) {
			kLock.reenableKeyguard();
			wLock.release();
			wLock = null;
		}
	}

	public static void acquire() {
		if (pm == null)
			return;

		if (Math.abs(System.currentTimeMillis()-wTs) >= 2*60*1000)
			WakeTask.release();

		synchronized (wThread) {
			if (wLock == null) {
				wLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "v900");
				wLock.acquire();
				kLock.disableKeyguard();
			}
		}
		login.refresh();
		wTs = System.currentTimeMillis();
	}

	public static void release() {
		synchronized (wThread) {
			if (wLock != null) {
				kLock.reenableKeyguard();
				wLock.release();
				wLock = null;
			}
		}
	}

	public static Boolean timeout() {
		if (Math.abs(System.currentTimeMillis()-wTs) < timeout)
			return false;
		return true;
	}

	public static Boolean isScreenOn() {
		if (pm == null)
			return false;
		return pm.isScreenOn();
	}

	public static class ProcessThread implements Runnable {
		private long wifi = 0;

		//WIFI 有线自动切换程序，适用于可拆卸带电池分机
		public void WifiEthSwitch() {
			if (Math.abs(System.currentTimeMillis()-wifi) >= 10*1000) {
				wifi = System.currentTimeMillis();
				if (!utils.getEthWifi()) {
					int mtu = utils.getLanValue("/sys/class/net/eth0/mtu");
					if (mtu > 500) {
						if (utils.getWifiEnable())
							utils.setWifiEnable(false);
					} else {
						if (!utils.getWifiEnable())
							utils.setWifiEnable(true);
					}
				}
			}
		}

		@Override
		public void run() {
			while(true) {
				if ((!pm.isScreenOn() && wLock != null) || (pm.isScreenOn() && Math.abs(System.currentTimeMillis()-wTs) >= 60*1000))
					WakeTask.release();

				if (login.ok && login.timeout())
					login.settings(0);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
