package com.dnake.logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import com.dnake.v700.dxml;

@SuppressLint("SdCardPath")
public class JpegLogger {
	public static List<data> logger = new LinkedList<data>();
	public static String url = "/dnake/data/jpeg/logger.xml";

	public static class data {
		public long start;
		public String url;
	}

	public static void load() {
		logger.clear();

		File f = new File("/dnake/data/jpeg");
		if (f != null && !f.exists())
			f.mkdir();

		f = new File(url);
		if (f != null && !f.exists())
			return;

		dxml p = new dxml();
		if (p.load(url)) {
			for (int i = 0; i < 64; i++) {
				String s = "/logger/d" + i;
				String start = p.getText(s + "/start");
				if (start != null) {
					data d = new data();
					d.start = Long.parseLong(start);
					d.url = p.getText(s + "/url");
					if (d.url != null) {
						f = new File(d.url);
						if (f.exists())
							logger.add(d);
					}
				}
			}
		}
	}

	public static void insert(long start, String url) {
		if (url == null)
			return;

		data d = new data();
		d.start = start;
		d.url = url;
		logger.add(0, d);

		if (logger.size() >= 64) {
			d = logger.get(logger.size() - 1);
			File f = new File(d.url);
			if (f != null && f.isFile() && f.exists())
				f.delete();
			logger.remove(logger.size() - 1);
		}

		save();
	}

	public static void remove(long start) {
		boolean ok = false;

		for (int i = (logger.size() - 1); i >= 0; i--) {
			data d = logger.get(i);
			if (d.start == start) {
				File f = new File(d.url);
				if (f != null && f.isFile() && f.exists())
					f.delete();
				logger.remove(i);

				ok = true;
			}
		}

		if (ok)
			save();
	}

	public static void save() {
		dxml p = new dxml();
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			String s = "/logger/d" + i;
			p.setText(s + "/start", String.valueOf(d.start));
			p.setText(s + "/url", d.url);
		}
		p.save(url);
	}

	public static Boolean have(long start) {
		for (int i = 0; i < logger.size(); i++) {
			data d = logger.get(i);
			if (d.start == start)
				return true;
		}
		return false;
	}
}
