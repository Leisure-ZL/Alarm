package cn.edu.swu.alarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


//界面有开始，停止按钮，能输入以分钟为单位的闹钟，设定时间到了能发出提醒声音

public class MainActivity extends AppCompatActivity {

    TextView mHour,mMin,mSecond;
    EditText mEdit;
    Button mStart,mStop,mPause;
    Context context = this;

    private final int WORK_STATE_STOP = 0; // 计时状态，停止
    private final int WORK_STATE_RUN = 1;// 计时状态
    private final int WORK_STATE_OVER = 2;// 计时状态
    private Timer timer;
    private TimerTask task;
    private int userInputTime = 0;
    private int isPause = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHour = findViewById(R.id.hour);
        mMin = findViewById(R.id.min);
        mSecond = findViewById(R.id.second);
        mEdit = findViewById(R.id.edit);
        mStart = findViewById(R.id.start);
        mStop = findViewById(R.id.stop);
        mPause = findViewById(R.id.pause);

        init();

    }

    private void init() {
        mPause.setEnabled(false);
        mStart.setOnClickListener(v->{
            if(isPause != 1){
                try {
                    userInputTime =  Integer.parseInt(mEdit.getText().toString()) * 60;
                } catch (Exception e) {
                    Log.e("info", "TimerView->startTimer"+ e.getMessage());
                    Toast.makeText(this,"请输入时间",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            mStart.setEnabled(false);
            Toast.makeText(this,"开始",Toast.LENGTH_SHORT).show();
            mEdit.setText("");
            mEdit.clearFocus();
            startTimer();
            isPause = 0;
            mPause.setEnabled(true);
        });

        mPause.setOnClickListener(v->{
            mStart.setEnabled(true);
            Toast.makeText(this,"暂停",Toast.LENGTH_SHORT).show();
            stopTimer();
            isPause = 1;
        });

        mStop.setOnClickListener(v->{
            Toast.makeText(this,"停止",Toast.LENGTH_SHORT).show();
            stopTimer();
            reset();
        });

    }

    private void reset() {
        mStart.setEnabled(true);
        mHour.setText("00");
        mMin.setText("00");
        mSecond.setText("00");
        isPause = 0;
        userInputTime = 0;
        mPause.setEnabled(false);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch(msg.what){
                case WORK_STATE_RUN:
                    int hour =  userInputTime/60/60;
                    int minute = (userInputTime/60)%60;
                    int second = userInputTime%60;
                    mHour.setText(""+hour);
                    mMin.setText(""+minute);
                    mSecond.setText(""+second);
                    break;
                case WORK_STATE_STOP:
                    stopTimer();
                    break;
                case WORK_STATE_OVER:
                    alert();
                    stopTimer();
                    reset();
                    break;
            }

            return false;
        }
    });

    private void alert() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), notification);
        rt.play();
        AlertDialog.Builder dialog = new AlertDialog.Builder (MainActivity.this);
        dialog.setTitle("时间到！");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rt.stop();
            }
        });
        dialog.show();

    }

    private void startTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                userInputTime--;
                handler.sendEmptyMessage(WORK_STATE_RUN);
                if(userInputTime <=0){
                    handler.sendEmptyMessage(WORK_STATE_OVER);
                }
            }
        };
        timer.schedule(task, 1000,1000); //延迟一秒，再每隔一秒执行一次timertask.run()
    }

    private void stopTimer() {
        if(task != null){
            task.cancel();
            task = null;
        }
    }





}