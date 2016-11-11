package com.runcom.wgcwgc.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnCompletionListener , OnErrorListener , OnSeekBarChangeListener , OnItemClickListener , Runnable
{

	private ViewPager viewPager;
	private ArrayList < View > viewPager_list = new ArrayList < View >();
	// 底部点的布局
	private LinearLayout pointLayout;
	// 底部的点
	private ImageView [] dots;
	// 当前选中的索引
	private int currentIndex;
	//
	private boolean viewPagerFlag = true;
	// 自增int
	private AtomicInteger what = new AtomicInteger(0);

	// Audio play control

	protected static final int SEARCH_MUSIC_SUCCESS = 0;// 搜索成功标记
	private SeekBar seekBar;
	private ListView listView;
	private ImageButton btnPlay;
	private TextView tv_currTime , tv_totalTime , tv_showName;
	private List < String > list = new ArrayList < String >();
	private ProgressDialog pd; // 进度条对话框
	private MusicListAdapter ma;// 适配器
	private MediaPlayer mp;
	private int currIndex = 0;// 表示当前播放的音乐索引
	private boolean seekBarFlag = true;// 控制进度条线程标记

	// 定义当前播放器的状态
	private static final int IDLE = 0;
	private static final int PAUSE = 1;
	private static final int START = 2;
	private static final int CURR_TIME_VALUE = 1;

	private int currState = IDLE; // 当前播放器的状态
	// 定义线程池（同时只能有一个线程运行）
	ExecutorService es = Executors.newSingleThreadExecutor();

	@Override
	protected void onCreate(Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
		initDots();

		loopPlay();

		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);

		initView();

	}

	private void initView()
	{
		btnPlay = (ImageButton) findViewById(R.id.media_play);
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setOnSeekBarChangeListener(this);
		listView = (ListView) findViewById(R.id.main_listView);
		listView.setOnItemClickListener(this);
		tv_currTime = (TextView) findViewById(R.id.textView1_curr_time);
		tv_totalTime = (TextView) findViewById(R.id.textView1_total_time);
		tv_showName = (TextView) findViewById(R.id.tv_showName);
	}

	public Handler hander = new Handler()
	{
		public void handleMessage(android.os.Message msg )
		{
			switch(msg.what)
			{
				case SEARCH_MUSIC_SUCCESS:
					// 搜索音乐文件结束
					ma = new MusicListAdapter();
					listView.setAdapter(ma);
					pd.dismiss();
					break;
				case CURR_TIME_VALUE:
					// 设置当前时间
					tv_currTime.setText(msg.obj.toString());
					break;
				default:
					break;
			}
		};
	};

	// 搜索音乐文件
	private void search(File file , String [] ext )
	{
		if(file != null)
		{
			if(file.isDirectory())
			{
				// System.out.println("2:" + file.toString());
				File [] listFile = file.listFiles();
				if(listFile != null)
				{
					for(int i = 0 ; i < listFile.length ; i ++ )
					{
						search(listFile[i] ,ext);
					}
				}
			}
			else
			{
				String filename = file.getAbsolutePath();
				for(int i = 0 ; i < ext.length ; i ++ )
				{
					if(filename.endsWith(ext[i]))
					{
						// list.add(filename.substring(filename.lastIndexOf("/")));
						list.add(filename);
						// System.out.println("3:" + filename);
						break;
					}
				}
			}
		}
	}

	@SuppressLint("InflateParams")
	class MusicListAdapter extends BaseAdapter
	{

		public int getCount()
		{
			return list.size();
		}

		public Object getItem(int position )
		{
			return list.get(position);
		}

		public long getItemId(int position )
		{
			return position;
		}

		public View getView(int position , View convertView , ViewGroup parent )
		{
			if(convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.list_item ,null);
			}
			TextView tv_music_name = (TextView) convertView.findViewById(R.id.textView1_music_name);
			String name = list.get(position).toString();
			name = name.substring(name.lastIndexOf("/") + 1);
			// tv_music_name.setText(list.get(position));
			tv_music_name.setText(name);
			Toast.makeText(MainActivity.this ,"搜索完成！！！" ,Toast.LENGTH_SHORT).show();
			return convertView;
		}

	}

	private void play()
	{
		switch(currState)
		{
			case IDLE:
				start();
				break;
			case PAUSE:
				mp.pause();
				btnPlay.setImageResource(R.drawable.ic_media_play);
				currState = START;
				break;
			case START:
				mp.start();
				btnPlay.setImageResource(R.drawable.ic_media_pause);
				currState = PAUSE;
		}
	}

	// 上一首
	private void previous()
	{
		if((currIndex - 1) >= 0 && list.size() > 0)
		{
			currIndex -- ;
			start();
		}
		else
			if(list.size() <= 0)
			{
				Toast.makeText(this ,"播放列表为空" ,Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(this ,"当前已经是第一首歌曲了" ,Toast.LENGTH_SHORT).show();
			}
	}

	// 下一首
	private void next()
	{
		if(currIndex + 1 < list.size() && list.size() > 1)
		{
			currIndex ++ ;
			start();
		}
		else
			if(currIndex == list.size())
			{
				Toast.makeText(this ,"播放列表为空" ,Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(this ,"当前已经是最后一首歌曲了" ,Toast.LENGTH_SHORT).show();
			}
	}

	// 开始播放
	private void start()
	{
		if(list.size() > 0 && currIndex < list.size())
		{
			String SongPath = list.get(currIndex);
			mp.reset();
			try
			{
				mp.setDataSource(SongPath);
				mp.prepare();
				mp.start();
				initSeekBar();
				es.execute(this);
				tv_showName.setText(list.get(currIndex));
				btnPlay.setImageResource(R.drawable.ic_media_pause);
				currState = PAUSE;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Toast.makeText(this ,"播放列表为空" ,Toast.LENGTH_SHORT).show();
		}
	}

	// 播放按钮
	public void play(View v )
	{
		play();
	}

	// 上一首按钮
	public void previous(View v )
	{
		previous();
	}

	// 下一首按钮
	public void next(View v )
	{
		next();
	}

	// 监听器，当当前歌曲播放完时触发，播放下一首
	public void onCompletion(MediaPlayer mp )
	{
		if(list.size() > 0)
		{
			next();
		}
		else
		{
			Toast.makeText(this ,"播放列表为空" ,Toast.LENGTH_SHORT).show();
		}
	}

	// 当播放异常时触发
	public boolean onError(MediaPlayer mp , int what , int extra )
	{
		mp.reset();
		return false;
	}

	// 初始化SeekBar
	private void initSeekBar()
	{
		seekBar.setMax(mp.getDuration());
		seekBar.setProgress(0);
		tv_totalTime.setText(toTime(mp.getDuration()));
	}

	private String toTime(int time )
	{
		int minute = time / 1000 / 60;
		int s = time / 1000 % 60;
		String mm = null;
		String ss = null;
		if(minute < 10)
			mm = "0" + minute;
		else
			mm = minute + "";

		if(s < 10)
			ss = "0" + s;
		else
			ss = "" + s;

		return mm + ":" + ss;
	}

	public void run()
	{
		seekBarFlag = true;
		while(seekBarFlag)
		{
			if(mp.getCurrentPosition() < seekBar.getMax())
			{
				seekBar.setProgress(mp.getCurrentPosition());
				Message msg = hander.obtainMessage(CURR_TIME_VALUE ,toTime(mp.getCurrentPosition()));
				hander.sendMessage(msg);
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				seekBarFlag = false;
			}
		}
	}

	// SeekBar监听器
	public void onProgressChanged(SeekBar seekBar , int progress , boolean fromUser )
	{
		// 是否由用户改变
		if(fromUser)
		{
			mp.seekTo(progress);
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar )
	{
	}

	public void onStopTrackingTouch(SeekBar seekBar )
	{
	}

	// ListView监听器
	public void onItemClick(AdapterView < ? > parent , View view , int position , long id )
	{
		currIndex = position;
		start();
	}

	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	private void init()
	{
		viewPager = (ViewPager) findViewById(R.id.main_viewPager);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view1 = inflater.inflate(R.layout.first_layout1 ,null);
		View view2 = inflater.inflate(R.layout.first_layout2 ,null);
		View view3 = inflater.inflate(R.layout.first_layout3 ,null);
		viewPager_list.add(view1);
		viewPager_list.add(view2);
		viewPager_list.add(view3);

		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int arg0 )
			{
				setDots(arg0);
				// Log.d("LOG" ,"arg0:" + arg0);
			}

			@Override
			public void onPageScrolled(int arg0 , float arg1 , int arg2 )
			{
				// Log.d("LOG" ,"arg0:" + arg0 + "\narg1:" + arg1 + "\narg2:" +
				// arg2);
			}

			@Override
			public void onPageScrollStateChanged(int arg0 )
			{
				// Log.d("LOG" ,"arg0:" + arg0);
			}
		});
	}

	/**
	 * 初始化底部的点
	 */
	private void initDots()
	{
		pointLayout = (LinearLayout) findViewById(R.id.point_layout);
		dots = new ImageView [viewPager_list.size()];
		for(int i = 0 ; i < viewPager_list.size() ; i ++ )
		{
			dots[i] = (ImageView) pointLayout.getChildAt(i);
		}
		currentIndex = 0;
		dots[currentIndex].setBackgroundResource(R.drawable.dian_down);
	}

	/**
	 * 当滚动的时候更换点的背景图
	 */
	private void setDots(int position )
	{
		if(position < 0 || position > viewPager_list.size() - 1 || currentIndex == position)
		{
			return;
		}
		dots[position].setBackgroundResource(R.drawable.dian_down);
		dots[currentIndex].setBackgroundResource(R.drawable.dian);
		currentIndex = position;
	}

	private PagerAdapter pagerAdapter = new PagerAdapter()
	{
		@Override
		public boolean isViewFromObject(View arg0 , Object arg1 )
		{
			return arg0 == arg1;
		}

		@Override
		public int getCount()
		{
			return viewPager_list.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container , int position )
		{
			container.addView(viewPager_list.get(position));
			return viewPager_list.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container , int position , Object object )
		{
			container.removeView(viewPager_list.get(position));
		}

	};

	private final Handler viewHandler = new Handler()
	{
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(android.os.Message msg )
		{
			viewPager.setCurrentItem(msg.what);
			setDots(msg.what);
		};
	};

	/**
	 * 循环播放图片
	 */
	private void loopPlay()
	{
		/**
		 * 开辟线程来控制图片轮播-左右轮播
		 */
		new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
					viewHandler.sendEmptyMessage(what.get());

					if(what.get() >= viewPager_list.size() - 1)
					{
						// what.getAndAdd( -3);
						viewPagerFlag = false;
					}
					if(what.get() < 1)
					{
						viewPagerFlag = true;
					}
					if(viewPagerFlag)
					{
						what.incrementAndGet();
					}
					else
					{
						what.decrementAndGet();
					}

					// what.incrementAndGet();
					try
					{
						Thread.sleep(2000);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main ,menu);
		// return true;
		//
		// 从.xml文件中装载菜单
		getMenuInflater().inflate(R.menu.media_menu ,menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item )
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
		// 搜索本地音乐菜单
			case R.id.item1_search:
				list.clear();
				// 是否有外部存储设备
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					pd = ProgressDialog.show(this ,"搜索" ,"正在搜索音乐文件..." ,true);
//					Toast.makeText(this ,"正在搜索音乐文件" ,Toast.LENGTH_LONG).show();
					new Thread(new Runnable()
					{
						String [] ext =
						{ ".mp3", ".wav" };
						File file = Environment.getExternalStorageDirectory();// sd卡根目录

						// File file = getExternalFilesDir(".mp3");
						// /data/data/your_package/;
						// File file = getFilesDir(); // /data/data/files/ ;
						// File file = getCacheDir();// /data/data/cache/ ;

						// File file = Environment.getRootDirectory();

						public void run()
						{
							// System.out.println("1:" + file.toString());
							search(file ,ext);
							hander.sendEmptyMessage(SEARCH_MUSIC_SUCCESS);
							// Log.d("" , "");
						}
					}).start();

				}
				else
				{
					Toast.makeText(this ,"请插入外部存储设备..." ,Toast.LENGTH_LONG).show();
					System.out.println(Environment.getExternalStorageDirectory().toString());
				}
				
				break;
			// 清除播放列表菜单
			case R.id.item2_clear:
				list.clear();
				ma.notifyDataSetChanged();
				break;
			// 退出菜单
			case R.id.item3_exit:
				seekBarFlag = false;
				this.finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
