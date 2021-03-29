package com.dnake.v700;

public class ioctl {
	public static void hooter(int onoff) {
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/onoff", onoff);
		req.to("/control/hooter", p.toString());
	}

	public static void led(int led, int onoff) {
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/led", led);
		p.setInt("/params/onoff", onoff);
		req.to("/control/led", p.toString());
	}

	public static void handset(int mode) { // 0: 手柄模式  1: 外放模式
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/mode", mode);
		req.to("/control/audio", p.toString());
	}
}
