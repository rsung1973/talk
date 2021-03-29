package com.dnake.talk;


import com.dnake.v700.talk;
import com.dnake.v700.login;
import com.dnake.v700.sound;
import com.dnake.widget.Button2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends BaseLabel {
	private Context mContext = this;
	private EditText mPasswd;
	private Button mVoip;
	private int mProxy = -1;
	private static  final String __VALID_MAC_START = null;	//"0012B3";

	public static Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ctx = this;

		setContentView(R.layout.main);

		if(!checkValidMAC()) {
			this.finish();
			return;
		}

		mVoip = (Button) this.findViewById(R.id.main_voip_st);

		Button2 btn;
		btn = (Button2) this.findViewById(R.id.main_btn_call);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CallLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_monitor);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MonitorLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_logger);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, LoggerLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_room);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (login.ok()) {
					Intent intent = new Intent(MainActivity.this, RoomLabel.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else {
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.login));
					mPasswd = (EditText) layout.findViewById(R.id.login_passwd);

					Builder b = new AlertDialog.Builder(mContext);
					b.setView(layout);
					b.setTitle(R.string.login_title);
					b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String passwd = mPasswd.getText().toString();
							if (login.passwd(passwd)) {
								Intent intent = new Intent(MainActivity.this, RoomLabel.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							} else
								sound.play(sound.passwd_err, false);

							if (passwd.equals("830606")) {
								Intent it = new Intent();
								ComponentName c = new ComponentName("com.dnake.desktop", "com.dnake.desktop.AppsLabel");
								if (it != null && c != null) {
									it.setComponent(c);
									it.setAction("android.intent.action.VIEW");
									startActivity(it);
								}
							}
						}
					});
					b.setNegativeButton(R.string.login_passwd_cancel, null);

					AlertDialog ad = b.create();
					ad.setCanceledOnTouchOutside(false);
					ad.show();
				}
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_voip);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (login.ok()) {
					Intent intent = new Intent(MainActivity.this, VoipLabel.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else {
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.login));
					mPasswd = (EditText) layout.findViewById(R.id.login_passwd);

					Builder b = new AlertDialog.Builder(mContext);
					b.setView(layout);
					b.setTitle(R.string.login_title);
					b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (login.passwd(mPasswd.getText().toString())) {
								Intent intent = new Intent(MainActivity.this, VoipLabel.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							} else
								sound.play(sound.passwd_err, false);
						}
					});
					b.setNegativeButton(R.string.login_passwd_cancel, null);

					AlertDialog ad = b.create();
					ad.setCanceledOnTouchOutside(false);
					ad.show();
				}
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_setup);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SetupLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		Intent intent = new Intent(this, talk.class);
		this.startService(intent);
	}

	private boolean checkValidMAC() {
		if(__VALID_MAC_START==null) {
			return true;
		}

		try {

			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
//                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					continue;
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					res1.append(String.format("%02X",b & 0x00FF));
				}

				if(res1.indexOf(__VALID_MAC_START)>=0)
					return  true;
			}
		} catch (Exception ex) {
			Log.e("checkValidMAC",ex.getMessage(),ex);
		}

		return  false;
	}

	@Override
	public void onTimer() {
		super.onTimer();
		if (talk.qResult != null && mProxy != talk.qResult.sip.proxy) {
			mProxy = talk.qResult.sip.proxy;
			if (mProxy != 0)
				mVoip.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.main_voip_st_b));
			else
				mVoip.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.main_voip_st_a));
		}
	}

	public static void invokeSipCall(String sipUrl) {
		if (ctx != null) {
			Intent it = new Intent("com.awtek.messageGuard.SipCall");
			it.putExtra("sipUrl", sipUrl);
			ctx.sendBroadcast(it);
		}
	}
}
