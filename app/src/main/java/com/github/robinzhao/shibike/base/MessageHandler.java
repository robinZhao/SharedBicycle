package com.github.robinzhao.shibike.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by zhaoruibin on 2016/12/4.
 */

public class MessageHandler extends Handler {
    private static final String MSG_KEY = "msg";
    private Context ctx;
    ProgressDialog progress;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Toast.makeText(ctx, msg.getData().getString(MSG_KEY), Toast.LENGTH_SHORT).show();
    }

    public MessageHandler(Context ctx) {
        this.ctx = ctx;
    }

    public void showMsg(String s) {
        Message msg = this.obtainMessage();
        msg.getData().putString(MSG_KEY, s);
        this.sendMessage(msg);
    }

    public void showProgress(String s) {
        progress = ProgressDialog.show(ctx, "", s);
    }

    public void hideProgress() {
        if (null != this.progress) this.progress.cancel();
        this.progress = null;
    }


}
