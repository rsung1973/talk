package com.dnake.talk;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import com.dnake.logger.AviLogger;
import com.dnake.logger.JpegLogger;
import com.dnake.logger.TalkLogger;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.ioctl;
import com.dnake.v700.sCaller;
import com.dnake.v700.sound;
import com.dnake.v700.sys;
import com.dnake.v700.talk;
import com.dnake.widget.Button2;
import com.dnake.widget.Storage;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.annotation.SuppressLint;
import android.content.Intent;

@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
public class TalkLabel extends BaseLabel {
	public static TalkLabel mContext = null;
	public static Intent mIntent = null;

	public static Boolean mTalking = false;
	public static String mHost;
	public static int mType = 0;

	public static Boolean mUsbMode = false; // 非可视副机模式
	public static String mBusyHost = null;
	private static Boolean mHostId = false;
	public static Boolean mUseFullscreen = false; //呼叫全屏

	public long mStart = 0, mTalkTs = 0;
	private long mPromptTs = 0;
	private TextView mPrompt;

	private ImageView mVideo;
	private int mViVisible = 0;

	private Button2 mAnswer, mRec;
	private Boolean mIsRec = false;
	private TextView mRecText;

	private TextView mHostText;

	private SeekBar mVolume;
	private Button2 mMic, mDtmf;
	private Boolean mMute = false, mDtmfKey = false;

	private DtmfLabel mDtmfLabel = new DtmfLabel();
	private int mLcdHeight = 600;

	private RelativeLayout mLayoutFull;

