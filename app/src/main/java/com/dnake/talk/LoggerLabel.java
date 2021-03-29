package com.dnake.talk;

import com.dnake.logger.AviLogger;
import com.dnake.logger.JpegLogger;
import com.dnake.logger.TalkLogger;
import com.dnake.v700.dmsg;
import com.dnake.v700.sCaller;
import com.dnake.v700.sys;
import com.dnake.v700.talk;
import com.dnake.v700.utils;
import com.dnake.widget.Button2;
import com.dnake.widget.Storage;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
public class LoggerLabel extends BaseLabel {
	private static int MAX = 6;
	private ImageView type[] = new ImageView[MAX];
	private TextView idx[] = new TextView[MAX];
	private TextView id[] = new TextView[MAX];
	private TextView ts[] = new TextView[MAX];
	private TextView len[] = new TextView[MAX];
	private TableRow row[] = new TableRow[MAX];
	private ImageView have[] = new ImageView[MAX];
	private int logger_idx = 0, logger_max, logger_sel = -1;

	private RelativeLayout layout_callback;
	private TextView cb_prompt;
	private Context context;

    private long reload_ts = 0;

    private static LoggerLabel __instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logger);

		context = this;
		__instance = this;

		layout_callback = (RelativeLayout) this.findViewById(R.id.logger_callback_bkg);
		layout_callback.setVisibility(RelativeLayout.GONE);
		cb_prompt = (TextView) this.findViewById(R.id.logger_callback_prompt);
		cb_prompt.setText("");

		Button2 b;
		b = (Button2) this.findViewById(R.id.logger_btn_callback);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (logger_sel != -1) {
					TalkLogger.data data = TalkLogger.logger.get(logger_idx+logger_sel);
					if (data != null) {
						sCaller.query(data.host);
						cb_prompt.setText(getResources().getString(R.string.call_err_search));
						layout_callback.setVisibility(RelativeLayout.VISIBLE);
					}
				}
			}
		});

		b = (Button2) this.findViewById(R.id.logger_btn_del);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (logger_sel != -1) {
					TalkLogger.data data = TalkLogger.logger.get(logger_idx+logger_sel);
					if (data != null)
						TalkLogger.remove(data.start);
					loadData();
				}
			}
		});

		b = (Button2) this.findViewById(R.id.logger_btn_block);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (logger_sel != -1) {
					String phoneNo = id[logger_sel].getText().toString();
					checkBlockedPhoneNumber(phoneNo,true);
				}
			}
		});


		b = (Button2) this.findViewById(R.id.logger_btn_up);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				logger_idx -= MAX;
				if (logger_idx < 0)
					logger_idx = 0;
				loadData();
			}
		});

		b = (Button2) this.findViewById(R.id.logger_btn_down);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (logger_idx + MAX < TalkLogger.logger.size())
					logger_idx += MAX;
				loadData();
			}
		});

		b = (Button2) this.findViewById(R.id.logger_btn_backup);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (logger_sel != -1) {
					TalkLogger.data data = TalkLogger.logger.get(logger_idx+logger_sel);
					if (JpegLogger.have(data.start)) {
						if (Storage.isMount(talk.mContext, Storage.extsd)) {
							int n = 0;
							for (int i = 0; i < JpegLogger.logger.size(); i++) {
								JpegLogger.data d = JpegLogger.logger.get(i);
								if (d.start == data.start) {
									String to = d.url.replace("/mnt/sdcard/Snapshot", Storage.extsd);
									utils.copyTo(d.url, to);
									n++;
								}
							}
							String s = getResources().getString(R.string.logger_backup_sd_ok);
							s = String.format(s, n);
							Toast.makeText(talk.mContext, s, Toast.LENGTH_SHORT).show();
						} else {
							String s = getResources().getString(R.string.logger_backup_sd_err);
							Toast.makeText(talk.mContext, s, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});

		for (int i = 0; i < MAX; i++) {
			row[i] = (TableRow) this.findViewById(R.id.logger_data_0 + i);
			row[i].setOnClickListener(new RowOnClickListener());

			type[i] = (ImageView) this.findViewById(R.id.logger_type_0 + i);
			idx[i] = (TextView) this.findViewById(R.id.logger_data_idx_0 + i);
			id[i] = (TextView) this.findViewById(R.id.logger_data_id_0 + i);
			ts[i] = (TextView) this.findViewById(R.id.logger_data_ts_0 + i);
			len[i] = (TextView) this.findViewById(R.id.logger_data_len_0 + i);
			have[i] = (ImageView) this.findViewById(R.id.logger_have_0 + i);
		}
		logger_idx = 0;
		loadData();

		Boolean ok = false;
		for(int i=0; i<TalkLogger.logger.size(); i++) {
			TalkLogger.data d = TalkLogger.logger.get(i);
			if (d.type == TalkLogger.CALL_MISS) {
				TalkLogger.setRead(i);
				ok = true;
			}
		}
		if (ok)
			TalkLogger.save();
	}

	public long av_ts = 0;
	private long layout_ts = 0;

	private static final String _BlockedName = "戶內機封鎖號碼";

	public static boolean isBlockedNumber(String phoneNumber) {
	    if(__instance!=null) {
            return __instance.checkBlockedPhoneNumber(phoneNumber, false);
        }
        return false;
    }

	public boolean checkBlockedPhoneNumber(String phoneNumber,boolean storeImmediately) {

		boolean result = false;
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
				null, "DISPLAY_NAME = '" + _BlockedName + "'", null, null);
		long contactId;
		if(cursor.moveToFirst()) {
			contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor cursorPhone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId
							+ " AND "
							+ ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + phoneNumber +"'",
					null,
					null);

			if (cursorPhone.moveToFirst()) {
				result = true;
			}
			cursorPhone.close();

		} else {
			ContentValues values = new ContentValues();
			values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "google.com");
			values.put(ContactsContract.RawContacts.ACCOUNT_NAME, _BlockedName);
			values.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, _BlockedName);
			Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
			long rawContactId = ContentUris.parseId(rawContactUri);

			values.clear();
			values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
			values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
			values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, _BlockedName);
			Uri contactUri = getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
			contactId = ContentUris.parseId(contactUri);
		}

		cursor.close();

		if(!result && storeImmediately) {
			ContentValues values = new ContentValues();
			values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
			values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
			getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
		}

		return result;
	}


	@SuppressLint("DefaultLocale")
	@Override
	public void onTimer() {
		super.onTimer();

		if (reload_ts != 0 && reload_ts < System.currentTimeMillis()) {
			reload_ts = 0;
			loadData();
		}
		if (sCaller.running == sCaller.QUERY) {
			if (sCaller.timeout() >= 4000) {
				if (talk.qResult.sip.proxy != 0 && sys.sip.enable != 0) {
					String s = "sip:"+sCaller.id+"@"+sys.sip.proxy.substring(4);
					sCaller.start(s);
					cb_prompt.setText(getResources().getString(R.string.call_err_call));
				} else {
					cb_prompt.setText(getResources().getString(R.string.call_err_addr));
					sCaller.stop();
					layout_ts = System.currentTimeMillis();
				}
			} else {
				if (talk.qResult.sip.url != null) {
					sCaller.start(talk.qResult.sip.url);
					cb_prompt.setText(getResources().getString(R.string.call_err_call));
				}
			}
		} else if (sCaller.running == sCaller.CALL) {
			if (sCaller.timeout() >= 15 * 1000) {
				cb_prompt.setText(getResources().getString(R.string.call_err_timeout));
				sCaller.stop();
				layout_ts = System.currentTimeMillis();

				dmsg req = new dmsg();
				req.to("/talk/stop", null);
			} else {
				if (talk.qResult.result == 180) { // �Է�����
					cb_prompt.setText("");
					sCaller.stop();
					layout_callback.setVisibility(RelativeLayout.GONE);
				} else if (talk.qResult.result >= 400) { // ����ʧ��
					cb_prompt.setText(getResources().getString(R.string.call_err_failed));
					sCaller.stop();
					layout_ts = System.currentTimeMillis();
				}
			}
		}
		if (layout_ts != 0 && Math.abs(System.currentTimeMillis()-layout_ts) >= 2*1000) {
			layout_ts = 0;
			layout_callback.setVisibility(RelativeLayout.GONE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		reload_ts = System.currentTimeMillis()+800;
	}

	@Override
    public void onStop() {
    	super.onStop();

		talk.broadcast();
	}

	@SuppressLint("DefaultLocale")
	private void loadData() {
		String s;
		if (logger_idx + MAX > TalkLogger.logger.size())
			logger_max = TalkLogger.logger.size() - logger_idx;
		else
			logger_max = MAX;
		for (int i = 0; i < MAX; i++) {
			type[i].setVisibility(View.GONE);
			idx[i].setText("");
			id[i].setText("");
			ts[i].setText("");
			len[i].setText("");
			idx[i].setTextColor(Color.WHITE);
			id[i].setTextColor(Color.WHITE);
			ts[i].setTextColor(Color.WHITE);
			len[i].setTextColor(Color.WHITE);
			have[i].setVisibility(View.GONE);
		}
		for (int i = 0; i < logger_max; i++) {
			TalkLogger.data d = TalkLogger.logger.get(logger_idx + i);

			type[i].setVisibility(ImageView.VISIBLE);
			type[i].setImageDrawable(this.getResources().getDrawable(R.drawable.logger_type_1 + d.type - 1));

			s = String.format("%d", logger_idx + i + 1);
			idx[i].setText(s);

			String name = TalkLogger.queryName(d.host, this);
			if (name == null)
				name = d.host;
			id[i].setText(name);

			SimpleDateFormat fmt = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			Date dt = new Date(d.start);
			ts[i].setText(fmt.format(dt));

			len[i].setText(String.valueOf(d.duration) + "s");
			if (JpegLogger.have(d.start) || AviLogger.have(d.start))
				have[i].setVisibility(View.VISIBLE);
		}
		logger_sel = -1;
	}

	private final class RowOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Boolean ok = false;
			for (int i = 0; i < MAX; i++) {
				idx[i].setTextColor(Color.WHITE);
				id[i].setTextColor(Color.WHITE);
				ts[i].setTextColor(Color.WHITE);
				len[i].setTextColor(Color.WHITE);
				if (logger_idx + i < TalkLogger.logger.size() && row[i].getId() == v.getId()) {
					TalkLogger.data data = TalkLogger.logger.get(logger_idx + i);
					if (logger_sel == i && (JpegLogger.have(data.start) || AviLogger.have(data.start))) {
						LoggerViewLabel.start = data.start;
						Intent intent = new Intent(context, LoggerViewLabel.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
					
					logger_sel = i;
					ok = true;

					idx[i].setTextColor(Color.BLUE);
					id[i].setTextColor(Color.BLUE);
					ts[i].setTextColor(Color.BLUE);
					len[i].setTextColor(Color.BLUE);

					if (data.read == 0) {
						TalkLogger.setRead(logger_idx+i);
						TalkLogger.save();
					}
				}
			}
			if (!ok)
				logger_sel = -1;
		}
	}

	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
}
