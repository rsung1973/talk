package com.dnake.talk;

import com.dnake.v700.dmsg;
import com.dnake.v700.sCaller;
import com.dnake.v700.talk;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.widget.Button2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class MonitorLabel extends BaseLabel {
	public static MonitorLabel mContext;
	public static Boolean bStop = false;
	private static Boolean mStartVo = false;
	private static int mRunning = 0;

	private ImageView mView;
	private Button mUnit;
	private TextView mErr;
	private String mTextUnit, mTextSub;
	private Boolean mIsUnit = true;
	private int mSelect = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor);

		mContext = this;
		mView = (ImageView) this.findViewById(R.id.monitor_view_label);
		mTextUnit = this.getResources().getString(R.string.monitor_select_unit);
		mTextSub = this.getResources().getString(R.string.monitor_select_sub);

		Button2 b;
		b = (Button2) this.findViewById(R.id.monitor_btn_left);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mSelect > 0) {
					mSelect--;
					String s = (mIsUnit ? mTextUnit : mTextSub)+String.format("%02d", mSelect+1);
					mUnit.setText(s);
					startMonitor();
				}
			}
		});

		mUnit = (Button) this.findViewById(R.id.monitor_btn_unit);
		mUnit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mIsUnit = mIsUnit ? false : true;
				String s = (mIsUnit ? mTextUnit : mTextSub)+String.format("%02d", mSelect+1);
				mUnit.setText(s);
			}
		});
		mUnit.setText(mTextUnit+String.format("%02d", mSelect+1));

		b = (Button2) this.findViewById(R.id.monitor_btn_right);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mSelect < 9) {
					mSelect++;
					String s = (mIsUnit ? mTextUnit : mTextSub)+String.format("%02d", mSelect+1);
					mUnit.setText(s);

					startMonitor();
				}
			}
		});

		b = (Button2) this.findViewById(R.id.monitor_btn_unlock);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sCaller.unlock();
                Intent it = new Intent("com.dnake.doorStatus");
                sendBroadcast(it);
            }
		});

		b = (Button2) this.findViewById(R.id.monitor_btn_start);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mRunning == 0)
					startMonitor();
			}
		});

		b = (Button2) this.findViewById(R.id.monitor_btn_stop);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mRunning = 0;

				dmsg req = new dmsg();
				req.to("/talk/stop", null);
				sCaller.stop();

				mErr.setText("");
			}
		});

		mErr = (TextView)this.findViewById(R.id.monitor_err_text);
		startMonitor();
	}

	public void startMonitor() {
		if (mRunning > 0) {
			dmsg req = new dmsg();
			req.to("/talk/stop", null);

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		sCaller.stop();

		mRunning = 1;
		if (mIsUnit) {
			sCaller.query(String.format("%d%02d99%02d", sys.talk.building, sys.talk.unit, mSelect+1)); //700门口机查询
			sCaller.q600(String.format("M%04d%02d%d000", sys.talk.building, sys.talk.unit, mSelect)); //800门口机查询
		} else {
			sCaller.query(String.format("%d%03d%02d%02d%02d", mSelect+1, sys.talk.building, sys.talk.unit, sys.talk.floor, sys.talk.family)); //700小门口机
			sCaller.q600(String.format("H%04d%02d%02d%02d%c", sys.talk.building, sys.talk.unit, sys.talk.floor, sys.talk.family, '0'+mSelect)); //800小门口机
		}
		mErr.setText(getResources().getString(R.string.monitor_err_search));
	}

	@Override
	public void onStart() {
		super.onStart();
		mContext = this;
		bStop = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		mContext = null;
		if (mRunning != 0) {
			dmsg req = new dmsg();
			req.to("/talk/stop", null);
		}
		mRunning = 0;
		sCaller.stop();
	}

	public static void start() {
		mStartVo = true;
		mRunning = 1;
	}

	public static void stop() {
		mRunning = 0;
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (mRunning > 0)
			WakeTask.acquire();

		if (bStop) {
			bStop = false;
			if (!this.isFinishing())
				this.finish();
			return;
		}

		if (sCaller.running == sCaller.QUERY) {
			if (sCaller.timeout() >= 3000) {
				mRunning = 0;
				mErr.setText(getResources().getString(R.string.monitor_err_addr));
				sCaller.stop();
			} else {
				if (talk.qResult.sip.url != null) {
					sCaller.m700(talk.qResult.sip.url);
				} else if (talk.qResult.d600.ip != null) {
					sCaller.m600(talk.qResult.d600.host, talk.qResult.d600.ip);
				}
			}
		} else if (sCaller.running == sCaller.CALL) {
			if (sCaller.timeout() >= 5000) {
				mRunning = 0;
				mErr.setText(getResources().getString(R.string.monitor_err_failed));
				sCaller.stop();

				dmsg req = new dmsg();
				req.to("/talk/stop", null);
			} else {
				if (talk.qResult.result == 180) { //对方振铃
					mErr.setText("");
					sCaller.stop();

					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/mode", 0);
					p.setInt("/params/enable", 1);
					req.to("/talk/mute", p.toString());
				} else if (talk.qResult.result >= 400) { //呼叫失败
					mRunning = 0;
					sCaller.stop();
					mErr.setText(getResources().getString(R.string.monitor_err_failed));
				}
			}
		}

		if (mStartVo) {
			int[] xy = new int[2];  
			mView.getLocationOnScreen(xy);

			if (xy[0] > 0 && xy[1] > 0) {
				DisplayMetrics dm = this.getResources().getDisplayMetrics();
				if (dm.heightPixels < 280) {
					int w = mView.getRight()-mView.getLeft()-4;
					int h = mView.getBottom()-mView.getTop()-4;

					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/x", xy[0]+2);
					p.setInt("/params/y", xy[1]+2);
					p.setInt("/params/width", w);
					p.setInt("/params/height", h);
					req.to("/talk/vo_start", p.toString());
				} else {
					int w = mView.getRight()-mView.getLeft()-8;
					int h = mView.getBottom()-mView.getTop()-8;

					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/x", xy[0]+4);
					p.setInt("/params/y", xy[1]+4);
					p.setInt("/params/width", w);
					p.setInt("/params/height", h);
					req.to("/talk/vo_start", p.toString());
				}
				mStartVo = false;
			}
		}
	}
}
