package com.sobinary.clockplus.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.sobinary.clockplus.R;
import com.sobinary.clockplus.base.Core;
import com.sobinary.clockplus.base.IO;

public class SmlInfoProvider extends AppWidgetProvider
{

	@Override
	public void onEnabled(Context cont)
	{
		super.onEnabled(cont);
	}
	
	@Override
	public void onUpdate(Context cont, AppWidgetManager appMan, int[] ids)
	{
		super.onUpdate(cont, appMan, ids);
		Core.print("Generic update");
		BigInfoProvider.beginTime(cont.getApplicationContext());
	}
	
	@Override
	public void onDisabled(Context cont)
	{
		super.onDisabled(cont);
		Core.print("Provider disabled");
		BigInfoProvider.endTime(cont);
	}

	public static boolean isAlive(Context cont)
	{
		ComponentName thisWidget = new ComponentName(cont, SmlInfoProvider.class);
		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
		return (appMan.getAppWidgetIds(thisWidget).length > 0) ? true : false;
	}
	
	public static void setTextLine(Context cont, int line, String left)
	{
		RemoteViews remote = resetRem(cont);
		ComponentName thisWidget = new ComponentName(cont, SmlInfoProvider.class);
		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
		
		int color = prefs(cont).getInt("clcol", 0xffffff);
		remote.setTextViewText(R.id.line0, left);
		remote.setTextColor(R.id.line0, color);
		save(cont, left, 0);
		appMan.updateAppWidget(thisWidget, remote);
	}
	
	public static void setImage(Context cont, Bitmap bmp, int id)
	{
		RemoteViews remote = resetRem(cont);
		
		ComponentName thisWidget = new ComponentName(cont, SmlInfoProvider.class);
		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
		remote.setImageViewBitmap(id, bmp);
		appMan.updateAppWidget(thisWidget, remote);  
	}
	
	private static SharedPreferences prefs(Context cont)
	{
		return PreferenceManager.getDefaultSharedPreferences(cont);
	}
	
	private static RemoteViews resetRem(Context cont)
	{
		RemoteViews remoteV = new RemoteViews( cont.getPackageName(), R.layout.date_time );
		String[]old = load(cont);

		int color = prefs(cont).getInt("clcol", 0xffffffff);
		remoteV.setTextColor(R.id.line0, color);
		remoteV.setTextViewText(R.id.line0, old[0]);
		
		Intent prefAct = new Intent(cont, PreferencesActivity.class);
		PendingIntent prefActP = PendingIntent.getActivity( cont, 0, prefAct, 0); 
  
		String lChoice = prefs(cont).getString("launchoice", "com.android.vending");
		Intent launcher = cont.getPackageManager().getLaunchIntentForPackage(lChoice);
		PendingIntent launcherP = PendingIntent.getActivity(cont, 0, launcher, 0);

		remoteV.setOnClickPendingIntent(R.id.line0, prefActP);
		remoteV.setOnClickPendingIntent(R.id.clockimg, launcherP);
		return remoteV;
	}
	
	private static String[] load(Context cont)
	{
		String[]old = (String[])IO.load("savedtextsml", cont);
		if(old == null) 
			old = new String[]{"Preferences"};
		return old;
	}
	
	private static void save(Context cont, String str, int pos)
	{
		String[]old = load(cont);
		old[pos] = str;
		IO.save("savedtextsml", old, cont);
	}
	
}
