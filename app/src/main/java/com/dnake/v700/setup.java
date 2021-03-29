package com.dnake.v700;

public class setup {
	private static String url = "/dnake/cfg/setup.xml";

	public static final class quick {
		public static int enable = 0;
		public static String url = "sip:911@192.168.12.40";
	}

	public static final class unlock {
		public static final class dtmf {
			public static int enable = 0;
			public static String data = "#";
		}
	}

	public static void load() {
		dxml p = new dxml();
		if (p.load(url)) {
			quick.enable = p.getInt("/sys/quick/enable", 0);
			quick.url = p.getText("/sys/quick/url");

			unlock.dtmf.enable = p.getInt("/sys/unlock/dtmf/enable", 0);
			unlock.dtmf.data = p.getText("/sys/unlock/dtmf/data");
		} else
			save();
	}

	public static void save() {
		dxml p = new dxml();

		p.setInt("/sys/quick/enable", quick.enable);
		p.setText("/sys/quick/url", quick.url);

		p.setInt("/sys/unlock/dtmf/enable", unlock.dtmf.enable);
		p.setText("/sys/unlock/dtmf/data", unlock.dtmf.data);

		p.save(url);
	}
}
