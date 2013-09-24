package com.sobinary.work;
  
import java.util.List;

import com.sobinary.app.BigClockProvider;
import com.sobinary.app.BigInfoProvider;
import com.sobinary.app.SmlInfoProvider;
import com.sobinary.base.Core;


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
 
		List<Class<?>>alive = BigInfoProvider.getLivingProviders(cont);
		debug(alive);
		
		if(alive.contains(BigClockProvider.class))
		{
			rend.renderClockWidget();
		}
		
		if(alive.contains(BigInfoProvider.class) || alive.contains(SmlInfoProvider.class))
		{
			rend.renderInfoWidget();
			String date = Core.weekdayToString(rend.getCalendar());
			
			if(alive.contains(BigInfoProvider.class)) 
				BigInfoProvider.setTextLine(cont, 0, date, null);

			if(alive.contains(SmlInfoProvider.class)) 
				SmlInfoProvider.setTextLine(cont, 0, date);
		}
		rend = null;
		System.gc();
	}
	
	private static void debug(List<Class<?>>provs)
	{
		String present = "";
		for(Class<?> prov : provs)present += prov.getSimpleName();
		Core.print(present);
		
		Core.print("BigInfoProvider: " + provs.contains(BigInfoProvider.class));
		Core.print("BigClockProvider: " + provs.contains(BigClockProvider.class));
		Core.print("SmlInfoProvider: " + provs.contains(SmlInfoProvider.class));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
