package com.dnake.talk;

import com.dnake.v700.sys;
import com.dnake.v700.sound;
import com.dnake.widget.Button2;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

public class VoipLabel extends BaseLabel {
	private Button2 btn_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voip);

		EditText e = (EditText) findViewById(R.id.voip_edit_proxy);
		e.setText(sys.sip.proxy);
		e = (EditText) findViewById(R.id.voip_edit_realm);
		e.setText(sys.sip.realm);
		e = (EditText) findViewById(R.id.voip_edit_passwd);
		e.setText(sys.sip.passwd);
		CheckBox c = (CheckBox) findViewById(R.id.voip_ck_ex_en);
		c.setChecked(sys.sip.enable == 0 ? false : true);
		e = (EditText) findViewById(R.id.voip_edit_ex_user);
		e.setText(sys.sip.user);
		e = (EditText) findViewById(R.id.voip_edit_stunip);
		e.setText(sys.sip.stun.ip);
		e = (EditText) findViewById(R.id.voip_edit_stunport);
		e.setText(String.valueOf(sys.sip.stun.port));

		this.btn_ok = (Button2) this.findViewById(R.id.voip_btn_ok);
		this.btn_ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText e = (EditText) findViewById(R.id.voip_edit_proxy);
				String s = e.getText().toString();
				if (s.indexOf("sip:") == -1) {
					s = "sip:" + s;
					e.setText(s);
				}
				sys.sip.proxy = new String(s);
				e = (EditText) findViewById(R.id.voip_edit_realm);
				sys.sip.realm = new String(e.getText().toString());
				e = (EditText) findViewById(R.id.voip_edit_passwd);
				sys.sip.passwd = new String(e.getText().toString());
				CheckBox c = (CheckBox) findViewById(R.id.voip_ck_ex_en);
				sys.sip.enable = c.isChecked() ? 1 : 0;
				e = (EditText) findViewById(R.id.voip_edit_ex_user);
				sys.sip.user = new String(e.getText().toString());
				e = (EditText) findViewById(R.id.voip_edit_stunip);
				sys.sip.stun.ip = new String(e.getText().toString());
				e = (EditText) findViewById(R.id.voip_edit_stunport);
				sys.sip.stun.port = Integer.valueOf(e.getText().toString());
				sys.save();

				sound.play(sound.modify_success, false);
			}
		});
	}
}
