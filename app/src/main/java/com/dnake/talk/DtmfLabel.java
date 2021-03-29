package com.dnake.talk;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class DtmfLabel {
	private AlertDialog mDialog = null;
	private TextView mDtmfView = null;
	private String mText = "";
	private View mEvent = null;

	public void start(Activity ctx, int x, int y) {
		if (mDialog != null)
			return;

		LayoutInflater inflater = ctx.getLayoutInflater();
		View layout = inflater.inflate(R.layout.talk_dtmf, (ViewGroup) ctx.findViewById(R.id.talk_dtmf_dialog));
		Builder builder = new AlertDialog.Builder(ctx, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setView(layout);

		mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);

		LayoutParams p = mDialog.getWindow().getAttributes();
		p.x = x;
		p.y = y;
		p.alpha = 0.5f;
		mDialog.getWindow().setAttributes(p);
		mDialog.hide();

		mEvent = inflater.inflate(R.layout.talk_dtmf, (ViewGroup) ctx.findViewById(R.id.talk_dtmf_dialog));
		mEvent.setVisibility(View.GONE);
		mEvent.setAlpha(0.0f);
		mEvent.setX(x);
		mEvent.setY(y);
		ctx.addContentView(mEvent, p);

		Button b;
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_0);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_1);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_2);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_3);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_4);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_5);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_6);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_7);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_8);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_9);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_A);
		b.setOnClickListener(new OnDtmfClickListener());
		b = (Button) mEvent.findViewById(R.id.talk_dtmf_B);
		b.setOnClickListener(new OnDtmfClickListener());

		mDtmfView = (TextView)layout.findViewById(R.id.talk_dtmf_text);
	}

	public void stop() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public void show() {
		if (mDialog != null) {
			mDialog.show();
			mEvent.setVisibility(View.VISIBLE);
		}
	}

	public void hide() {
		if (mDialog != null) {
			mDialog.hide();
			mEvent.setVisibility(View.GONE);
		}
	}

	private final class OnDtmfClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String dtmf = (String)v.getTag();

			dxml p = new dxml();
			dmsg req = new dmsg();
			p.setText("/params/dtmf", dtmf);
			req.to("/talk/send_dtmf", p.toString());

			if (mText.length() >= 16)
				mText = "";
			mText += dtmf;
			mDtmfView.setText(mText);
		}
	}
}
