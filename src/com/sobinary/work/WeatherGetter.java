package com.sobinary.work;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import com.sobinary.base.Core;

public class WeatherGetter 
{
    private static final String URL_WUND = "http://api.wunderground.com/api/5880406b8de33379";
	private static final String URL_LEFT = "http://api.openweathermap.org/data/2.5/";
	private static final String URL_RIGHT = "&mode=json";

	private Context cont;
	private boolean euro;
	private String coords, resp;
	
	public WeatherGetter(Context cont)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cont);
		this.cont = cont;
		this.euro = prefs.getBoolean("eurotemp", false);
		this.coords = getLocation();
	}
	
	private String getLocation()
	{
		try
		{
			Core.print("Getting device coordinates");
			LocationManager locMan = (LocationManager) cont.getSystemService(Context.LOCATION_SERVICE);
			Location netLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(netLoc == null)
			{ 
				Core.print("[GetLocation]Location null"); 
				return null; 
			}
			return "lat="+netLoc.getLatitude()+"&lon="+netLoc.getLongitude();
		}
		catch(Exception e)
		{
			Core.print("[GetLocation] General error");
			Core.printe(e);
			return null;
		}
	}

	public String[] getTempAndCond()
	{
		return this.getTempAndCond(false);
	}
	
	private String[] getTempAndCond(boolean internal)
	{
		String[]raw = getCrtConds();
		if(raw == null) return null;
		if(!internal) return new String[]{raw[1], prettyTemp(raw[0])};
		else return raw;
	}
	
	public float[]getTempAndRat()
	{
		String[]ray = getTempAndCond(true);
		if(ray == null) return null;
		float t = Float.parseFloat(ray[0]);
		
		double[]extremae = getExtremae();
		if(extremae == null) return null;
		float hi = (float)extremae[1]; 
		float lo = (float)extremae[0];
		float rat = (t - lo) / (hi - lo); 
		
		Core.print("Lo: " + lo + " Hi: " + hi + " t: " + t); 
		rat = rat > 1.0f ? 1.0f : rat;
		
		Core.print("[GetTempAndRat] Weather fetch success");
		return new float[]{t, rat};
	}
	
	private String[]getCrtConds()
	{
		try 
		{
			sendReceive("weather?" + coords, false);
			JSONObject root = new  JSONObject(resp);

			double temp = root.getJSONObject("main").getDouble("temp");
			String cond = root.getJSONArray("weather").getJSONObject(0).getString("description");
			
			Core.print("[GetTempAndCont]Success");
			return new String[]{temp+"", capitalize(cond)};
		}
		catch (Exception e) 
		{
			Core.print("[GetCrtConds]Parse error: " + e.getMessage());
			Core.printe(e);
			return null;
		}			
	}
	  
	private double[]getExtremae()
	{
		try
		{
			sendReceive("forecast/daily?cnt=2&" + coords, false);
			JSONObject block = getTodayBlock(new JSONObject(resp).getJSONArray("list"));
			block = block.getJSONObject("temp");
			return new double[]{block.getDouble("min"), block.getDouble("max")};
		}
		catch(Exception e)
		{
			Core.print("[GetExtremae]Parse Error: " + e.getMessage());
			Core.printe(e);
			return null;
		}
	}
 	
	private JSONObject getTodayBlock(JSONArray list)
	{
		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();

		try
		{
			for(int i=0; i < list.length(); i++)
			{
				cal.setTimeInMillis(list.getJSONObject(i).getLong("dt") * 1000l); 
				if(cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))
					return list.getJSONObject(i);
			}
			Core.print("[GetTodayBlock]Today not in list...");
			return null;
		}
		catch(Exception e) 
		{
			Core.print("GetTodayBlock]Error: " + e.getMessage());
			return null;
		}
	}
	
	public String[] getOutlook()
	{
		try 
		{	
			sendReceive("/hourly/q/" + coordsToWund(coords), true);
			JSONObject root = new JSONObject(resp);
			JSONArray days = root.getJSONArray("hourly_forecast");
			int first =-1, last=0, dif = 0, pop = 0, popMax = 0, popMin = 100;

			boolean preciping = false, snow = false;
			int h_offset = Integer.parseInt(days.getJSONObject(0).getJSONObject("FCTTIME").getString("hour"));
			
			for(int i=0; i < 12; i++)
			{
				JSONObject day = days.getJSONObject(i);
				pop = Integer.parseInt( day.getString("pop") );
				
				if(pop >= 20)
				{
					popMax = Core.max(pop, popMax);
					popMin = Core.min(pop, popMin);
					dif = pop - dif;
					preciping = true;
					first = (first==-1) ? i : first;
					last = i;
					snow = snow || day.getString("condition").contains("Snow");
				}
				else
				{
					if(preciping) break;
				}
			}
			
			String precipType = (snow) ? "Snow" : "Rain";
			String[]rainMsg = rainlessText();

			if(first == -1) return new String[]{rainMsg[0], rainMsg[1]}; 
			else if(first == 0 && last == 0) return new String[]{precipType, "Almost Done"};
			else if(first == 0) return new String[]{popMax + "% Until " + iToH(h_offset+last, euro, true)};
			else return new String[]{precipType, popMax+ "% at "+iToH(h_offset+first, euro, true)};			
		}
		catch(Exception e)
		{
			Core.print("[GetOutlook]Parsing error: " + e.getMessage());
			Core.printe(e);
			return null;
		}
	}	

	private  String[]rainlessText()
	{
		return new String[]{"No Rain", "In Sight"};
	}
	
	private  String iToH(int h, boolean euro, boolean post)
	{
		Core.print("In h: " + h);
		if(h == 24) return "Midnight";
		else if(h == 12) return "Noon";
		else if( euro ) return h+(post ? "h" : "");
		else
		{
			String suf = h < 12 ? "AM" : "PM";
			return h%12 + (post ? suf : "");
		}
	}

	private synchronized void sendReceive(final String urlRaw, final boolean wund)
	{
		if(!Core.isOnline(cont))
		{
			resp = null;
			return;
		}
		
		Thread net = new Thread()
		{
			@Override
			public synchronized void run()
			{
				try 
				{
					final DefaultHttpClient client = new DefaultHttpClient();
					Core.print("==> " + url(urlRaw, wund));
					final HttpGet getRequest = new HttpGet(url(urlRaw, wund));

					HttpResponse response = client.execute(getRequest);
					int statusCode = response.getStatusLine().getStatusCode();
					if( statusCode == HttpStatus.SC_OK )
					{
						InputStream in = response.getEntity().getContent();
						resp = streamToString(in);		
					}
					else 
					{
						Core.print("[S-R]Bad response code: " + statusCode);  
						resp = null;
					}
				} 
				catch (Exception e)
				{
					Core.print("[S-R]Fail General I/O");
					Core.printe(e);
					resp = null;
				}		
			}
		};
		net.start();
		try{ net.join(); }
		catch(InterruptedException e){Core.print("Interrupted!");}
	}

	private String streamToString(InputStream is)
	{
		try
		{
			BufferedReader br = new BufferedReader( new InputStreamReader(is));
			String line, whole = "";
			while ((line = br.readLine()) != null)  
				whole += line;
			return whole;
		}
		catch(Exception e)
		{
			Core.print("Error inputStream to String");
			Core.printe(e);
			return null;
		}
	}

	private String prettyTemp(String t)
	{
		return prettyTemp((int)Double.parseDouble(t));
	}
	
	private String prettyTemp(int t)
	{
		return t + (euro ? "°C" : "°F");
	}
	 
	private String url(String params, boolean wund)
	{
		if(wund)
			return URL_WUND + coordsToWund(params) + ".json";
		else
			return URL_LEFT + params + "&units=" + units() + URL_RIGHT;
	}
	
	private String coordsToWund(String raw)
	{
		return raw.replace("lat=", "").replace("&lon=", ",");
	}
	
	private String units()
	{
		return euro ? "metric" : "imperial";
	}
	
	@SuppressLint("DefaultLocale")
	public static String capitalize(String str)  
	{
		char[] chars = str.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) 
		{
			if (!found && Character.isLetter(chars[i])) 
			{
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} 
			else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') 
			{
				found = false;
			}
		}
		return String.valueOf(chars);
	}
	


}