	private MediaPlayer mPlayer = null;
	private Boolean mStartPlayer = true;
	private Boolean mStartVo = false;
	private Boolean mJpeg = false;
	private String ringTone = sound.ringing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talk);
		if (mIntent == null) { // 任务栏直接进入切换处理
			mContext = null;

			Intent it = new Intent(this, MainActivity.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);

			finish();
			return;
		}
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		if (LoggerViewLabel.mPlayer != null) { // 停止录象播放
			LoggerViewLabel.mPlayer.stop();
			LoggerViewLabel.mPlayer.release();
			LoggerViewLabel.mPlayer = null;
		}

		mLcdHeight = this.getResources().getDisplayMetrics().heightPixels;
		if (mLcdHeight > 600) {
			TextView dummy = (TextView) this.findViewById(R.id.talk_align_dummy);
			RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) dummy.getLayoutParams();
			layout.height = (int) (16 * sys.scaled);
			dummy.setLayoutParams(layout);
		}

		mStart = System.currentTimeMillis();
		mTalkTs = System.currentTimeMillis();

		mVideo = (ImageView) this.findViewById(R.id.talk_view);
		mVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUseFullscreen) {
					startFullscreen();
				} else {
					if (mLcdHeight > 272) {
						if (mDtmfKey == false) {
							mViVisible = mViVisible == 1 ? 0 : 1;
							dxml p = new dxml();
							dmsg req = new dmsg();
							p.setInt("/params/visible", mViVisible);
							req.to("/talk/vi_render", p.toString());
						}
					} else {
						mDtmfKey = mDtmfKey ? false : true;
						if (mDtmfKey)
							mDtmfLabel.show();
						else
							mDtmfLabel.hide();
					}
				}
			}
		});

		this.mPrompt = (TextView) this.findViewById(R.id.talk_prompt);
		this.mPrompt.setText("");

		this.mRecText = (TextView) this.findViewById(R.id.talk_text);
		this.mRecText.setText("");

		this.mHostText = (TextView) this.findViewById(R.id.talk_host);

		this.mAnswer = (Button2) this.findViewById(R.id.talk_answer);
		this.mAnswer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mTalking)
					hungup();
				else
					answer();
			}
		});

		Button2 b;
		b = (Button2) this.findViewById(R.id.talk_unlock);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sCaller.unlock();
                Intent it = new Intent("com.dnake.doorStatus");
				it.putExtra("status", 1);
				sendBroadcast(it);
			}
		});

		b = (Button2) this.findViewById(R.id.talk_jpeg);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setText("/params/url", Storage.sdcard + "/Snapshot/" + String.valueOf(System.currentTimeMillis()) + ".jpg");
				req.to("/talk/snapshot", p.toString());
			}
		});

		this.mRec = (Button2) this.findViewById(R.id.talk_rec);
		this.mRec.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onRecClicked();
			}
		});
		this.mRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_rec));

		mVolume = (SeekBar) this.findViewById(R.id.talk_volume);
		mVolume.setMax(4 * sys.volume.MAX);
		mVolume.setProgress(4 * (sys.volume.MAX - sys.volume.talk));
		mVolume.setEnabled(false);
		mVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			private boolean mFromUser = false;

			@Override
			public void onProgressChanged(SeekBar arg0, int progrees, boolean fromUser) {
				mFromUser = fromUser;
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				int m = (arg0.getProgress() + 3) / 4;
				if (mFromUser) {
					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/volume", sys.volume.MAX - m);
					req.to("/talk/volume", p.toString());
				}
				mFromUser = false;
				mVolume.setProgress(4 * m);
			}
		});

		mMic = (Button2) this.findViewById(R.id.talk_mic);
		mMic.setEnabled(false);
		mMic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mMute) {
					mMute = false;
					talk_mute(0, 0);
					mMic.setBackground(getResources().getDrawable(R.drawable.talk_mic_ok));
				} else {
					mMute = true;
					talk_mute(0, 1);
					mMic.setBackground(getResources().getDrawable(R.drawable.talk_mic_mute));
				}
			}
		});

		if (mLcdHeight > 272)
			mDtmfLabel.start(this, -120, -20);
		else
			mDtmfLabel.start(this, -50, 0);

		mDtmf = (Button2) this.findViewById(R.id.talk_dtmf);
		mDtmf.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDtmfKey = mDtmfKey ? false : true;
				if (mDtmfKey)
					mDtmfLabel.show();
				else
					mDtmfLabel.hide();
			}
		});

		mLayoutFull = (RelativeLayout) this.findViewById(R.id.talk_full);
		mLayoutFull.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStartVo = true;
				mLayoutFull.setVisibility(View.GONE);
			}
		});

		if (mType == TalkLogger.CALL_OUT)
			sCaller.logger(sCaller.logger.CALL);

		if (sys.mute == 1) {
			if (Math.abs(System.currentTimeMillis() - talk.mMuteTs) >= 8 * 6060 * 1000) {
				// 大于8小时取消静音
				sys.mute = 0;
				talk.mBroadcast();
			}
		}

		if (sys.sip.nVideo != 0)
			this.talk_mute(1, 1);

		mHostId = true;
	}

	private void startFullscreen() {
		int w = 1024, h = 600;

		if (mLcdHeight > 600) {
			w = 1280;
			h = 800;
		} else if (mLcdHeight > 480) {
			w = 1024;
			h = 600;
		} else if (mLcdHeight > 272) {
			w = 800;
			h = 480;
		} else {
			w = 480;
			h = 272;
		}

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/x", 0);
		p.setInt("/params/y", 0);
		p.setInt("/params/width", w);
		p.setInt("/params/height", h);
		req.to("/talk/vo_start", p.toString());

		mStartVo = false;
		mLayoutFull.setVisibility(View.VISIBLE);
	}

	public void onRecClicked() {
		if (mTalking && Storage.isMount(mContext, Storage.extsd)) {
			if (mIsRec) {
				mIsRec = false;
				mRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_rec));

				dmsg req = new dmsg();
				req.to("/talk/rec/stop", null);
				mRecText.setText("");
			} else {
				mIsRec = true;
				mRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_stop));

				dmsg req = new dmsg();
				dxml p = new dxml();
				SimpleDateFormat df = new SimpleDateFormat("yy_MM_dd_hh_mm_ss");
				String s = df.format(new java.util.Date());
				String url = Storage.extsd + "/Records/" + mHost + "_" + s + ".avi";
				p.setText("/params/url", url);
				req.to("/talk/rec/start", p.toString());

				AviLogger.insert(mStart, url);
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) { //页面显示完成
			talk.d400_ex_menu(0);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		if (mTalking) { // 呼出
			this.mAnswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_hungup));
		} else { // 呼入
			this.mAnswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_answer));
			ioctl.handset(1); // 振铃音强制使用外放
			mStartVo = true;
		}

		mContext = this;
		mBusyHost = null;
	}

	@Override
	public void onStop() {
		if (bRun) {
			//其他方式退出界面
			this.hungup();
		}

		if (mStart != 0) {
			long du = Math.abs(System.currentTimeMillis() - mTalkTs) / 1000;
			TalkLogger.insert(mHost, mStart, du, mType);
			if (mType == TalkLogger.CALL_OUT) {
				if (mPlayer != null)
					sCaller.logger(sCaller.logger.FAILED);
				else
					sCaller.logger(sCaller.logger.END);
			}
		}

		this.stopPlayer();
		talk.d400_ex_menu(1);
		mDtmfLabel.stop();
		h2id_list.clear();

		mStart = 0;
		mContext = null;
		mIntent = null;

		super.onStop();
	}

	private int mRecIdx = 0;

	@Override
	public void onTimer() {
		super.onTimer();

		if (mContext == null)
			return;

		WakeTask.acquire();
		if (mIsRec && mRecIdx++ > 3) {
			dmsg req = new dmsg();
			if (req.to("/talk/rec/length", null) != 200) {
				mIsRec = false;
				mRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_rec));
				mRecText.setText("");
			} else {
				dxml p = new dxml();
				p.parse(req.mBody);

				int len = p.getInt("/params/length", 0);
				int ss = len % 60;
				len /= 60;
				int mm = len % 60;
				String s = String.format("%02d:%02d", mm, ss);
				this.mRecText.setText(s);
			}
			mRecIdx = 0;
		}
		if (mStartVo) {
			int w = mVideo.getRight() - mVideo.getLeft();
			int h = mVideo.getBottom() - mVideo.getTop();
			if (w > 16 && h > 16) {
				if (mUseFullscreen) {
					startFullscreen();
				} else {
					int dummy = mLcdHeight < 280 ? 1 : 4;
					int[] xy = new int[2];
					mVideo.getLocationOnScreen(xy);

					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/x", xy[0] + dummy);
					p.setInt("/params/y", xy[1] + dummy);
					p.setInt("/params/width", w - 2 * dummy);
					p.setInt("/params/height", h - 2 * dummy);
					req.to("/talk/vo_start", p.toString());
					mStartVo = false;
				}
			}
		}

        ringTone = sound.ringing;
        if (mHostId) {
            this.mHostText.setText(this.host2id(mHost));
            mHostId = false;
        }

		if (mStartPlayer) {
			int w = mVideo.getRight() - mVideo.getLeft();
			if (w > 16) { //等待界面初始化完成再播放提示音
				this.stopPlayer();
				if (mTalking) { // 呼出
					mPlayer = sound.play(sound.ringback, true);
				} else {
					if (sys.mute == 0) { // 呼入
						Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
						if (uri == null)
							mPlayer = sound.play(ringTone, true);
						else
							mPlayer = sound.play(uri.toString(), true);
					}
				}
				mStartPlayer = false;
			}
		}

