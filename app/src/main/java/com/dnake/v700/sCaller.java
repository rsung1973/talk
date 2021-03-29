package com.dnake.v700;

import com.dnake.talk.MainActivity;

public class sCaller {
	public static int NONE = 0;
	public static int QUERY = 1;
	public static int CALL = 2;

	public static int running = 0;
	public static long ts;
	public static String id;

	public static void query(String id) {
		talk.qResult.sip.url = null;
		talk.qResult.d600.ip = null;
		talk.qResult.d600.host = null;

		sCaller.id = id;
		running = QUERY;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/id", id);
		req.to("/talk/sip/query", p.toString());
	}

	public static void start(String url) {

		MainActivity.invokeSipCall(url);

		talk.qResult.result = 0;
		talk.qResult.sip.url = null;

		running = CALL;
		ts = System.currentTimeMillis();

		int n = url.indexOf('@');
		if (n > 0)
			id = url.substring(4, n);

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		req.to("/talk/sip/call", p.toString());
	}

	public static void m700(String url) {

		MainActivity.invokeSipCall(url);

		talk.qResult.result = 0;
		talk.qResult.sip.url = null;

		running = CALL;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		p.setInt("/params/type", 1);
		req.to("/talk/sip/call", p.toString());
	}

	public static void q600(String id) {
		talk.qResult.d600.ip = null;
		talk.qResult.d600.host = null;

		running = QUERY;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", id);
		req.to("/talk/device/query", p.toString());
	}

	public static void m600(String host, String ip) {
		talk.qResult.result = 0;
		talk.qResult.d600.ip = null;
		talk.qResult.d600.host = null;

		sCaller.id = host;
		running = CALL;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", host);
		p.setText("/params/ip", ip);
		req.to("/talk/monitor", p.toString());
	}

	public static void s600(String host, String ip) {
		talk.qResult.result = 0;
		talk.qResult.d600.ip = null;
		talk.qResult.d600.host = null;

		sCaller.id = host;
		running = CALL;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", host);
		p.setText("/params/ip", ip);
		req.to("/talk/call", p.toString());
	}

	public static void stop() {
		running = NONE;
	}

	public static long timeout() {
		return Math.abs(System.currentTimeMillis()-ts);
	}

	public static void unlock() {
		dmsg req = new dmsg();
		req.to("/talk/open", null);
		if (setup.unlock.dtmf.enable != 0) {
			dxml p = new dxml();
			p.setText("/params/dtmf", setup.unlock.dtmf.data);
			req.to("/talk/send_dtmf", p.toString());
		}
	}

	public static class logger {
		public static int ANSWER = 0;
		public static int FAILED = 1;
		public static int UNLOCK = 2;
		public static int CALL = 3;
		public static int END = 4;
	}

	//mode 0:已接听  1:未接听  2:开锁  3:呼叫  4:结束
	public static void logger(int mode) {
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/event_url", "/msg/talk/logger");
		p.setText("/params/to", id);
		p.setInt("/params/mode", mode);
		req.to("/talk/center/to", p.toString()); //700协议呼叫日志
	}
}
