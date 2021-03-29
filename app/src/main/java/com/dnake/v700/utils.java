package com.dnake.v700;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@SuppressLint("DefaultLocale")
public class utils {
	public static String getLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
						return inetAddress.getHostAddress().toString();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLocalMac() {
		String mac_s = "";
		try {
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(utils.getLocalIp()));
			if (ne != null) {
				byte[] mac = ne.getHardwareAddress();
				if (mac != null)
					mac_s = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}

	public static void setWifiEnable(boolean enable) {
		if (talk.mContext == null)
			return;

		WifiManager wm = (WifiManager) talk.mContext.getSystemService(Context.WIFI_SERVICE);
		if (wm != null) {
			if (enable) {
				if (!wm.isWifiEnabled())
					wm.setWifiEnabled(true);
			} else {
				if (wm.isWifiEnabled())
					wm.setWifiEnabled(false);
			}
		}
	}

	public static boolean getWifiEnable() {
		if (talk.mContext == null)
			return false;

		WifiManager wm = (WifiManager) talk.mContext.getSystemService(Context.WIFI_SERVICE);
		if (wm != null)
			return wm.isWifiEnabled();
		return false;
	}

	public static Boolean getEthWifi() {
		int val = 0;
		try {
			FileInputStream in = new FileInputStream("/var/etc/eth_wifi");
			byte[] data = new byte[256];
			int ret = in.read(data);
			if (ret > 0) {
				String s = new String();
				char[] d = new char[1];
				for (int i = 0; i < ret; i++) {
					if (data[i] >= '0' && data[i] <= '9') {
						d[0] = (char) data[i];
						s += new String(d);
					} else
						break;
				}
				val = Integer.parseInt(s);
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return (val == 1 ? true : false);
	}

	public static int getLanValue(String url) {
		int val = 0;
		try {
			FileInputStream in = new FileInputStream(url);
			byte[] data = new byte[32];
			int ret = in.read(data);
			if (ret > 0) {
				String s = new String();
				char[] d = new char[1];
				for (int i = 0; i < ret; i++) {
					if (data[i] >= '0' && data[i] <= '9') {
						d[0] = (char) data[i];
						s += new String(d);
					} else
						break;
				}
				val = Integer.parseInt(s);
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return val;
	}

	public static void copyTo(String from, String to) {
		File fin = new File(from);
		if (!fin.exists())
			return;
		if (!fin.isFile())
			return;
		if (!fin.canRead())
			return;

		File fout = new File(to);
		if (!fout.getParentFile().exists())
			fout.getParentFile().mkdirs();
		if (fout.exists())
			fout.delete();

		try {
			FileInputStream is = new FileInputStream(fin);
			FileOutputStream os = new FileOutputStream(fout);

			byte[] bt = new byte[1024];
			int sz;
			while ((sz = is.read(bt)) > 0) {
				os.write(bt, 0, sz);
			}
			is.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class UnlockThread implements Runnable {
		public int mode = 0;
		public int index = 0;

		@Override
		public void run() {
			String s;
			if (mode == 0)
				s = String.format("%d%02d99%02d", sys.talk.building, sys.talk.unit, index);
			else
				s = String.format("%d%03d%02d%02d%02d", index, sys.talk.building, sys.talk.unit, sys.talk.floor, sys.talk.family);
			sCaller.query(s);

			for (int i = 0; i < 10; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (talk.qResult.sip.url != null) {
					dxml p = new dxml();
					dmsg req = new dmsg();

					p.setText("/params/to", talk.qResult.sip.url);
					p.setText("/params/data/params/event_url", "/talk/unlock");
					s = String.format("%d%02d%02d%02d", sys.talk.building, sys.talk.unit, sys.talk.floor, sys.talk.family);
					p.setText("/params/data/params/host", s);
					p.setInt("/params/data/params/build", sys.talk.building);
					p.setInt("/params/data/params/unit", sys.talk.unit);
					p.setInt("/params/data/params/floor", sys.talk.floor);
					p.setInt("/params/data/params/family", sys.talk.family);

					req.to("/talk/sip/sendto", p.toString());
					break;
				}
			}

			uThread = null;
		}
	}

	private static Thread uThread = null;

	// mode: 0:单元门口机 1:小门口机
	// index: 门口机编号
	public static void exUnlock(int mode, int index) {
		if (uThread == null) {
			UnlockThread d = new UnlockThread();
			d.mode = mode;
			d.index = index;
			uThread = new Thread(d);
			uThread.start();
		}
	}
}
