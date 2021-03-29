package com.dnake.talk;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sCaller;
import com.dnake.v700.sys;
import com.dnake.v700.talk;
import com.dnake.v700.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SysReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent it) {
        String a = it.getAction();
        if (a.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent = new Intent(ctx, talk.class);
            ctx.startService(intent);
        } else if (a.equals("com.dnake.broadcast")) {
            String e = it.getStringExtra("event");
            if (e.equals("com.dnake.boot"))
                talk.broadcast();
            else if (e.equals("com.dnake.talk.smute")) {
                sys.mute = it.getIntExtra("data", 0);
                talk.mMuteTs = System.currentTimeMillis();
                talk.mBroadcast();
            } else if (e.equals("com.dnake.talk.apk.upgrade")) {
                String url = it.getStringExtra("url");
                String name = it.getStringExtra("name");
                int mode = it.getIntExtra("mode", 0);
                dxml p = new dxml();
                dmsg req = new dmsg();
                p.setText("/params/url", url);
                p.setText("/params/name", name);
                p.setInt("/params/mode", mode);
                req.to("/upgrade/system/apk", p.toString());
            } else if (e.equals("com.dnake.talk.access.setup")) {
                dxml p = new dxml();
                dmsg req = new dmsg();
                p.setText("/event/broadcast_url", "/access/auth/setup");
                p.setInt("/event/auth", it.getIntExtra("auth", 0));
                p.setInt("/event/build", sys.talk.building);
                p.setInt("/event/unit", sys.talk.unit);
                p.setInt("/event/floor", sys.talk.floor);
                p.setInt("/event/family", sys.talk.family);
                req.to("/talk/broadcast/data", p.toString());
            } else if (e.equals("com.dnake.talk.access.sync")) { //同步卡号
                dxml p = new dxml();
                dmsg req = new dmsg();
                p.setText("/event/broadcast_url", "/access/auth/sync");
                p.setInt("/event/build", sys.talk.building);
                p.setInt("/event/unit", sys.talk.unit);
                p.setInt("/event/floor", sys.talk.floor);
                p.setInt("/event/family", sys.talk.family);
                req.to("/talk/broadcast/data", p.toString());
            } else if (e.equals("com.dnake.talk.access.unlock")) { //开锁
                utils.exUnlock(it.getIntExtra("mode", 0), it.getIntExtra("index", 0));
            } else if (e.equals("com.dnake.talk.settime")) {
                String s = it.getStringExtra("time");
                dxml p = new dxml();
                dmsg req = new dmsg();
                p.setText("/params/time", s);
                req.to("/control/settime", p.toString());
                System.err.println("com.dnake.talk.settime: " + s);
            }
        } else if (a.equals("com.dnake.quickCall")) {
            String id = it.getStringExtra("call_id");
            if (id == null) {
                id = "1999";
            }
            Intent intent = new Intent(ctx, CallLabel.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("callID", id);
            ctx.startActivity(intent);
//			sCaller.query(id);
        } else if (a.equals("com.dnake.unlock")) {
            sCaller.unlock();
        }
    }
}
