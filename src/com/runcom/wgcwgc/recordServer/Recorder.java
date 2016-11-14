package com.runcom.wgcwgc.recordServer;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.runcom.wgcwgc.audio.R;

public class Recorder extends Activity
{

	// private MediaRecorder myAutoRecorder;
	private String outputFile = null;
	private ImageButton start , stop , play , share;

	@Override
	protected void onCreate(Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recorder);

		start = (ImageButton) findViewById(R.id.button1);
		stop = (ImageButton) findViewById(R.id.button2);
		play = (ImageButton) findViewById(R.id.button3);
		share = (ImageButton) findViewById(R.id.button4);

		stop.setEnabled(false);
		play.setEnabled(false);
		share.setEnabled(false);

		String name = new Random(57).toString().substring(17);
		outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myRecording_" + name + ".mp3";

		// System.out.println(outputFile.toString());

		// myAutoRecorder = new MediaRecorder();
		//
		// // 从麦克风源进行录音
		// myAutoRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		// // 设置输出格式
		// myAutoRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		// // 设置编码格式
		// myAutoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		//
		// myAutoRecorder.setOutputFile(outputFile);

	}

	public void stop(View view )
	{
		// myAutoRecorder.stop();
		// myAutoRecorder.release();
		// myAutoRecorder = null;
		// Toast.makeText(getApplicationContext() ,"Recorded successfully"
		// ,Toast.LENGTH_LONG).show();

		Intent stopIntent = new Intent(this , MyService.class);
		stopService(stopIntent);

		stop.setEnabled(false);
		play.setEnabled(true);
		share.setEnabled(true);
	}

	public void start(View view )
	{
		// try
		// {
		// myAutoRecorder.prepare();
		// myAutoRecorder.start();
		// }
		// catch(Exception e)
		// {
		// Log.d("LOG" ,e.toString());
		// e.printStackTrace();
		// }
		// Toast.makeText(getApplicationContext() ,"Recording started"
		// ,Toast.LENGTH_LONG).show();

		Intent startIntent = new Intent(this , MyService.class);
		startIntent.putExtra("outputFile" ,outputFile);
		startService(startIntent);

		start.setEnabled(false);
		stop.setEnabled(true);
	}

	public void play(View view )
	{

		MediaPlayer m = new MediaPlayer();
		try
		{
			m.setDataSource(outputFile);
			m.prepare();
			m.start();
			Toast.makeText(getApplicationContext() ,"Playing audio" ,Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
			Log.d("LOG" ,e.toString());
			e.printStackTrace();
		}
	}

	public void share(View view )
	{

		Intent intent = new Intent(Intent.ACTION_SEND);
		 intent.setType("image/*");
		// intent.setType("media/*");
//		 intent.setType("text/plain");

//		intent.setType("audio/*");
		intent.putExtra(Intent.EXTRA_SUBJECT ,"Share");
		// intent.putExtra(Intent.EXTRA_TEXT
		// ,"I have successfully share my message through my app");
		String url = outputFile.toString();
		intent.putExtra(Intent.EXTRA_STREAM ,url);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent ,"分享"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu )
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item )
	{

		return super.onOptionsItemSelected(item);
	}

}
