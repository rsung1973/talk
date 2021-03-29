package com.dnake.talk;

import com.dnake.v700.dmsg;
import com.dnake.v700.sys;
import com.dnake.v700.sound;
import com.dnake.widget.Button2;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class RoomLabel extends BaseLabel {
	private Button2 btn_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room);

		EditText e = (EditText) findViewById(R.id.room_edit_build);
		e.setText(String.valueOf(sys.talk.building));
		e = (EditText) findViewById(R.id.room_edit_unit);
		e.setText(String.valueOf(sys.talk.unit));
		e = (EditText) findViewById(R.id.room_edit_floor);
		e.setText(String.valueOf(sys.talk.floor));
		e = (EditText) findViewById(R.id.room_edit_family);
		e.setText(String.valueOf(sys.talk.family));
		e = (EditText) findViewById(R.id.room_edit_dcode);
		e.setText(String.valueOf(sys.talk.dcode));
		e = (EditText) findViewById(R.id.room_edit_sync);
		e.setText(sys.talk.sync);
		e = (EditText) findViewById(R.id.room_edit_server);
		e.setText(sys.talk.server);
		e = (EditText) findViewById(R.id.room_edit_passwd);
		e.setText(sys.talk.passwd);

		this.btn_ok = (Button2) this.findViewById(R.id.room_btn_ok);
		this.btn_ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int building = 0, unit = 0, floor = 0, family = 0, dcode = 0;
				String sync, server, passwd, s;
				Boolean ok = true;

				EditText e = (EditText) findViewById(R.id.room_edit_build);
				s = e.getText().toString();
				if (s != null && s.length() > 0)
					building = Integer.valueOf(s);
				else
					ok = false;
				e = (EditText) findViewById(R.id.room_edit_unit);
				s = e.getText().toString();
				if (s != null && s.length() > 0)
					unit = Integer.valueOf(s);
				else
					ok = false;
				e = (EditText) findViewById(R.id.room_edit_floor);
				s = e.getText().toString();
				if (s != null && s.length() > 0)
					floor = Integer.valueOf(s);
				else
					ok = false;
				e = (EditText) findViewById(R.id.room_edit_family);
				s = e.getText().toString();
				if (s != null && s.length() > 0)
					family = Integer.valueOf(s);
				else
					ok = false;
				e = (EditText) findViewById(R.id.room_edit_dcode);
				s = e.getText().toString();
				if (s != null && s.length() > 0)
					dcode = Integer.valueOf(s);
				else
					ok = false;

				e = (EditText) findViewById(R.id.room_edit_sync);
				sync = e.getText().toString();
				e = (EditText) findViewById(R.id.room_edit_server);
				server = e.getText().toString();
				e = (EditText) findViewById(R.id.room_edit_passwd);
				passwd = e.getText().toString();

				if (server == null || passwd == null || sync == null || sync.length() >= 16 || server.length() >= 32)
					ok = false;

				if (building < 1 || building >= 1000)
					ok = false;
				if (unit >= 100 || floor >= 99 || family >= 100 || dcode >= 10)
					ok = false;

				if (ok) {
					sys.talk.building = building;
					sys.talk.unit = unit;
					sys.talk.floor = floor;
					sys.talk.family = family;
					sys.talk.dcode = dcode;
					sys.talk.sync = new String(sync);
					sys.talk.server = new String(server);
					sys.talk.passwd = new String(passwd);
					sys.save();

					sound.play(sound.modify_success, false);

					dmsg req = new dmsg();
					req.to("/talk/slave/reset", null);
				} else {
					sound.play(sound.modify_failed, false);
				}
			}
		});
	}
}
