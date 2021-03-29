package com.dnake.talk;

import java.util.LinkedList;
import java.util.List;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sCaller;
import com.dnake.v700.setup;
import com.dnake.v700.talk;
import com.dnake.v700.sys;
import com.dnake.v700.talk.qResult.__slaves;
import com.dnake.widget.Button2;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class CallLabel extends BaseLabel {
	public static CallLabel mContext;
	public static Boolean bAuto = false;
	public static Boolean bStop = false;

	private TextView mText;
	private PhoneBookAdapter mAdapter;
	private Boolean bCenter = false;

	private String mData = new String();
	private int mDataIdx = 0;
	private int mBuild = 0;
	private int mUnit = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call);

		this.mText = (TextView) this.findViewById(R.id.call_text_data);

		Button2 btn;
		btn = (Button2) this.findViewById(R.id.call_btn_del);
		btn.setOnClickListener(new OnDeleteClickListener());

		btn = (Button2) this.findViewById(R.id.call_btn_build);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mDataIdx == 0 && mData.length() > 0) {
					mBuild = Integer.valueOf(mData);
					mData = "";
					mDataIdx++;

					if (sys.talk.unit == 0) {
						mUnit = 0;
						mDataIdx++;
					}
				}
				mText.setText(Data2Text());
			}
		});

		btn = (Button2) this.findViewById(R.id.call_btn_unit);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mDataIdx == 1 && mData.length() > 0) {
					mUnit = Integer.valueOf(mData);
					mData = "";
					mDataIdx++;
				}
				mText.setText(Data2Text());
			}
		});
		if (sys.talk.unit == 0)
			btn.setVisibility(View.GONE);

		btn = (Button2) this.findViewById(R.id.call_btn_center);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toCenter();
			}
		});

		this.setupKeypad();

		btn = (Button2) this.findViewById(R.id.call_btn_start);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				start();
			}
		});

		mAdapter = new PhoneBookAdapter(this);
		ListView book = (ListView) this.findViewById(R.id.call_phonebook);
		book.setAdapter(mAdapter);
		book.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int arg2, long arg3) {
				ContactData c = mAdapter.list.get(arg2);
				mDataIdx = 0;
				mData = c.phone.replaceAll(" ", "");
				mText.setText(Data2Text());

				mAdapter.select = arg2;
				mAdapter.notifyDataSetInvalidated();
			}
		});

		sCaller.stop();
		Intent intent = getIntent();
		if (intent != null) {
			String callID = intent.getStringExtra("callID");
			if (callID != null) {
				if ("center".equals(callID)) {
					toCenter();
				} else {
					mData = callID;
					start();
				}
			}
		}
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

		if (talk.qResult.result != 180 && talk.qResult.result != 200) {
			dmsg req = new dmsg();
			req.to("/media/usb/stop", null);

			showPromptText(null);
			sCaller.stop();
			this.reset();
		}
	}

	public void toCenter() {
		if (sCaller.running == sCaller.NONE) {
			mDataIdx = 0;
			mData = "";

			if (setup.quick.enable != 0) {
				sCaller.start(setup.quick.url);
				showPromptText(this.getResources().getString(R.string.call_err_call));
			} else {
				bCenter = true;
				sCaller.query("10001");
				showPromptText(this.getResources().getString(R.string.call_err_search));
			}

			dxml p = new dxml();
			dmsg req = new dmsg();
			p.setText("/params/url", "/dnake/bin/ringtone/ringback.wav");
			p.setInt("/params/mode", 1);
			req.to("/media/usb/play", p.toString());
		}
	}

	private void start() {
		if (sCaller.running != sCaller.NONE)
			return;

		boolean b = mData.matches("[0-9]*");
		if (!b) { // URL输入
			String s = mData;
			mData = "";
			mDataIdx = 0;

			if (s.startsWith("sip:")) {
				sCaller.start(s);
				showPromptText(this.getResources().getString(R.string.call_err_call));
			}
		} else {
			if (mData.length() > 0) {
				if (mData.length() <= 4) {
					if (mDataIdx == 0) {
						if (sys.sip.enable == 0) {
							mBuild = sys.talk.building;
							mUnit = sys.talk.unit;
							mDataIdx = 2;
						} else {
							String s = String.format("%d%02d%04d", sys.talk.building, sys.talk.unit, Integer.valueOf(mData));
							sCaller.query(s);
							showPromptText(this.getResources().getString(R.string.call_err_search));
						}
					}

					if (mDataIdx == 2) { // 完整输入呼叫
						String s = String.format("%d%02d%04d", mBuild, mUnit, Integer.valueOf(mData));
						sCaller.query(s);

						// 延时发送600、200协议，如果是700设备可以提前收到回应
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						s = String.format("S%04d%02d%04d0", mBuild, mUnit, Integer.valueOf(mData));
						sCaller.q600(s);
						showPromptText(this.getResources().getString(R.string.call_err_search));
					}
				} else if (sys.sip.enable == 1) {
					sCaller.query(mData);
					showPromptText(this.getResources().getString(R.string.call_err_search));
				}
			}
		}
	}

	private void reset() {
		if (bCenter) {
			mData = "";
			mDataIdx = 0;
			bCenter = false;
		}
	}

	private void eCallFailed() {
		dmsg req = new dmsg();
		req.to("/media/usb/stop", null);

		dxml p = new dxml();
		p.setText("/params/url", "/dnake/bin/prompt/call_err.wav");
		req.to("/media/usb/play", p.toString());
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (bStop) {
			bStop = false;

			if (sCaller.running != sCaller.NONE) {
				showPromptText(null);
				sCaller.stop();

				dmsg req = new dmsg();
				req.to("/media/usb/stop", null);

				this.reset();
			}
			if (!this.isFinishing())
				this.finish();
		}
		if (bAuto) {
			bAuto = false;
			this.toCenter();
		}

		if (sCaller.running != sCaller.NONE)
			WakeTask.acquire();

		if (sCaller.running == sCaller.QUERY) {
			if (sCaller.timeout() >= 3000) {
				sCaller.stop();
				if (bCenter) {
					int id = Integer.valueOf(sCaller.id) + 1;
					if (id >= 10001 && id <= 10005) {
						mData = String.valueOf(id);
						sCaller.query(mData);
						showPromptText(this.getResources().getString(R.string.call_err_search));
					} else {
						showPromptText(this.getResources().getString(R.string.call_err_addr));
						this.eCallFailed();
						this.reset();
					}
				} else {
					if (sys.sip.enable > 0) {
						String s = "sip:" + mData + "@" + sys.sip.proxy.substring(4);
						sCaller.start(s);
						showPromptText(this.getResources().getString(R.string.call_err_call));
					} else
						showPromptText(this.getResources().getString(R.string.call_err_addr));
				}
			} else {
				if (talk.qResult.sip.url != null) {
					sCaller.start(talk.qResult.sip.url);
					showPromptText(this.getResources().getString(R.string.call_err_call));
				} else if (talk.qResult.d600.ip != null) {
					sCaller.s600(talk.qResult.d600.host, talk.qResult.d600.ip);
					showPromptText(this.getResources().getString(R.string.call_err_call));
				}
			}
		} else if (sCaller.running == sCaller.CALL) {
			if (sCaller.timeout() >= 15 * 1000) {
				sCaller.stop();
				dmsg req = new dmsg();
				req.to("/talk/stop", null);

				if (bCenter) {
					int id = Integer.valueOf(sCaller.id) + 1;
					if (id >= 10001 && id <= 10005) {
						mData = String.valueOf(id);
						sCaller.query(mData);
						showPromptText(this.getResources().getString(R.string.call_err_search));
					} else {
						showPromptText(this.getResources().getString(R.string.call_err_timeout));
						this.eCallFailed();
						this.reset();
					}
				} else
					showPromptText(this.getResources().getString(R.string.call_err_timeout));
			} else {
				if (talk.qResult.result == 180) { // 对方振铃
					showPromptText(null);
					sCaller.stop();
					this.reset();
				} else if (talk.qResult.result >= 400) { // 呼叫失败
					sCaller.stop();

					if (bCenter) {
						int id = Integer.valueOf(sCaller.id) + 1;
						if (id >= 10001 && id <= 10005) {
							mData = String.valueOf(id);
							sCaller.query(mData);
							showPromptText(this.getResources().getString(R.string.call_err_search));
						} else {
							showPromptText(this.getResources().getString(R.string.call_err_failed));
							this.eCallFailed();
							this.reset();
						}
					} else
						showPromptText(this.getResources().getString(R.string.call_err_failed));
				}
			}
		}
	}

	private void showPromptText(String s) {
		String ss = new String();
		ss = Data2Text();
		if (s != null)
			ss = ss + "  " + s;
		this.mText.setText(ss);
	}

	private String Data2Text() {
		String s = new String();
		if (bCenter) {
			int idx = 1;
			boolean b = sCaller.id.matches("[0-9]*");
			if (b)
				idx = Integer.valueOf(sCaller.id) - 10000;
			s = this.getResources().getString(R.string.call_btn_center) + " " + idx + " ";
		} else {
			switch (mDataIdx) {
			case 0:
				s = mData;
				break;
			case 1:
				s = String.valueOf(mBuild) + "-" + mData;
				break;
			case 2:
				if (sys.talk.unit == 0)
					s = String.valueOf(mBuild) + "-" + mData;
				else
					s = String.valueOf(mBuild) + "-" + String.valueOf(mUnit) + "-" + mData;
				break;
			}
		}
		return s;
	}

	private final class OnDeleteClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			boolean b = mData.matches("[0-9]*");
			if (!b) { // URL输入
				mData = "";
				mDataIdx = 0;
			}

			if (mData.length() > 0)
				mData = mData.substring(0, mData.length() - 1);
			if (mData.length() == 0) {
				switch (mDataIdx) {
				case 1:
					mDataIdx--;
					mData = String.valueOf(mBuild);
					break;
				case 2:
					mDataIdx--;
					mData = String.valueOf(mUnit);
					if (sys.talk.unit == 0) {
						mDataIdx--;
						mData = String.valueOf(mBuild);
					}
					break;
				}
			}
			mText.setText(Data2Text());
		}
	}

	private final class OnKeypadClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			boolean b = mData.matches("[0-9]*");
			if (!b) { // URL输入
				mData = "";
				mDataIdx = 0;
			}

			int key = (Integer) v.getTag();
			switch (mDataIdx) {
			case 0:
				if (mData.length() >= 4 && sys.sip.enable == 0) {
					mBuild = Integer.valueOf(mData);
					mData = String.valueOf(key);
					mDataIdx++;

					if (sys.talk.unit == 0) {
						mUnit = 0;
						mDataIdx++;
					}
				} else
					mData += String.valueOf(key);
				break;
			case 1:
				if (mData.length() >= 2) {
					mUnit = Integer.valueOf(mData);
					mData = String.valueOf(key);
					mDataIdx++;
				} else
					mData += String.valueOf(key);
				break;
			case 2:
				if (mData.length() < 4) {
					mData += String.valueOf(key);
				}
				break;
			}
			mText.setText(Data2Text());
		}
	}

	private void setupKeypad() {
		Button2 b;
		b = (Button2) this.findViewById(R.id.call_btn_0);
		b.setTag(0);
		b.setOnClickListener(new OnKeypadClickListener());

		for (int i = 0; i < 9; i++) {
			b = (Button2) this.findViewById(R.id.call_btn_1 + i);
			b.setTag(i + 1);
			b.setOnClickListener(new OnKeypadClickListener());
		}
	}

	private class ContactData {
		public String name;
		public String phone;
	}

	@SuppressLint("InflateParams")
	private class PhoneBookAdapter extends BaseAdapter {
		private Context ctx;
		private LayoutInflater inflater;

		public int select = -1;
		public List<ContactData> list = new LinkedList<ContactData>();

		public PhoneBookAdapter(Context context) {
			super();

			this.ctx = context;
			inflater = LayoutInflater.from(context);

			// 主副分机URL添加
			for (int i = 0; i < __slaves.MAX; i++) {
				if (talk.qResult.slaves.url[i] != null && i != sys.talk.dcode) {
					ContactData c = new ContactData();
					String s = getString(R.string.call_slave_name) + " " + String.format("%02d", i + 1);
					c.name = s;
					c.phone = talk.qResult.slaves.url[i];
					list.add(c);
				}
			}

			Cursor cur = this.ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (cur == null)
				return;
			if (cur.moveToFirst()) {
				int iIdx = cur.getColumnIndex(ContactsContract.Contacts._ID);
				int nIdx = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				do {
					ContactData c = new ContactData();
					String id = cur.getString(iIdx);
					c.name = cur.getString(nIdx);
					if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
						Cursor phones = this.ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
								null, null);
						if (phones.moveToFirst()) {
							c.phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							list.add(c);
						}
						phones.close();
					}
				} while (cur.moveToNext());
			}
			cur.close();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int idx, View view, ViewGroup arg2) {
			if (view == null)
				view = inflater.inflate(R.layout.call_pb, null);

			ContactData c = list.get(idx);
			TextView t = (TextView) view.findViewById(R.id.call_phonebook_name);
			t.setText(c.name);

			if (select == idx)
				view.setBackgroundColor(Color.GRAY);
			else
				view.setBackgroundColor(Color.TRANSPARENT);
			return view;
		}
	}
}
