package com.runcom.wgcwgc.audio;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint(
{ "InflateParams", "HandlerLeak" })
public class MainActivity extends Activity
{

	private ViewPager viewPager;
	private ArrayList < View > list = new ArrayList < View >();
	// 底部点的布局
	private LinearLayout pointLayout;
	// 底部的点
	private ImageView [] dots;
	// 当前选中的索引
	private int currentIndex;
	//
	@SuppressWarnings("unused")
	private boolean flag = true;
	// 自增int
	private AtomicInteger what = new AtomicInteger(0);

	@Override
	protected void onCreate(Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
		initDots();

		loopPlay();

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
		list.add(view1);
		list.add(view2);
		list.add(view3);

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
		dots = new ImageView [list.size()];
		for(int i = 0 ; i < list.size() ; i ++ )
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
		if(position < 0 || position > list.size() - 1 || currentIndex == position)
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
			return list.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container , int position )
		{
			container.addView(list.get(position));
			return list.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container , int position , Object object )
		{
			container.removeView(list.get(position));
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

					if(what.get() >= list.size() - 1)
					{
						what.getAndAdd( -3);
						// flag = false;
					}
					// if(what.get() < 1)
					// {
					// flag = true;
					// }
					// if(flag)
					// {
					// what.incrementAndGet();
					// }
					// else
					// {
					// what.decrementAndGet();
					// }

					what.incrementAndGet();
					try
					{
						Thread.sleep(3000);
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
		getMenuInflater().inflate(R.menu.main ,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item )
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
