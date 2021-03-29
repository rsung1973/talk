package com.dnake.logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.talk;

public class TalkLogger {
	public static List<data> logger = new LinkedList<data>();
	public static String dir = "/dnake/data/talk";
	public static String url = dir+"/logger.xml";

	public static int CALL_IN = 1;
	public static int CALL_OUT = 2;
	public static int CALL_MISS = 3;
	public static int CALL_MSG = 4;

	public static int MAX = 60;

	public static class data {
		public String host;
		public long start;
		public long duration;
		public int type; // 1 呼入 2 呼出 3 未接 4 留言
		public int read; // 0:未读 1:已读
	}

	public static void load() {
		logger.clear();

		File f = new File(dir);
		if (f != null && !f.exists())
			f.mkdir();

		f = new File(url);
		if (f != null && !f.exists())
			return;

		dxml p = new dxml();
		if (p.load(url)) {
			for (int i = 0; i < MAX; i++) {
				String s = "/logger/d" + i;
				String host = p.getText(s + "/host");
				if (host != null) {
					data d = new data();
					d.host = host;
					d.start = Long.parseLong(p.getText(s + "/start"));
					d.duration = Long.parseLong(p.getText(s + "/duration"));
					d.type = p.getInt(s + "/type", 0);
					d.read = p.getInt(s + "/read", 1);
					logger.add(d);
				}
			}
		}
	}

	public static void insert(String host, long start, long duration, int type) {
		// duration: 通话时长 type: 1 呼入 2 呼出 3 未接 4 留言

		if (duration > 4*60*60)
			duration = 0;

		data d = new data();
		d.host = host;
		d.start = start;
		d.duration = duration;
		d.type = type;
		d.read = 0;
		logger.add(0, d);

		if (logger.size() > MAX)
			logger.remove(logger.size() - 1);

		save();
	}

	public static void remove(long start) {
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			if (d.start == start) {
				logger.remove(i);
				JpegLogger.remove(start);
				break;
			}
		}
		save();
	}

	public static void save() {
		dxml p = new dxml();
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			String s = "/logger/d" + i;
			p.setText(s + "/host", d.host);
			p.setText(s + "/start", String.valueOf(d.start));
			p.setText(s + "/duration", String.valueOf(d.duration));
			p.setInt(s + "/type", d.type);
			p.setInt(s + "/read", d.read);
		}
		p.save(url);

		dmsg req = new dmsg();
		req.to("/upgrade/sync", null);

		talk.broadcast();
	}

	public static void setRead(int n) {
		data d = logger.get(n);
		d.read = 1;
		logger.set(n, d);
	}

	public static int missed() {
		int n = 0;
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			if (d.type == TalkLogger.CALL_MISS && d.read == 0)
				n++;
		}
		return n;
	}

	public static int leaved() {
		int n = 0;
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			if (d.type == TalkLogger.CALL_MSG && d.read == 0)
				n++;
		}
		return n;
	}

	public static String queryName(String phone, Context context) {
		ContentResolver cr = context.getContentResolver();
		if (cr == null)
			return null;

		Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME }, ContactsContract.CommonDataKinds.Phone.NUMBER
				+ "='" + phone + "'", null, null);

		String s = null;
		if (c != null) {
			if (c.moveToFirst())
				s = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			c.close();
		}

		return s;
	}
}
