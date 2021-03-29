package com.dnake.v700;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.annotation.SuppressLint;
import com.dnake.v700.setup.unlock;

@SuppressLint("DefaultLocale")
public final class sys {

	public static int version_major = 1; // 主版本
	public static int version_minor = 1; // 次版本
	public static int version_minor2 = 5; // 次版本2

	public static String version_date = "20180201"; // 日期
	public static String version_ex = "(std)"; // 扩展标注

	public static float scaled = 1.0f;

	public static int mute = 0;

	public static String url = "/dnake/cfg/sys.xml";
	private static String url_b = "/dnake/data/sys.xml";

	public static final class admin {
		public static String passwd = new String("123456");
	}

	public static final class talk {
		public static int building = 1;
		public static int unit = 1;
		public static int floor = 11;
		public static int family = 11;

		public static int dcode = 0;
		public static String sync = new String("123456");

		public static String server = new String("192.168.12.40");
		public static String passwd = new String("123456");

		public static int timeout = 300;

		public static int auto_msg = 0;

		public static int auto_answer = 0;

		public static int onu_arp = 0;
	}

	public static final class sip {
		public static int enable = 0;

		public static String proxy = new String("sip:192.168.12.40");
		public static String realm = new String("192.168.12.40");
		public static String user = new String("100");
		public static String passwd = new String("123456");
		public static String outbound = new String("sip:");

		public static final class stun {
			public static String ip = new String("192.168.12.40");
			public static int port = 5060;
		}

		public static int nVideo = 0;
		public static int host2id = 1;
	}

	public static final class volume {
		public static int talk = 0;
		public static final int MAX = 5;
	}

	public static final class link {
		public static int enable[] = new int[4];
		public static String url[] = new String[4];
	}

	public static String id() {
		String s = String.format("%d%02d%02d%02d", talk.building, talk.unit, talk.floor, talk.family);
		return s;
	}

	public static void id(String s) {
		String b, u, f, r;
		int sz = s.length();
		r = s.substring(sz-2, sz);
		f = s.substring(sz-4, sz-4+2);
		u = s.substring(sz-6, sz-6+2);
		b = s.substring(0, sz-6);

		talk.building = Integer.parseInt(b);
		talk.unit = Integer.parseInt(u);
		talk.floor = Integer.parseInt(f);
		talk.family = Integer.parseInt(r);
	}

	public static void load() {
		int s = 100000 + (new Random(System.currentTimeMillis())).nextInt(899999);
		talk.sync = String.valueOf(s);

		dxml p = new dxml();
		boolean result = p.load(url);
		if (!result)
			result = p.load(url_b);

		if (result) {
			admin.passwd = p.getText("/sys/admin/passwd", admin.passwd);

			talk.building = p.getInt("/sys/talk/building", 1);
			talk.unit = p.getInt("/sys/talk/unit", 1);
			talk.floor = p.getInt("/sys/talk/floor", 1);
			talk.family = p.getInt("/sys/talk/family", 1);
			talk.dcode = p.getInt("/sys/talk/dcode", 0);
			talk.sync = p.getText("/sys/talk/sync", talk.sync);
			talk.server = p.getText("/sys/talk/server", talk.server);
			talk.passwd = p.getText("/sys/talk/passwd", talk.passwd);
			talk.timeout = p.getInt("/sys/talk/timeout", 300);
			talk.auto_msg = p.getInt("/sys/talk/auto_msg", 0);
			talk.auto_answer = p.getInt("/sys/talk/auto_answer", 0);
			talk.onu_arp = p.getInt("/sys/talk/onu_arp", 0);

			sip.enable = p.getInt("/sys/sip/ex_enable", 0);
			sip.proxy = p.getText("/sys/sip/proxy", sip.proxy);
			sip.realm = p.getText("/sys/sip/realm", sip.realm);
			sip.user = p.getText("/sys/sip/ex_user", sip.user);
			sip.passwd = p.getText("/sys/sip/passwd", sip.passwd);

			sip.nVideo = p.getInt("/sys/sip/nvideo", 0);
			sip.host2id = p.getInt("/sys/sip/host2id", 0);

			sip.stun.ip = p.getText("/sys/stun/ip", sip.stun.ip);
			sip.stun.port = p.getInt("/sys/stun/port", 5060);

			volume.talk = p.getInt("/sys/volume/talk", 0);

			for (int i = 0; i < 4; i++) {
				link.enable[i] = p.getInt("/sys/sip/ex_slv" + i, 0);
				link.url[i] = p.getText("/sys/sip/ex_slv_url" + i);
			}
		} else
			save();
		sys.httpPasswd();
	}

