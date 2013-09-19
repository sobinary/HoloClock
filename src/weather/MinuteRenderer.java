package weather;

import java.util.Calendar;

import sobinmain.DTMan;
import sobinmain.GenMan;
import sobinmain.UniMan;

import com.sobinary.clockplus.R;

import base.Core;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

public class MinuteRenderer 
{
	private float BIG_CIRC_STROKE_WIDTH = 4.0f;
	private float SML_CIRC_STROKE_WIDTH = 4.0f;
	private float GAUGE_TEXT_SIZE = 19f;
	private float HOUR_DOT_RAD = 4.0f;
	private float MINUTE_DOT_RAD = 4.0f;
	
	private int minute, hour;
	private float minuteRdn, hourRdn, smallRad, bigRad, gaugeRad, d;
	private boolean isVisibleBat, isVisibleWeath;
	
	private Bitmap bmp;
	private Canvas can;
	private final Paint paint;
	
	private final Context cont;
	private final SharedPreferences prefs;
	private final Calendar now;
	

	
	
	
	
	
	
	
	
	/*******************************PUBLIC INIT API**************************************/
	
	
	
	
	public MinuteRenderer(Context cont)
	{
		this.cont = cont;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(cont);
		this.now = Calendar.getInstance();
		
		this.paint = new Paint();
		reloadState();
		initParams();
	}

	public void initParams()
	{
		this.minute = now.get(Calendar.MINUTE);
		this.hour = now.get(Calendar.HOUR);
		this.minuteRdn = (float)Math.toRadians((((float)minute / 60f) * 360f) - 90);
		this.hourRdn = (float)Math.toRadians(((float)hour/12f)*360 - 90);
	}

	public void reloadState()
	{
		d = prefs.getFloat("sdens", 1.0f);
		BIG_CIRC_STROKE_WIDTH *= d;
		SML_CIRC_STROKE_WIDTH *= d;
		GAUGE_TEXT_SIZE *= d;
		HOUR_DOT_RAD *= d;
		MINUTE_DOT_RAD *= d;
		
		smallRad = Float.parseFloat( Core.bleach(prefs.getString("ssize", "60")) ) * d;
		bigRad = Float.parseFloat( Core.bleach(prefs.getString("lsize", "220")) ) * d;
		
		isVisibleBat =  prefs.getBoolean("showbat", true);
		isVisibleWeath =  prefs.getBoolean("showweather", false);
		
		gaugeRad = Float.parseFloat(prefs.getString("smallPerc", "50f")) / 100f * bigRad;
		gaugeRad -= BIG_CIRC_STROKE_WIDTH;
		
		paint.setColor(Color.parseColor( prefs.getString("clcol", "#ffffffff")));
	}

	public Calendar getCalendar()
	{
		return this.now;
	}


	
	
	
	
	
	
	
	
	
	/***************************************DRAWING*****************************************/
	

	
	
	
	
	
	
	
	
	
	
	public void renderInfoWidget()
	{
		this.bmp = Bitmap.createBitmap((int)smallRad, (int)smallRad, Config.ARGB_8888);
		this.can = new Canvas(bmp);
		this.can.drawColor(0, Mode.CLEAR);

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(SML_CIRC_STROKE_WIDTH);
		can.drawCircle(smallRad, smallRad, smallRad, paint);

		float minArmLen = smallRad - SML_CIRC_STROKE_WIDTH / 2;
		float houArmLen = smallRad - SML_CIRC_STROKE_WIDTH / 2;

		float xMinArmTip = smallRad + minArmLen * (float)Math.cos(minuteRdn);
		float yMinArmTip = smallRad + minArmLen * (float)Math.sin(minuteRdn);
		float xHouArmTip = smallRad + houArmLen * (float)Math.cos(hourRdn);
		float yHouArmTip = smallRad + houArmLen * (float)Math.sin(hourRdn);
		
		can.drawLine(smallRad, smallRad, xMinArmTip, yMinArmTip, paint);
		can.drawLine(smallRad, smallRad, xHouArmTip, yHouArmTip, paint);
		
		paint.setStyle(Paint.Style.FILL);
		can.drawCircle(smallRad/2, smallRad/2, 6f, paint);
		
		GenMan.setImage(cont, bmp, R.id.clockimg);
		DTMan.setImage(cont, bmp, R.id.clockimg);
	}
	
	public void renderClockWidget()
	{
		this.bmp = Bitmap.createBitmap((int)bigRad, (int)bigRad, Config.ARGB_8888);
		this.can = new Canvas(bmp);
		this.can.drawColor(0, Mode.CLEAR);
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(BIG_CIRC_STROKE_WIDTH);
		
		paint.setStyle(Paint.Style.FILL);
		can.drawColor(Color.BLACK);
		
		drawTime();
//		drawBattery();
//		drawWeather();
		UniMan.setImage(cont, bmp);
	}

