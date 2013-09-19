package sobinmain;
  
import base.Core;

import weather.MinuteRenderer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
   
public class MinuteService extends Service
{    
	  
	@Override     
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		super.onStartCommand(intent, flags, id);
		tic(this);
		this.stopSelf();
		return Service.START_STICKY;
	}

	public static void tic(Context cont)
	{
		MinuteRenderer rend = new MinuteRenderer(cont);
 
		if(UniMan.isAlive(cont)) rend.renderClockWidget();
		if(GenMan.isAlive(cont) || DTMan.isAlive(cont))
		{
			String date = Core.weekdayToString(rend.getCalendar());
			GenMan.setTextLine(cont, 0, date, null);
			DTMan.setTextLine(cont, 0, date);
		}
		rend = null;
		System.gc();
	}
}
