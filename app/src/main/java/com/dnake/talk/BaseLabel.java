package com.dnake.talk;

import com.dnake.v700.sound;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class BaseLabel extends Activity {

	private Handler e_timer = null;
	protected Boolean bFinish = true;

	private Thread bThread = null;
	protected Boolean bRun = true;

	public class ProcessThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			while(bRun) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
				if (e_timer != null)
	    			e_timer.sendMessage(e_timer.obtainMessage());
			}
		}
	}

	public void onTimer() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources r = this.getResources();
		sys.scaled = r.getDisplayMetrics().density;
		sound.load();
	}

	@Override
    public void onStart() {
		super.onStart();
	}

	@Override
    public void onStop() {
		super.onStop();
		this.tStop();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (bFinish && WakeTask.timeout()) {
			this.tStop();
			this.finish();
		} else
			this.onBaseStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void tStart() {
		bRun = true;
		if (bThread == null) {
			ProcessThread pt = new ProcessThread();
			bThread = new Thread(pt);
			bThread.start();
		}
	}

	protected void tStop() {
		bRun = false;
		if (bThread != null) {
			bThread.interrupt();
			bThread = null;
		}
	}

	private void onBaseStart() {
		if (e_timer == null) {
			e_timer = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					onTimer();

					if (bFinish && WakeTask.timeout()) {
						tStop();
						finish();
					}
				}
			};
		}
		WakeTask.acquire();
		this.tStart();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			this.onBaseStart();
		}
    }
}
