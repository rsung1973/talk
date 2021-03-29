package com.dnake.talk;

import java.util.ArrayList;

import com.dnake.logger.AviLogger;
import com.dnake.logger.JpegLogger;
import com.dnake.v700.sys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class LoggerViewLabel extends BaseLabel {
	private ArrayList<AppData> mApps = new ArrayList<AppData>();
	private GridView mGrid;
	private ImageView mJpeg;
	private SurfaceView sView;

	public static long start = 0;
	public static MediaPlayer mPlayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logger_view);

		mGrid = (GridView) findViewById(R.id.logger_view_grid);
		mGrid.setAdapter(new AppsAdapter(this, mApps));
		mGrid.setOnItemClickListener(mListener);

		mJpeg = (ImageView) this.findViewById(R.id.logger_view_jpeg);
		mJpeg.setVisibility(View.GONE);
		mJpeg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mJpeg.setVisibility(View.GONE);
			}
		});
		sView = (SurfaceView) findViewById(R.id.logger_view_avi);
		sView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mPlayer != null) {
					mPlayer.release();
					mPlayer = null;
					sView.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		this.loadLogger();
	}

	@Override
	public void onRestart() {
		super.onRestart();

		this.loadLogger();
	}

	@Override
    public void onStop() {
    	super.onStop();

		sView.setVisibility(View.INVISIBLE);
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mJpeg.getVisibility() != View.GONE) {
				mJpeg.setVisibility(View.GONE);
				return true;
			}
			if (mPlayer != null) {
				sView.setVisibility(View.INVISIBLE);
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (mPlayer != null) {
			WakeTask.acquire();
		}
	}

	@SuppressWarnings("deprecation")
	private Bitmap loadBitmap(String url) {
		int w = (int) (320 * sys.scaled);
		int h = (int) (w * 0.75);

		Bitmap b = null;
		if (url.indexOf(".jpg") > 0) {
			b = BitmapFactory.decodeFile(url);
			return this.toBmp(new BitmapDrawable(b), w, h);
		} else {
			b = ThumbnailUtils.createVideoThumbnail(url, Thumbnails.MINI_KIND);
			if (b == null) {
				b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				b.eraseColor(Color.BLACK);
			} else
				b = ThumbnailUtils.extractThumbnail(b, w, h);

			Bitmap b2 = BitmapFactory.decodeResource(getResources(), R.drawable.logger_data_play).copy(Bitmap.Config.ARGB_8888, true);

			b = Bitmap.createBitmap(b);
			Canvas canvas = new Canvas(b);
			Paint paint = new Paint();
			canvas.drawBitmap(b2, (b.getWidth() - b2.getWidth()) / 2, (b.getHeight() - b2.getHeight()) / 2, paint);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			return b;
		}
	}

	private void loadLogger() {
		for (int i = 0; i < JpegLogger.logger.size(); i++) {
			JpegLogger.data d = JpegLogger.logger.get(i);
			if (d.start == start) {
				AppData a = new AppData();
				a.bmp = this.loadBitmap(d.url);
				a.url = d.url;
				mApps.add(a);
			}
		}

		for (int i = 0; i < AviLogger.logger.size(); i++) {
			AviLogger.data d = AviLogger.logger.get(i);
			if (d.start == start) {
				AppData a = new AppData();
				a.bmp = this.loadBitmap(d.url);
				a.url = d.url;
				mApps.add(a);
			}
		}
	}

	public Bitmap toBmp(Drawable drawable, int w, int h) {
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);

		drawable.draw(canvas);
		return bitmap;
	}

	public class AppData {
		public Bitmap bmp;
		public String url;
	}

	public class AppsAdapter extends ArrayAdapter<AppData> {
		private final LayoutInflater mInflater;

		public AppsAdapter(Context context, ArrayList<AppData> apps) {
			super(context, 0, apps);
			mInflater = LayoutInflater.from(context);
		}

		public View getView(int position, View v, ViewGroup parent) {
			if (v == null)
				v = mInflater.inflate(R.layout.logger_view_bmp, parent, false);

			AppData d = mApps.get(position);
			ImageView tv = (ImageView) v;
			tv.setImageBitmap(d.bmp);
			return v;
		}

		public final int getCount() {
			return mApps.size();
		}

		public final AppData getItem(int position) {
			return mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	private OnItemClickListener mListener = new OnItemClickListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			AppData d = mApps.get(position);
			if (d.url.indexOf(".jpg") > 0) {
				Bitmap b = BitmapFactory.decodeFile(d.url);
				if (b.getWidth() == 704 && (b.getHeight() == 288 || b.getHeight() == 240)) {
					//700门口机半个D1图像，进行拉伸
					b = toBmp(new BitmapDrawable(b), b.getWidth(), 2*b.getHeight());
				}
				mJpeg.setImageBitmap(b);
				mJpeg.setVisibility(View.VISIBLE);
			} else if (mPlayer == null) {
				sView.setVisibility(View.VISIBLE);
				SurfaceHolder holder = sView.getHolder();
				mPlayer = new MediaPlayer();
				mPlayer.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer p) {
						p.release();
						mPlayer = null;
						sView.setVisibility(View.INVISIBLE);
					}
				});
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayer.setDisplay(holder);
				try{
					mPlayer.setDataSource(d.url);
					mPlayer.prepare();
					mPlayer.start();
		        }catch (Exception e){ 
		            e.printStackTrace();
		            mPlayer.release();
		            mPlayer = null;
		            sView.setVisibility(View.INVISIBLE);
		        }
			}
		}
	};
}
