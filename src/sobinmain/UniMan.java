package sobinmain;

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
import base.Core;
import com.sobinary.clockplus.R;

public class UniMan extends AppWidgetProvider  
{
	@Override
	public void onEnabled(Context cont)
	{
		super.onEnabled(cont);
		Core.print("UniMan Enabled");
	}
	
	@Override
	public void onUpdate(Context cont, AppWidgetManager appMan, int[] ids)
	{
		super.onUpdate(cont, appMan, ids);
		Core.print("UniMan Update");
		GenMan.setGlobalDims(cont);
		GenMan.beginTime(cont);
	}   
	
	@Override
	public void onDeleted (Context cont, int[] ids)
	{
		super.onDeleted(cont, ids);
		Core.print("UniMan Deleted");
	}    
	
	@Override
	public void onDisabled(Context cont)
	{
		super.onDisabled(cont);
		Core.print("UniMan Disabled");
		GenMan.endTime(cont);
	}
	
	public static boolean isAlive(Context cont)
	{
		ComponentName thisWidget = new ComponentName(cont, UniMan.class);
		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
		return (appMan.getAppWidgetIds(thisWidget).length > 0) ? true : false;
	}
	
	
	
	
	/*******************************STATIC PUB API***********************************/

	
	
	public static void setImage(Context cont, Bitmap bmp)
	{
		Intent prefAct = new Intent(cont, PreferencesActivity.class);
		PendingIntent prefActP = PendingIntent.getActivity( cont, 0, prefAct, 0); 

		RemoteViews remote = new RemoteViews( cont.getPackageName(), R.layout.uni_layout );
		ComponentName thisWidget = new ComponentName(cont, UniMan.class);
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







