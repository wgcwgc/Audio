package com.runcom.wgcwgc.recordServer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service
{
	MediaRecorder myAutoRecorder;
	String outputFile = null;
	public Intent intent;

	@Override
	public IBinder onBind(Intent intent )
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		// Notification notification=new Notification(R.drawable.ic_launcher,
		// Notification comes , System.currentTimeMillis());
		// Intent notificationIntent=new Intent(this,MediaActivity.class);
		// PendingIntent pendingIntent=PendingIntent.getActivity(this,
		// 0,notificationIntent,0);
		// notification.setLatestEventInfo(this, "title", "content"
		// ,pendingIntent);
		// startForeground(1, notification);

		// super.onCreate();

	}

	@Override
	public void onDestroy()
	{
		myAutoRecorder.stop();
		myAutoRecorder.release();
		myAutoRecorder = null;

		Toast.makeText(getApplicationContext() ,"Recorded successfully" ,Toast.LENGTH_LONG).show();

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent , int flags , int startId )
	{

		outputFile = intent.getStringExtra("outputFile");

		myAutoRecorder = new MediaRecorder();

		// 从麦克风源进行录音
		myAutoRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		// 设置输出格式
		myAutoRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		// 设置编码格式
		myAutoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

		myAutoRecorder.setOutputFile(outputFile);

		try
		{
			myAutoRecorder.prepare();
			myAutoRecorder.start();
//			myAutoRecorder.pause();
		}
		catch(Exception e)
		{
			Log.d("LOG" ,e.toString());
			e.printStackTrace();
		}
		// start.setEnabled(false);
		// stop.setEnabled(true);
		Toast.makeText(getApplicationContext() ,"Recording started" ,Toast.LENGTH_LONG).show();

		return super.onStartCommand(intent ,flags ,startId);
	}

}
