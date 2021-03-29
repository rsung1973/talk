package com.dnake.v700;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.dnake.logger.AviLogger;
import com.dnake.logger.JpegLogger;
import com.dnake.logger.TalkLogger;
import com.dnake.talk.R;
import com.dnake.talk.TalkLabel;
import com.dnake.talk.WakeTask;
import com.dnake.widget.Storage;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class talk extends Service {
	public static long mMuteTs = 0;
	public static int mHandset = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static class qResult {
		public __sip sip = new __sip();
		public __d600 d600 = new __d600();
		public __slaves slaves = new __slaves();
		public int result = 0;

		public class __sip {
			public String url = null;
			public int proxy;
		}

		public class __d600 {
			public String ip = null;
			public String host = null;
		}

		public class __slaves {
			public static final int MAX = 10;
			public String url[] = new String[MAX];

			public void load(dxml p) {
				for(int i=0; i<MAX; i++)
					url[i] = p.getText("/params/url"+i);
			}
		}
	}

	private static Handler e_talk_start = null;
	private static Handler e_talk_stop = null;
	private static Handler e_talk_play = null;
	private static Handler e_talk_answer = null;
	private static Handler e_talk_hungup = null;
	private static Handler e_talk_ipErr = null;
	private static Handler e_talk_touch = null;
	private static Handler e_talk_key = null;

	public static qResult qResult = new qResult();
	public static Context mContext = null;

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;

		dmsg.start("/ui");
		devent.setup();
		sys.load();
		setup.load();
		sound.load();

		Storage.load();
		TalkLogger.load();
		JpegLogger.load();
		AviLogger.load();

		WakeTask.onCreate(this);

		e_talk_start = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (TalkLabel.mIntent == null) {
					WakeTask.acquire();
					for(int i=0; i<3; i++) {
						if (WakeTask.isScreenOn())
							break;
						try {
							Thread.sleep(800);
						} catch (InterruptedException e) {
						}
					}

					dmsg req = new dmsg();
					while (req.to("/media/rtsp/length", null) == 200) {
						req.to("/media/rtsp/stop", null);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}

					TalkLabel.mIntent = new Intent(talk.this, TalkLabel.class);
					TalkLabel.mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(TalkLabel.mIntent);
				}
			}
		};

		e_talk_stop = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (TalkLabel.mContext != null)
					TalkLabel.mContext.hungup();
				TalkLabel.mContext = null;
				TalkLabel.mIntent = null;
			}
		};

		e_talk_play = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (TalkLabel.mContext != null) {
					TalkLabel.mContext.play();
				} else {
					if (talk_play_err++ < 10) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						e_talk_play.sendMessage(e_talk_play.obtainMessage());
					}
				}
			}
		};

		e_talk_answer = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (TalkLabel.mContext != null)
					TalkLabel.mContext.answer();
			}
		};

		e_talk_hungup = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (TalkLabel.mContext != null)
					TalkLabel.mContext.hungup();
			}
		};

		e_talk_ipErr = new Handler() {
			private Toast mToast = null;

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (ipErr_result == 1 && mContext != null) { //IP冲突
					String s = mContext.getString(R.string.ip_conflict)+", MAC["+ipErr_mac+"]";
					if (mToast == null)
						mToast = Toast.makeText(mContext, s, Toast.LENGTH_LONG);
					else
						mToast.setText(s);
					mToast.show();
				}
			}
		};

		e_talk_touch = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				WakeTask.acquire();
				if (sys.mute == 1) {
					if (Math.abs(System.currentTimeMillis()-mMuteTs) >= 8*60*60*1000) {
						// 大于8小时取消静音
						sys.mute = 0;
						talk.mBroadcast();
					}
				}
			}
		};

		e_talk_key = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (mContext != null) {
					AudioManager a = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
					a.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5f);
				}
			}
		};

		String lang = "CHS";
		if (!Locale.getDefault().getCountry().equals("CN"))
			lang = "EN";
		try {
			FileOutputStream out = new FileOutputStream("/var/etc/language");
			out.write(lang.toString().getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		devent.boot = true;

		dmsg req = new dmsg();
		req.to("/talk/setid", null);
		req.to("/talk/slave/reset", null);
		req.to("/control/eth/reset", null);

		talk.broadcast();
		sLocale.load();
		eDhcp.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		WakeTask.onDestroy();
	}

	public static void talk_start() {
		if (e_talk_start != null)
			e_talk_start.sendMessage(e_talk_start.obtainMessage());
	}

	public static void talk_stop() {
		if (e_talk_stop != null)
			e_talk_stop.sendMessage(e_talk_stop.obtainMessage());
	}

	private static int talk_play_err = 0;
	public static void talk_play() {
		talk_play_err = 0;
		if (e_talk_play != null)
			e_talk_play.sendMessage(e_talk_play.obtainMessage());
	}

	public static void talk_answer() {
		if (e_talk_answer != null)
			e_talk_answer.sendMessage(e_talk_answer.obtainMessage());
	}

	public static void talk_hungup() {
		if (e_talk_hungup != null)
			e_talk_hungup.sendMessage(e_talk_hungup.obtainMessage());
	}

	public static void touch() {
		if (e_talk_touch != null)
			e_talk_touch.sendMessage(e_talk_touch.obtainMessage());
	}

	public static void press() {
		if (e_talk_key != null)
			e_talk_key.sendMessage(e_talk_key.obtainMessage());
		touch();
	}

	public static void broadcast() {
		if (mContext != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.talk.data");
			it.putExtra("miss", TalkLogger.missed());
			it.putExtra("msg", TalkLogger.leaved());
			it.putExtra("mute", sys.mute);
			mContext.sendBroadcast(it);
		}
		mBroadcast();
	}

	public static void mBroadcast() {
		if (mContext != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.talk.mute");
			it.putExtra("mute", sys.mute);
			mContext.sendBroadcast(it);
		}
	}

	public static void tBroadcast() {
		if (mContext != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.talk.touch");
			mContext.sendBroadcast(it);
		}
	}

	public static void d400_ex_menu(int visible) {
		if (mContext != null && sys.limit() == 400) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.d400.ex_menu");
			it.putExtra("visible", visible);
			mContext.sendBroadcast(it);
		}
	}

	private static int ipErr_result = 0;
	private static String ipErr_mac = null;

	public static void ipMacErr(int result, String ip, String mac) {
		ipErr_result = result;
		ipErr_mac = mac;
		if (e_talk_ipErr != null)
			e_talk_ipErr.sendMessage(e_talk_ipErr.obtainMessage());
		talk.touch();
	}
}
