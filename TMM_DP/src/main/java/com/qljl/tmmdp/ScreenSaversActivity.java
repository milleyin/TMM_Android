package com.qljl.tmmdp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * 伪屏保测试
 * Created by luow on 2016/7/20.
 */
public class ScreenSaversActivity extends Activity {
    private static final int MSGKEY = 0x10001;

    private long exitTime = 0;

    //屏保右上角的系统时间
    private Button mTime;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_savers_layout);
        mTime = (Button) findViewById(R.id.start_screen_Saver);
        //启动线程刷新屏保界面右上角的时间
        new TimeThread().start();
    }

    public class TimeThread extends Thread
    {
        @Override
        public void run()
        {
            do
            {
                try
                {
                    //更新时间
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = MSGKEY;
                    mHandler.sendMessage(msg);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /**
     * 2秒内连续两次按下回车键退出屏保
     * 重写方法
     * @param keyCode
     * @param event
     * @return
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_ENTER)
        {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        exit();
        return super.onTouchEvent(event);
    }

    public void exit()
    {
       /* if ((System.currentTimeMillis() - exitTime) > 2000)
        {
            Toast.makeText(getApplicationContext(), "再按一次解锁键即可进入应用", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        }
        else
        {*/
            finish();
      //  }
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSGKEY:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("hh:mm", sysTime);
                    mTime.setText(sysTimeStr);
                    break;

            }
        }
    };
}
