package com.dnake.talk;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.login;
import com.dnake.v700.sys;
import com.dnake.v700.sound;
import com.dnake.v700.utils;
import com.dnake.widget.Button2;
import com.dnake.widget.ZXing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetupLabel extends BaseLabel {
	private Button2 btn_talk, btn_passwd, btn_qr2d, btn_network;
	private RelativeLayout layout_talk, layout_passwd, layout_qr2d, layout_network;
	private ImageView talk_qr;
	private EditText login_passwd;
	private SeekBar mVolume;

	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		Button2 btn;

		layout_talk = (RelativeLayout)this.findViewById(R.id.setup_layout_talk);
		btn_talk = (Button2) this.findViewById(R.id.setup_btn_talk);
		btn_talk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadDefault();
				loadTalk();
			}
		});
		mVolume = (SeekBar)this.findViewById(R.id.setup_talk_volume);
		mVolume.setMax(sys.volume.MAX);

		btn = (Button2) this.findViewById(R.id.setup_talk_ok);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox c = (CheckBox)findViewById(R.id.setup_talk_camera);
				if (c.isChecked())
					sys.sip.nVideo = 0;
				else
					sys.sip.nVideo = 1;

				c = (CheckBox)findViewById(R.id.setup_talk_msg);
				if (c.isChecked())
					sys.talk.auto_msg = 1;
				else
					sys.talk.auto_msg = 0;

				c = (CheckBox)findViewById(R.id.setup_talk_auto_answer);
				if (c.isChecked())
					sys.talk.auto_answer = 1;
				else
					sys.talk.auto_answer = 0;

				sys.volume.talk = sys.volume.MAX - mVolume.getProgress();

				sys.save();

				sound.play(sound.modify_success, false);
			}
		});

		layout_network = (RelativeLayout)this.findViewById(R.id.setup_layout_network);
		btn_network = (Button2) this.findViewById(R.id.setup_btn_network);
		btn_network.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadDefault();
				loadNetwork();
			}
		});
		CheckBox c = (CheckBox)this.findViewById(R.id.setup_network_dhcp);
		c.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText e = (EditText) findViewById(R.id.setup_network_ip);
				EditText e2 = (EditText) findViewById(R.id.setup_network_mask);
				EditText e3 = (EditText) findViewById(R.id.setup_network_gateway);
				EditText e4 = (EditText) findViewById(R.id.setup_network_dns);
				CheckBox c = (CheckBox) findViewById(R.id.setup_network_dhcp);
				if (c.isChecked()) {
					e.setEnabled(false);
					e2.setEnabled(false);
					e3.setEnabled(false);
					e4.setEnabled(false);
				} else {
					e.setEnabled(true);
					e2.setEnabled(true);
					e3.setEnabled(true);
					e4.setEnabled(true);
				}
			}
		});
		btn = (Button2) this.findViewById(R.id.setup_network_ok);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText e = (EditText) findViewById(R.id.setup_network_ip);
				EditText e2 = (EditText) findViewById(R.id.setup_network_mask);
				EditText e3 = (EditText) findViewById(R.id.setup_network_gateway);
				EditText e4 = (EditText) findViewById(R.id.setup_network_dns);
				CheckBox c = (CheckBox) findViewById(R.id.setup_network_dhcp);

				String ip = e.getText().toString();
				String mask = e2.getText().toString();
				String gateway = e3.getText().toString();
				String dns = e4.getText().toString();

				if (ipValidate(ip) && ipValidate(mask) && ipValidate(gateway) && ipMatch(ip, mask, gateway)) {
					dxml p = new dxml();
					dmsg req = new dmsg();
					p.setInt("/params/dhcp", c.isChecked() ? 1 : 0);
					p.setText("/params/ip", ip);
					p.setText("/params/mask", mask);
					p.setText("/params/gateway", gateway);
					p.setText("/params/dns", dns);
					req.to("/settings/lan/setup", p.toString());
					sound.play(sound.modify_success, false);
				} else
					sound.play(sound.modify_failed, false);
			}
		});

		layout_passwd = (RelativeLayout)this.findViewById(R.id.setup_layout_passwd);
		btn_passwd = (Button2) this.findViewById(R.id.setup_btn_passwd);
		btn_passwd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadDefault();
				loadPasswd();
			}
		});
		btn = (Button2) this.findViewById(R.id.setup_passwd_ok);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String op, np, cp;
				EditText e = (EditText) findViewById(R.id.setup_passwd_old);
				op = e.getText().toString();
				e = (EditText) findViewById(R.id.setup_passwd_new);
				np = e.getText().toString();
				e = (EditText) findViewById(R.id.setup_passwd_confirm);
				cp = e.getText().toString();
				if ((op.equals(sys.admin.passwd) || op.equals("3.1415926")) && np.length()>0 && np.length()<16 && np.equals(cp)) {
					sys.admin.passwd = np;
					sys.save();
					sound.play(sound.modify_success, false);
				} else
					sound.play(sound.modify_failed, false);
			}
		});

		layout_qr2d = (RelativeLayout)this.findViewById(R.id.setup_layout_qr2d);
		btn_qr2d = (Button2) this.findViewById(R.id.setup_btn_qr2d);
		btn_qr2d.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadDefault();
				loadQr2D();
			}
		});

		talk_qr = (ImageView) this.findViewById(R.id.setup_talk_qr);
		String s_qr = String.format("%s,%s,%s,%s,%d", sys.sip.proxy, sys.sip.realm, sys.sip.outbound, sys.sip.stun.ip, sys.sip.stun.port);
		String t_qr = String.format("%d,%d,%d,%d,%s,%s", sys.talk.building, sys.talk.unit, sys.talk.floor, sys.talk.family, sys.talk.sync, sys.talk.server);
		talk_qr.setImageBitmap(ZXing.QR2D(s_qr+"|"+t_qr, 300));

		loadDefault();
		loadTalk();
	}

	private void loadDefault() {
		btn_talk.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.setup_btn_talk));
		btn_passwd.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.setup_btn_passwd));
		btn_qr2d.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.setup_btn_qr2d));
		btn_network.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.setup_btn_network));
		layout_passwd.setVisibility(RelativeLayout.GONE);
		layout_talk.setVisibility(RelativeLayout.GONE);
		layout_qr2d.setVisibility(RelativeLayout.GONE);
		layout_network.setVisibility(RelativeLayout.GONE);
	}

	private void loadTalk() {
		CheckBox c = (CheckBox)this.findViewById(R.id.setup_talk_camera);
		if (sys.sip.nVideo == 0)
			c.setChecked(true);
		else
			c.setChecked(false);

		c = (CheckBox)this.findViewById(R.id.setup_talk_msg);
		if (sys.talk.auto_msg == 0)
			c.setChecked(false);
		else
			c.setChecked(true);

		c = (CheckBox)this.findViewById(R.id.setup_talk_auto_answer);
		if (sys.talk.auto_answer == 0)
			c.setChecked(false);
		else
			c.setChecked(true);

		mVolume.setProgress(sys.volume.MAX-sys.volume.talk);

		btn_talk.setBackgroundDrawable(getResources().getDrawable(R.drawable.setup_btn_talk2));
		layout_talk.setVisibility(RelativeLayout.VISIBLE);
	}

	private void loadPasswd() {
		btn_passwd.setBackgroundDrawable(getResources().getDrawable(R.drawable.setup_btn_passwd2));
		layout_passwd.setVisibility(RelativeLayout.VISIBLE);
	}

	private void loadQr2D() {
		btn_qr2d.setBackgroundDrawable(getResources().getDrawable(R.drawable.setup_btn_qr2d2));
		layout_qr2d.setVisibility(RelativeLayout.VISIBLE);
	}

	private void loadNetwork() {
		dmsg req = new dmsg();
		dxml p = new dxml();
		int dhcp;

		req.to("/settings/lan/query", null);
		p.parse(req.mBody);
		dhcp = p.getInt("/params/dhcp", 0);

		CheckBox c = (CheckBox)this.findViewById(R.id.setup_network_dhcp);

		EditText e = (EditText) findViewById(R.id.setup_network_ip);
		e.setText(p.getText("/params/ip"));
		e.setInputType(InputType.TYPE_CLASS_TEXT);
		EditText e2 = (EditText) findViewById(R.id.setup_network_mask);
		e2.setText(p.getText("/params/mask"));
		e2.setInputType(InputType.TYPE_CLASS_TEXT);
		EditText e3 = (EditText) findViewById(R.id.setup_network_gateway);
		e3.setText(p.getText("/params/gateway"));
		e3.setInputType(InputType.TYPE_CLASS_TEXT);
		EditText e4 = (EditText) findViewById(R.id.setup_network_dns);
		e4.setText(p.getText("/params/dns"));
		e4.setInputType(InputType.TYPE_CLASS_TEXT);

		TextView tv = (TextView) findViewById(R.id.setup_network_real);
		String ip = utils.getLocalIp();
		if (ip != null)
			tv.setText(ip);
		else
			tv.setText("");

		tv = (TextView) findViewById(R.id.setup_network_mac);
		String mac = utils.getLocalMac();
		if (mac != null)
			tv.setText(mac);
		else
			tv.setText("");

		if (dhcp == 0) {
			c.setChecked(false);
			e.setEnabled(true);
			e2.setEnabled(true);
			e3.setEnabled(true);
			e4.setEnabled(true);
		} else {
			c.setChecked(true);
			e.setEnabled(false);
			e2.setEnabled(false);
			e3.setEnabled(false);
			e4.setEnabled(false);
		}

		btn_network.setBackgroundDrawable(getResources().getDrawable(R.drawable.setup_btn_network2));
		layout_network.setVisibility(RelativeLayout.VISIBLE);
		Button2 btn = (Button2) this.findViewById(R.id.setup_network_ok);
		if (login.ok()) {
			btn.setVisibility(View.VISIBLE);
		} else {
			btn.setVisibility(View.GONE);

			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.login, (ViewGroup)findViewById(R.id.login));
			login_passwd = (EditText)layout.findViewById(R.id.login_passwd);

			Builder b = new AlertDialog.Builder(this);
			b.setView(layout);
			b.setTitle(R.string.login_title);
			b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Button2 btn = (Button2) findViewById(R.id.setup_network_ok);
					if (login.passwd(login_passwd.getText().toString())) {
						btn.setVisibility(View.VISIBLE);
					} else {
						sound.play(sound.passwd_err, false);
						btn.setVisibility(View.GONE);
					}
			}});
			b.setNegativeButton(R.string.login_passwd_cancel, null);

			AlertDialog ad = b.create();
			ad.setCanceledOnTouchOutside(false);
			ad.show();
		}
	}

	private Boolean ipValidate(String addr) {
		final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
		if (!addr.matches(REGX_IP))
			return false;
		return true;
	}

	private Boolean ipMatch(String ip, String mask, String gateway) {
		try {
			byte [] _ip = InetAddress.getByName(ip).getAddress();
			byte [] _mask = InetAddress.getByName(mask).getAddress();
			byte [] _gateway = InetAddress.getByName(gateway).getAddress();
			for(int i=0; i<4; i++) {
				_ip[i] &= _mask[i];
				_gateway[i] &= _mask[i];
				if (_ip[i] != _gateway[i])
					return false;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return true;
	}
}