	private void drawTime()
	{
		float xGauge = bigRad + (bigRad - gaugeRad) * (float)Math.cos(hourRdn);
		float yGauge = bigRad + (bigRad - gaugeRad) * (float)Math.sin(hourRdn);
		
		float xHourArmTip = xGauge - gaugeRad * (float)Math.cos(hourRdn);
		float yHourArmTip = xGauge - gaugeRad * (float)Math.sin(hourRdn);
				
		float minuteArmLen = gaugeRad - MINUTE_DOT_RAD;
		float xMinuteArmTip = xGauge + minuteArmLen * (float)Math.cos(minuteRdn);
		float yMinuteArmTip = xGauge + minuteArmLen * (float)Math.sin(minuteRdn);

		can.drawCircle(bigRad, bigRad, bigRad, paint);
//		can.drawCircle(xGauge, yGauge, gaugeRad, paint);
//		can.drawLine(bigRad, bigRad, xHourArmTip, yHourArmTip, paint);
//		can.drawLine(xGauge, yGauge, xMinuteArmTip, yMinuteArmTip, paint);
//		
//		paint.setStyle(Paint.Style.FILL);
//		can.drawCircle(xMinuteArmTip, yMinuteArmTip, MINUTE_DOT_RAD, paint);
//		can.drawCircle(xGauge, yGauge, MINUTE_DOT_RAD, paint);
//		can.drawCircle(bigRad, bigRad, HOUR_DOT_RAD, paint);
	}
	
	private void drawWeather()
	{
		if(!isVisibleWeath) return;
		String txt; float endAngle;
		
		long last = prefs.getLong("lastweather", 0l);
		long now = System.currentTimeMillis();
		long interval = Long.parseLong(Core.bleach(prefs.getString("weatherinterval", "60l"))) * 60 * 1000;

		if(now - last < interval)
		{
			txt = prefs.getString("lastweathval", "Err");
			endAngle = prefs.getFloat("lastweathrat", 180f);
			drawCircle(endAngle, false, txt);
		}
		else
		{
			float[]data = WeatherService.getQuickLook(cont);
			
			if(data != null)
			{
				txt = (int)data[0] + "°";
				endAngle = data[1] * 360;
				prefs.edit().putString("lastweathval", txt).commit();
				prefs.edit().putFloat("lastweathrat", endAngle).commit();
				prefs.edit().putLong("lastweather", now).commit();
			}
			else drawCircle(180, false, "?°");
		}
	}
	
	private void drawBattery()
	{
		if(!isVisibleBat) return;
		String txt; float endAngle;
		
		try
		{
			Intent tent = cont.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			float nom = tent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			float denom = tent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			endAngle = (nom/denom) * 360;
			int pretty = (int)((nom/(denom/100f)));
			txt = pretty + "%";
			drawCircle(endAngle, true, txt);
		}
		catch(Exception e)
		{
			Core.print("Battery fail");
			drawCircle(endAngle = 180, true, txt = "...");
		}
	}

	
	private void drawCircle(float dashAng, boolean hor, String text)
	{
		float hAngle = hor ? (float)Math.toRadians( (hour >= 6) ? 0 : 180 ) :
			(float)Math.toRadians( (hour >= 3 && hour <= 9) ? 270 : 90 );
	
		float xGauge = bigRad + (bigRad - gaugeRad) * (float)Math.cos(hAngle);
		float yGauge = bigRad + (bigRad - gaugeRad) * (float)Math.sin(hAngle);
		
		RectF frame = new RectF();      
		frame.bottom = xGauge + gaugeRad;
		frame.top = yGauge - gaugeRad;
		frame.left = xGauge - gaugeRad;
		frame.right = yGauge + gaugeRad;
		  
		paint.setStrokeWidth(BIG_CIRC_STROKE_WIDTH);
		PathEffect old = paint.getPathEffect();
		paint.setStyle(Paint.Style.STROKE);
		can.drawArc(frame, 270, dashAng, false, paint);
		paint.setPathEffect(new DashPathEffect(new float[] {4,4}, 0));
		can.drawArc(frame, 270+dashAng, 360-dashAng, false, paint);

		paint.setTextSize(GAUGE_TEXT_SIZE);
		paint.setStrokeWidth(2);
		paint.setTextAlign(Align.CENTER);
		paint.setPathEffect(old);
		paint.setStyle(Paint.Style.FILL);
		paint.setTypeface(Typeface.DEFAULT_BOLD);

		can.drawText(text, xGauge, yGauge + 13, paint);
	}	
}