	public static void load(String xml) {
		dxml p = new dxml();
		if (p.parse(xml)) {
			sys.admin.passwd = p.getText("/event/ex_passwd", admin.passwd);
			sys.talk.server = p.getText("/event/talk/server", talk.server);
			sys.talk.auto_answer = p.getInt("/event/ex_setup/auto_pickup", talk.auto_answer);

			sys.sip.proxy = p.getText("/event/sip/proxy", sip.proxy);
			sys.sip.realm = p.getText("/event/sip/realm", sip.realm);
			sys.sip.stun.ip = p.getText("/event/stun/ip", sip.stun.ip);
			sys.sip.stun.port = p.getInt("/event/stun/port", sip.stun.port);
			sys.save();

			setup.unlock.dtmf.enable = p.getInt("/event/unlock/dtmf/enable", unlock.dtmf.enable);
			setup.unlock.dtmf.data = p.getText("/event/unlock/dtmf/s", unlock.dtmf.data);
			setup.quick.enable = p.getInt("/event/ex_setup/quick/enable", setup.quick.enable);
			setup.quick.url = p.getText("/event/ex_setup/quick/url", setup.quick.url);
			setup.save();
		}
	}

	public static void save() {
		dxml p = new dxml();

		p.setText("/sys/admin/passwd", admin.passwd);

		p.setInt("/sys/talk/building", talk.building);
		p.setInt("/sys/talk/unit", talk.unit);
		p.setInt("/sys/talk/floor", talk.floor);
		p.setInt("/sys/talk/family", talk.family);
		p.setInt("/sys/talk/dcode", talk.dcode);
		p.setText("/sys/talk/sync", talk.sync);
		p.setText("/sys/talk/server", talk.server);
		p.setText("/sys/talk/passwd", talk.passwd);
		p.setInt("/sys/talk/timeout", talk.timeout);
		p.setInt("/sys/talk/auto_msg", talk.auto_msg);
		p.setInt("/sys/talk/auto_answer", talk.auto_answer);
		p.setInt("/sys/talk/onu_arp", talk.onu_arp);

		p.setInt("/sys/sip/ex_enable", sip.enable);
		p.setText("/sys/sip/proxy", sip.proxy);
		p.setText("/sys/sip/realm", sip.realm);
		p.setText("/sys/sip/ex_user", sip.user);
		p.setText("/sys/sip/passwd", sip.passwd);

		p.setInt("/sys/sip/nvideo", sip.nVideo);
		p.setInt("/sys/sip/host2id", sip.host2id);

		p.setText("/sys/stun/ip", sip.stun.ip);
		p.setInt("/sys/stun/port", sip.stun.port);

		p.setInt("/sys/volume/talk", volume.talk);

		for (int i = 0; i < 4; i++) {
			p.setInt("/sys/sip/ex_slv" + i, link.enable[i]);
			p.setText("/sys/sip/ex_slv_url" + i, link.url[i]);
		}

		p.save(url);
		p.save(url_b);

		dmsg req = new dmsg();
		req.to("/talk/setid", null);
		req.to("/control/set_id", null);

		sys.httpPasswd();
	}

	public static void httpPasswd() {
		try {
			FileOutputStream out = new FileOutputStream("/var/etc/httppasswd");
			String s = "admin:" + sys.admin.passwd + "\n";
			out.write(s.getBytes());
			s = "special:" + sys.admin.passwd + "\n";
			out.write(s.getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int sLimit = -1;

	public static int limit() {
		if (sLimit != -1)
			return sLimit;

		int limit = 0;
		try {
			FileInputStream in = new FileInputStream("/dnake/bin/limit");
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
				limit = Integer.parseInt(s);
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		sLimit = limit;
		return limit;
	}
}
