package com.dnake.v700;

import java.io.IOException;
import java.util.Locale;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class sound {
	public static String ringing = "/dnake/bin/ringtone/ring1.wav";
	public static String ringingForMain = "/dnake/bin/ringtone/ring_main.wav";
	public static String ringingForPerson = "/dnake/bin/ringtone/ring_person.wav";
	public static String ringingForCenter = "/dnake/bin/ringtone/ring_center.wav";
	public static String ringingForWall = "/dnake/bin/ringtone/ring_wall.wav";
	public static String ringback = "/dnake/bin/ringtone/ringback.wav";

	public static String modify_success = "/dnake/bin/prompt/modify_success.wav";
	public static String modify_failed = "/dnake/bin/prompt/modify_failed.wav";
	public static String passwd_err = "/dnake/bin/prompt/passwd_err.wav";

	public static String auto_msg_prompt = "/dnake/bin/prompt/talk/auto_answer.wav";

	public static void load() {
		if (!Locale.getDefault().getCountry().equals("CN")) {
			modify_success = "/dnake/bin/prompt/en/modify_success.wav";
			modify_failed = "/dnake/bin/prompt/en/modify_failed.wav";
			passwd_err = "/dnake/bin/prompt/en/passwd_err.wav";
			auto_msg_prompt = "/dnake/bin/prompt/talk/auto_answer_en.wav";
		}
	}

	public static MediaPlayer play(String url, Boolean looping) {
		return play(url, looping, null);
	}

	public static MediaPlayer play(String url, Boolean looping, OnCompletionListener listener) {
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(url);
			mp.prepare();
			mp.setLooping(looping);
			mp.start();

			if (listener != null)
				mp.setOnCompletionListener(listener);
			else {
				mp.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer p) {
						p.stop();
						p.release();
					}
				});
			}
			return mp;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mp != null) {
			mp.stop();
			mp.release();
		}
		return null;
	}
}