//        ringTone = sound.ringing;
//		if (mHostId) {
//			this.mHostText.setText(this.host2id(mHost));
//			mHostId = false;
//		}

		if (!mJpeg) {
			if (Math.abs(System.currentTimeMillis() - mStart) >= 3 * 1000) { // 3s抓拍
				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setText("/params/url", Storage.sdcard + "/Snapshot/" + String.valueOf(System.currentTimeMillis()) + ".jpg");
				req.to("/talk/snapshot", p.toString());
				mJpeg = true;
			}
		}
		if (!mTalking) {
			if (sys.talk.dcode == 0) {
				// 2秒自动接听
				if (sys.talk.auto_answer == 1 && Math.abs(System.currentTimeMillis() - mStart) >= 2 * 1000)
					this.answer();

				// 自动留言录像
				if (sys.talk.auto_msg == 1 && Math.abs(System.currentTimeMillis() - mStart) >= 20 * 1000 && Storage.isMount(mContext, Storage.extsd)) {
					this.answer();

					this.talk_mute(0, 1);
					this.talk_mute(1, 1);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setText("/params/url", sound.auto_msg_prompt);
					req.to("/talk/prompt", p.toString());

					onRecClicked();
					mType = TalkLogger.CALL_MSG;
				}
			}
		} else {
			if (mType == TalkLogger.CALL_MSG && Math.abs(System.currentTimeMillis() - mTalkTs) >= 30 * 1000) { // 自动留言
				this.hungup();
			}
		}

		if (Math.abs(System.currentTimeMillis() - mActiveTs) >= 3000) {
			mActiveTs = System.currentTimeMillis();

			dmsg req = new dmsg();
			if (req.to("/talk/active", null) == 200) {
				dxml p = new dxml();
				p.parse(req.mBody);
				if (p.getInt("/params/data", 0) == 0)
					mActive++;
				if (mActive >= 3) {
					mActive = 0;
					this.hungup();
				}
			}
		}

		if (mBusyHost != null) {
			String s = this.host2id(mBusyHost) + this.getResources().getString(R.string.talk_text_busy);
			this.mPrompt.setText(s);
			mPromptTs = System.currentTimeMillis();
			mBusyHost = null;
		}

		if (mPromptTs != 0 && Math.abs(mPromptTs - System.currentTimeMillis()) >= 5 * 1000) {
			mPromptTs = 0;
			this.mPrompt.setText("");
		}
	}

	private long mActiveTs = 0;
	private int mActive = 0;

	public void play() {
		this.stopPlayer();

		mStartVo = true;
		mVolume.setEnabled(true);
		mMic.setEnabled(true);

		sCaller.logger(sCaller.logger.ANSWER);
	}

	public void hungup() {
		dmsg req = new dmsg();
		req.to("/talk/stop", null);
		mTalking = false;
		mUsbMode = false;

		this.tStop();
		if (!this.isFinishing())
			finish();
	}

	public void answer() {
		if (mTalking == false) {
			this.stopPlayer();

			mType = TalkLogger.CALL_IN;
			mAnswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.talk_btn_hungup));

			dmsg req = new dmsg();
			req.to("/talk/start", null);

			mTalkTs = System.currentTimeMillis();
			mVolume.setEnabled(true);
			mMic.setEnabled(true);

			if (talk.mHandset != 0) // 手柄提机
				ioctl.handset(0);

			mTalking = true;
		}
	}

	public void snapshot(String url) {
		JpegLogger.insert(mStart, url);
	}

	public static class h2id {
		String host;
		int building;
		int unit;
		int floor;
		int family;
	}

	public static List<h2id> h2id_list = new LinkedList<h2id>();

	public static void host2id(dxml p) {
		mHostId = true;

		h2id id = new h2id();
		id.host = p.getText("/params/host");
		id.building = p.getInt("/params/building", 0);
		id.unit = p.getInt("/params/unit", 0);
		id.floor = p.getInt("/params/floor", 0);
		id.family = p.getInt("/params/family", 0);

		for (int i = 0; i < h2id_list.size(); i++) {
			h2id h = h2id_list.get(i);
			if (h.host.equals(id.host))
				return;
		}
		h2id_list.add(id);
	}

	public String host2id(String host) {
		String n = TalkLogger.queryName(host, this);
		if (n != null)
			return n;

		h2id id = null;
		for (int i = 0; i < h2id_list.size(); i++) {
			h2id h = h2id_list.get(i);
			if (h.host.equals(host)) {
				id = h;
				break;
			}
		}
		if (id != null) {
			if (id.building == 0 && id.unit == 0 && id.floor == 0) { // 管理机
                ringTone = sound.ringingForCenter;
				String s = this.getResources().getString(R.string.talk_text_center);
				return s + " " + id.family;
			} else if (id.floor == 99 && id.building > 0) { // 单元门口机
                ringTone = sound.ringingForMain;
				String s = this.getResources().getString(R.string.talk_text_main);
				return s + " " + id.family;
			} else if (id.floor == 99 && id.building == 0) { // 围墙机
                ringTone = sound.ringingForWall;
				String s = this.getResources().getString(R.string.talk_text_wall);
				return s + " " + id.family;
			} else if (host.length() == 10 && id.building == sys.talk.building && id.unit == sys.talk.unit && id.floor == sys.talk.floor && id.family == sys.talk.family) { // 小门口机呼叫
                ringTone = sound.ringingForPerson;
                String s = host.substring(0, 1);
				return this.getResources().getString(R.string.talk_text_person) + " " + s;
			} else {
				String s = String.format("%d%02d", id.floor, id.family);
				String b = this.getResources().getString(R.string.talk_text_build);
				String u = this.getResources().getString(R.string.talk_text_unit);
				return id.building + b + id.unit + u + s;
			}
		} else {
			char h = host.charAt(0);
			if (h == 'M') {
                ringTone = sound.ringingForMain;
				String s = this.getResources().getString(R.string.talk_text_main);
				return s + " " + String.valueOf(host.charAt(7) + 1 - '0');
			} else if (h == 'S') {
				String bb = this.getResources().getString(R.string.talk_text_build);
				String uu = this.getResources().getString(R.string.talk_text_unit);
				String b = host.substring(1, 5);
				String u = host.substring(5, 7);
				String r = host.substring(7, 11);
				return b + bb + u + uu + r;
			} else if (h == 'H') {
                ringTone = sound.ringingForPerson;
				String s = String.format("%d", host.charAt(11) + 1 - '0');
				return this.getResources().getString(R.string.talk_text_person) + " " + s;
			}
		}
		return host;
	}

	public void talk_mute(int av, int enable) {
		dxml p = new dxml();
		dmsg req = new dmsg();
		p.setInt("/params/mode", av);
		p.setInt("/params/enable", enable);
		req.to("/talk/mute", p.toString());
	}

	private void stopPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
}
