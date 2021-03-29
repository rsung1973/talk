package com.dnake.v700;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;

import com.dnake.logger.TalkLogger;
import com.dnake.talk.CallLabel;
import com.dnake.talk.LoggerLabel;
import com.dnake.talk.MonitorLabel;
import com.dnake.talk.TalkLabel;

public class devent {
	private static List<devent> elist = null;
	public static Boolean boot = false;

	public String url;

	public devent(String url) {
		this.url = url;
	}

	public void process(String xml) {
	}

	public static void event(String url, String xml) {
		Boolean err = true;
		if (boot && elist != null) {
			devent e;

			Iterator<devent> it = elist.iterator();
			while (it.hasNext()) {
				e = it.next();
				if (url.equals(e.url)) {
					e.process(xml);
					err = false;
					break;
				}
			}
		}
		if (err)
			dmsg.ack(480, null);
	}

	public static void setup() {
		elist = new LinkedList<devent>();

		devent de;
		de = new devent("/ui/run") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/version") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				String v = String.valueOf(sys.version_major) + "." + sys.version_minor + "." + sys.version_minor2;
				v = v + " " + sys.version_date + " " + sys.version_ex;
				p.setText("/params/version", v);
				p.setInt("/params/proxy", talk.qResult.sip.proxy);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/start") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				String phoneNo = p.getText("/params/host");
				if(!LoggerLabel.isBlockedNumber(phoneNo)) {
                    TalkLabel.mHost = phoneNo;  //p.getText("/params/host");
                    TalkLabel.mTalking = false;
                    TalkLabel.mType = TalkLogger.CALL_MISS;
                    talk.talk_start();
                }
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/stop") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				talk.talk_stop();
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/play") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				talk.talk_play();
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/busy") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				TalkLabel.mBusyHost = p.getText("/params/host");
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/host2id") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				TalkLabel.host2id(p);
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/snapshot") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				String url = p.getText("/params/url");
				if (TalkLabel.mContext != null && url != null)
					TalkLabel.mContext.snapshot(url);
			}
		};
		elist.add(de);

		de = new devent("/ui/device/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (talk.qResult.d600.ip == null) {
					dxml p = new dxml();
					p.parse(body);
					talk.qResult.d600.host = p.getText("/params/name");
					talk.qResult.d600.ip = p.getText("/params/ip");
				}
				dmsg req = new dmsg();
				req.to("/smart/device/query", body);
				req.to("/apps/device/query", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/monitor/start") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				// 监视600门口机无ringing和状态码
				talk.qResult.result = 180;

				MonitorLabel.start();
			}
		};
		elist.add(de);

		de = new devent("/ui/monitor/stop") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				MonitorLabel.stop();
			}
		};
		elist.add(de);

		de = new devent("/ui/slave/device") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dmsg req = new dmsg();
				req.to("/security/slave/device", body);
				req.to("/smart/slave/device", body);

				dxml p = new dxml();
				p.parse(body);
				talk.qResult.slaves.load(p);
			}
		};
		elist.add(de);

		de = new devent("/ui/slaves") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dmsg req = new dmsg();
				req.to("/security/slaves", body);
				req.to("/smart/slaves", body);

				dxml p = new dxml();
				p.parse(body);
				talk.qResult.slaves.load(p);
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/register") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (talk.qResult != null) {
					dxml p = new dxml();
					p.parse(body);
					talk.qResult.sip.proxy = p.getInt("/params/register", 0);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (talk.qResult.sip.url == null) {
					dxml p = new dxml();
					p.parse(body);
					talk.qResult.sip.url = new String(p.getText("/params/url"));
				}

				dmsg req = new dmsg();
				req.to("/smart/sip/query", body);
				req.to("/apps/sip/query", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				talk.qResult.result = p.getInt("/params/result", 0);
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/ringing") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				TalkLabel.mHost = p.getText("/params/host");
				TalkLabel.mTalking = true;
				TalkLabel.mType = TalkLogger.CALL_OUT;
				talk.talk_start();
			}
		};
		elist.add(de);

		de = new devent("/ui/touch/event") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				if (p.getInt("/params/data", -1) != -1) {
					talk.touch();
					if (p.getInt("/params/apk", 0) != 1)
						talk.tBroadcast();
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/key/answer") { // 非可视副机专用事件
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				int onoff = p.getInt("/params/onoff", 0);
				if (onoff == 0) {
					if (TalkLabel.mTalking && TalkLabel.mUsbMode)
						talk.talk_hungup();
					else {
						// 呼叫界面挂机，直接停止呼叫
						CallLabel.bStop = true;
					}
				} else {
					if (!TalkLabel.mTalking) {
						talk.talk_answer();
						TalkLabel.mUsbMode = true;
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/key/talk") { // 非可视副机专用事件
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				if (p.getInt("/params/onoff", 0) == 1 && TalkLabel.mContext == null) {
					talk.touch();
					CallLabel.bAuto = true;
					TalkLabel.mUsbMode = true;
					if (CallLabel.mContext == null) {
						Intent it = new Intent(talk.mContext, CallLabel.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						talk.mContext.startActivity(it);
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/key/open") { // 非可视副机专用事件
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				if (p.getInt("/params/onoff", 0) == 1) {
					dmsg req = new dmsg();
					req.to("/talk/open", null);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/key/mcu") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				int key = p.getInt("/params/key", 0);
				int onoff = p.getInt("/params/onoff", 0);

				System.out.println("key: " + key + " " + onoff);

				if (key < 100 && onoff == 0) {
					talk.press();
					if (key == 1) { // 接听挂断键
						if (TalkLabel.mTalking)
							talk.talk_hungup();
						else
							talk.talk_answer();
					} else if (key == 2) { // 暂时未定
					} else if (key == 3) { // 呼叫键
						if (TalkLabel.mContext == null && MonitorLabel.mContext == null) {
							if (CallLabel.mContext == null) {
								CallLabel.bAuto = true;
								Intent it = new Intent(talk.mContext, CallLabel.class);
								it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								talk.mContext.startActivity(it);
							} else
								CallLabel.bStop = true;
						}
					} else if (key == 4) { // 监视
						if (CallLabel.mContext == null && TalkLabel.mContext == null) {
							if (MonitorLabel.mContext == null) {
								Intent it = new Intent(talk.mContext, MonitorLabel.class);
								it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								talk.mContext.startActivity(it);
							} else
								MonitorLabel.bStop = true;
						}
					} else if (key == 5) { // 开锁键
						sCaller.unlock();
					}
				} else {
					if (key == 200) { // 手柄
						talk.mHandset = onoff;
						talk.touch();

						if (onoff == 0) { //挂机
							ioctl.handset(1);
							talk.talk_hungup();
						} else { //提机
							ioctl.handset(0);
							if (TalkLabel.mTalking == false)
								talk.talk_answer();
						}
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/broadcast/data") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				String s = p.getText("/event/broadcast_url");
				int b = p.getInt("/event/build", 0);
				int u = p.getInt("/event/unit", 0);
				int f = p.getInt("/event/floor", 0);
				int r = p.getInt("/event/family", 0);

				if (s.equals("/security/setup")) {
					dmsg req = new dmsg();
					req.to("/security/broadcast/data", body);
				} else {
					if (b == sys.talk.building && u == sys.talk.unit && f == sys.talk.floor && r == sys.talk.family) {
						if (s.equals("/access/auth/card")) {
							if (talk.mContext != null) {
								Intent it = new Intent("com.dnake.broadcast");
								it.putExtra("event", "com.dnake.eHome.sync");
								it.putExtra("card", p.getText("/event/card"));
								talk.mContext.sendBroadcast(it);
							}
						} else if (s.equals("/access/auth/add")) {
							if (talk.mContext != null) {
								Intent it = new Intent("com.dnake.broadcast");
								it.putExtra("event", "com.dnake.eHome.add");
								it.putExtra("card", p.getText("/event/card"));
								talk.mContext.sendBroadcast(it);
							}
						}
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/broadcast/settings") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				sys.load(body);
			}
		};
		elist.add(de);

		de = new devent("/ui/ipwatchd") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				int result = p.getInt("/params/result", 0);
				String ip = p.getText("/params/ip");
				String mac = p.getText("/params/mac");
				talk.ipMacErr(result, ip, mac);
			}
		};
		elist.add(de);

		de = new devent("/ui/web/voip/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();

				p.setText("/params/proxy", sys.sip.proxy);
				p.setText("/params/realm", sys.sip.realm);
				p.setText("/params/passwd", sys.sip.passwd);
				p.setInt("/params/nVideo", sys.sip.nVideo);
				p.setText("/params/stun_ip", sys.sip.stun.ip);
				p.setInt("/params/stun_port", sys.sip.stun.port);
				p.setInt("/params/ex_enable", sys.sip.enable);
				p.setText("/params/ex_user", sys.sip.user);
				p.setText("/params/outbound", sys.sip.outbound);
				p.setInt("/params/timeout", sys.talk.timeout);
				p.setInt("/params/host2id", sys.sip.host2id);

				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/voip/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				String s = p.getText("/params/proxy");
				if (s != null) {
					if (s.startsWith("sip:"))
						sys.sip.proxy = s;
					else
						sys.sip.proxy = "sip:" + s;
				}
				sys.sip.realm = p.getText("/params/realm", sys.sip.realm);
				sys.sip.outbound = p.getText("/params/outbound", sys.sip.outbound);

				sys.sip.stun.ip = p.getText("/params/stun_ip", sys.sip.stun.ip);
				sys.sip.stun.port = p.getInt("/params/stun_port", sys.sip.stun.port);

				sys.sip.enable = p.getInt("/params/ex_enable", sys.sip.enable);
				sys.sip.user = p.getText("/params/ex_user", sys.sip.user);
				sys.sip.passwd = p.getText("/params/passwd", sys.sip.passwd);

				sys.talk.timeout = p.getInt("/params/timeout", 300);
				sys.sip.nVideo = p.getInt("/params/nVideo", sys.sip.nVideo);
				sys.sip.host2id = p.getInt("/params/host2id", 0);

				sys.save();
			}
		};
		elist.add(de);

		de = new devent("/ui/web/advanced/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();

				for (int i = 0; i < 4; i++) {
					p.setInt("/params/ex_slv" + i, sys.link.enable[i]);
					p.setText("/params/ex_slv_url" + i, sys.link.url[i]);
				}
				p.setInt("/params/auto_pickup", sys.talk.auto_answer);
				p.setInt("/params/onu_arp", sys.talk.onu_arp);
				p.setInt("/params/quick/enable", setup.quick.enable);
				p.setText("/params/quick/url", setup.quick.url);

				p.setInt("/params/unlock/dtmf/enable", setup.unlock.dtmf.enable);
				p.setText("/params/unlock/dtmf/data", setup.unlock.dtmf.data);

				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/advanced/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				for (int i = 0; i < 4; i++) {
					sys.link.enable[i] = p.getInt("/params/ex_slv" + i, 0);
					sys.link.url[i] = p.getText("/params/ex_slv_url" + i);
				}
				sys.talk.auto_answer = p.getInt("/params/auto_pickup", 0);
				sys.talk.onu_arp = p.getInt("/params/onu_arp", 0);
				sys.save();

				setup.quick.enable = p.getInt("/params/quick/enable", 0);
				setup.quick.url = p.getText("/params/quick/url");

				setup.unlock.dtmf.enable = p.getInt("/params/unlock/dtmf/enable", 0);
				setup.unlock.dtmf.data = p.getText("/params/unlock/dtmf/data");

				setup.save();
			}
		};
		elist.add(de);

		de = new devent("/ui/web/room/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setInt("/params/build", sys.talk.building);
				p.setInt("/params/unit", sys.talk.unit);
				p.setInt("/params/floor", sys.talk.floor);
				p.setInt("/params/family", sys.talk.family);
				p.setInt("/params/dcode", sys.talk.dcode);
				p.setText("/params/sync", sys.talk.sync);
				p.setText("/params/server", sys.talk.server);
				p.setText("/params/passwd", sys.talk.passwd);

				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/room/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				sys.talk.building = p.getInt("/params/build", sys.talk.building);
				sys.talk.unit = p.getInt("/params/unit", sys.talk.unit);
				sys.talk.floor = p.getInt("/params/floor", sys.talk.floor);
				sys.talk.family = p.getInt("/params/family", sys.talk.family);
				sys.talk.dcode = p.getInt("/params/dcode", sys.talk.dcode);
				sys.talk.sync = p.getText("/params/sync", sys.talk.sync);
				sys.talk.server = p.getText("/params/server", sys.talk.server);
				sys.talk.passwd = p.getText("/params/passwd", sys.talk.passwd);
				sys.save();

				dmsg req = new dmsg();
				req.to("/talk/slave/reset", null);
			}
		};
		elist.add(de);

        de = new devent("/ui/broadcast/data") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);

                dxml p = new dxml();
                p.parse(body);
                String s = p.getText("/event/broadcast_url");
                int b = p.getInt("/event/build", 0);
                int u = p.getInt("/event/unit", 0);
                int f = p.getInt("/event/floor", 0);
                int r = p.getInt("/event/family", 0);

                if (s.equals("/security/setup")) {
                    dmsg req = new dmsg();
                    req.to("/security/broadcast/data", body);
                } else if (s.equals("/access/lock")) { //增加接收门口机开关门状态判断，参数如上，此处接收后要通知的desktop可以通过广播，
                    //发送广播参考 security com/dnake/security/security.java中的 mBroadcast()
                    //接收广播在desktop com/dnake/desktop/SysReceiver.java中增加
                    int status = p.getInt("/event/data", 0);
                    Intent it = new Intent("com.dnake.doorStatus");
                    it.putExtra("status", status);
                    talk.mContext.sendBroadcast(it);
                } else {
                    if (b == sys.talk.building && u == sys.talk.unit && f == sys.talk.floor && r == sys.talk.family) {
                        if (s.equals("/access/auth/card")) {
                            if (talk.mContext != null) {
                                Intent it = new Intent("com.dnake.broadcast");
                                it.putExtra("event", "com.dnake.eHome.sync");
                                it.putExtra("card", p.getText("/event/card"));
                                talk.mContext.sendBroadcast(it);
                            }
                        } else if (s.equals("/access/auth/add")) {
                            if (talk.mContext != null) {
                                Intent it = new Intent("com.dnake.broadcast");
                                it.putExtra("event", "com.dnake.eHome.add");
                                it.putExtra("card", p.getText("/event/card"));
                                talk.mContext.sendBroadcast(it);
                            }
                        }
                    }
                }
            }
        };
        elist.add(de);

    }
}
