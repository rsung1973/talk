package com.dnake.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

@SuppressLint("ClickableViewAccessibility")
@SuppressWarnings("deprecation")
public class Button2 extends Button {

	public Button2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Bitmap bkg_bmp = null;

	private BitmapDrawable normal() {
		if (bkg_bmp != null) {
			Bitmap bm = Bitmap.createBitmap(bkg_bmp.getWidth(), bkg_bmp.getHeight(), Bitmap.Config.ARGB_8888);

			for(int x=0; x<bm.getWidth(); x++) {
				for(int y=0; y<bm.getHeight(); y++) {
					bm.setPixel(x, y, bkg_bmp.getPixel(x, y));
				}
			}
			bm.setDensity(160);
			return (new BitmapDrawable(bm));
		}
		return null;
	}

	private BitmapDrawable press() {
		if (bkg_bmp != null) {
			Bitmap bm = Bitmap.createBitmap(bkg_bmp.getWidth(), bkg_bmp.getHeight(), Bitmap.Config.ARGB_8888);

			for(int x=0; x<bm.getWidth(); x++) {
				for(int y=0; y<bm.getHeight(); y++) {
					int color = bkg_bmp.getPixel(x, y);
					int a = (color>>24) & 0xFF;
					int r = (color>>16) & 0xFF;
					int g = ((color>>8) & 0xFF);
					int b = (color & 0xFF);

					r >>= 1;
					g >>= 1;
					b >>= 1;

					color = (a<<24) | (r<<16) | (g << 8) | b;
					bm.setPixel(x, y, color);
				}
			}
			bm.setDensity(160);
			return (new BitmapDrawable(bm));
		}
		return null;
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		Bitmap bm = ((BitmapDrawable) d).getBitmap();
		bkg_bmp = Bitmap.createBitmap(bm);
		super.setBackgroundDrawable(d);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (bkg_bmp != null) {
			if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				BitmapDrawable d = this.normal();
				if (d != null)
					super.setBackgroundDrawable(d);
			} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
				BitmapDrawable d = this.press();
				if (d != null)
					super.setBackgroundDrawable(d);
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (bkg_bmp == null) {
			try {
				BitmapDrawable bd = (BitmapDrawable)this.getBackground();
				Bitmap bm = bd.getBitmap();
				bkg_bmp = Bitmap.createBitmap(bm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.onDraw(canvas);
	}
}
