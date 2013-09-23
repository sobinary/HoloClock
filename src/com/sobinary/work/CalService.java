package com.sobinary.work;

import java.util.Calendar;

import com.sobinary.app.BigInfoProvider;
import com.sobinary.base.Core;





import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;





public class CalService extends Service
{
	private static int ind;
	
	Context cont;
	SharedPreferences prefs;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Core.print("Hello CalService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		super.onStartCommand(intent, flags, id);
		if(ind < 0) ind = 0;
		
		this.cont = getApplicationContext();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(cont);

		try{
			setCal2(cont);
		}
		catch(Exception e){
			Core.printe(e);
		}
	
		this.stopSelf();
		return Service.START_NOT_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}
	
	void setCal2(Context cont)
	{
		final String[] EVENTS_IN = new String[] 
		{
			Instances.TITLE,
			Instances.BEGIN,
			Instances.EVENT_LOCATION
		};

		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		long nowMil = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(prefs.getString("lookahead", "7")));
		long laterMil = cal.getTimeInMillis();
		
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, nowMil);
		ContentUris.appendId(builder, laterMil);

		Core.print("URI: " + (CalendarContract.Events.CONTENT_URI).toString());
		ContentResolver cr = cont.getContentResolver();
		Cursor cursor = cr.query(
									builder.build(), 
									EVENTS_IN, 
									null, 
									null, 
									null
								);
		Core.print("Result count: " + cursor.getCount());
		final String left, right;
		final boolean euro = prefs.getBoolean("eurotime", false);
		
		if(cursor.getCount() > 0)
		{
			cursor.moveToNext();
			cursor.move(ind % cursor.getCount());
			Core.print(cursor.getString(0));
			left = cursor.getString(0).split(" ")[0];
			cal.setTimeInMillis(cursor.getLong(1));
			
			int dayDif = cal.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
			
			if(dayDif < 14)
			{  
				if(dayDif == 0)
					right = "Today at " + prettyTime(cal, euro);
				
				else if(dayDif == 1)
					right = "Tomorrow at " + prettyTime(cal, euro);
				
				else if(cal.get(Calendar.DAY_OF_WEEK) > now.get(Calendar.DAY_OF_WEEK) && dayDif < 7)
					right = "This " + Core.weekdayToString(cal);
				
				else
					right = "Next " + Core.weekdayToString(cal);
			}
			else
			{
				int inWeeks = 1 + (int)((float)dayDif / 7f);
				right = "In " + inWeeks + " weeks";
			}
			ind++;
		}
		else
		{
			left = "0 Events";
			right = "Forever Alone";  
		}
		BigInfoProvider.setTextLine(cont, 3, left, right);
		cursor.close();
	}
	
	static String prettyTime(Calendar cal, boolean euro)
	{
		if(euro){
			if(cal.get(Calendar.MINUTE) == 0)
				return cal.get(Calendar.HOUR_OF_DAY) + "h";
			else
				return cal.get(Calendar.HOUR_OF_DAY)+":"+prettyMin(cal.get(Calendar.MINUTE));
		}
		else{
			if(cal.get(Calendar.MINUTE) == 0) 
				return cal.get(Calendar.HOUR) + ((cal.get(Calendar.AM_PM)==Calendar.AM) ? "am" : "pm"); 
			else 
				return cal.get(Calendar.HOUR)+":"+prettyMin(cal.get(Calendar.MINUTE))+
					((cal.get(Calendar.AM_PM)==Calendar.AM) ? "am" : "pm");
		}
	}
	
	static String prettyMin(int min)
	{  
		if(min < 10) return min+"0";
		else return min+"";
	}
}
