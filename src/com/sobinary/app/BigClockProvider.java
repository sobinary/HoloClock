package com.sobinary.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.sobinary.clockplus.R;

public class BigClockProvider extends BigInfoProvider  
{
	public static void setImage(Context cont, Bitmap bmp)
	{
		Intent prefAct = new Intent(cont, PreferencesActivity.class);
		PendingIntent prefActP = PendingIntent.getActivity( cont, 0, prefAct, 0); 

		RemoteViews remote = new RemoteViews( cont.getPackageName(), R.layout.uni_layout );
		ComponentName thisWidget = new ComponentName(cont, BigClockProvider.class);
		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
		
		remote.setImageViewBitmap(R.id.clockimg, bmp);
		remote.setOnClickPendingIntent(R.id.clockimg, prefActP);
		appMan.updateAppWidget(thisWidget, remote);  
  
		String lChoice = prefs(cont).getString("launchoice", "com.android.vending");
		Intent launcher = cont.getPackageManager().getLaunchIntentForPackage(lChoice);
		PendingIntent launcherP = PendingIntent.getActivity(cont, 0, launcher, 0);
		remote.setOnClickPendingIntent(R.id.launch, launcherP);
		appMan.updateAppWidget(thisWidget, remote);  
	}
	
	static SharedPreferences prefs(Context cont)
	{
		return PreferenceManager.getDefaultSharedPreferences(cont);
	}
	
}







