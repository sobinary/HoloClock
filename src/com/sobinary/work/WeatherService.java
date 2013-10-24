package com.sobinary.work;

import com.sobinary.app.BigInfoProvider;
import com.sobinary.base.Core;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class WeatherService extends Service 
{
	public static final String REQ_TYPE = "reqType"; 
	public static final int GET_NOW = 1;
	public static final int GET_OUTLOOK = 2;
	

	@Override 
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		super.onStartCommand(intent, flags, id);
		if(!Core.isOnline(getApplicationContext())) return Service.START_NOT_STICKY;
		
		int req = intent.getExtras().getInt(REQ_TYPE);
		final Context cont = getApplicationContext();
		final WeatherGetter weather = new WeatherGetter(cont);
		
		if(req == GET_NOW) 
		{
			BigInfoProvider.setTextLine(cont, 1, "...", "...");
			String[]res = weather.getTempAndCond();
			if(res != null) BigInfoProvider.setTextLine(cont, 1, res[0], res[1]);
			else BigInfoProvider.setTextLine(cont, 1, "Oops", "Try Again");
		}

		else if(req == GET_OUTLOOK) 
		{
			BigInfoProvider.setTextLine(cont, 2, "...", "...");
			String[]res = weather.getOutlook();
			if(res != null) BigInfoProvider.setTextLine(cont, 2, res[0], res[1]);
			else BigInfoProvider.setTextLine(cont, 2, "Oops", "Try Again");
		}

		else Core.print("Wrong flags dumbass");
		this.stopSelf();
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		Core.print("Goodbye Service!");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}
}
