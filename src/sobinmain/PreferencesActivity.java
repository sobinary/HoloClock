package sobinmain;



import java.util.ArrayList;
import java.util.List;

import base.Core;

import com.sobinary.clockplus.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity 
{	
	private ProgressDialog progDiag;
	
	@Override
	public void onCreate(Bundle sis)
	{
		super.onCreate(sis);  
		addPreferencesFromResource(R.xml.preferences);
       	startTutorial();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		Core.print("Prefs Stopped");
        MinuteService.tic(this);
	}
	
	private void startTutorial()
	{
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
		if(extras == null)
		{
			setupAppsList();
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Just One Quick Thing!");
		builder.setMessage(R.string.tutorial);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
		        Bundle extras = getIntent().getExtras();
		        if (extras != null) 
		        {
		            int mAppWidgetId = extras.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);

		            Intent resultValue = new Intent();
		            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		            setResult(RESULT_OK, resultValue);
		            finish();
		        }
				dialog.dismiss();
			}
        });
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void setupAppsList()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override
		    protected void onPreExecute() 
			{
		        progDiag = ProgressDialog.show(PreferencesActivity.this, "", "Getting apps...", true);
		        progDiag.setCancelable(false);
		        progDiag.show();
		    }
			
			@Override
			protected Void doInBackground(Void... arg0) 
			{
				populateAppsList();
				return null;
			}
			
			@Override
			protected void onPostExecute(Void arg)
			{
				progDiag.cancel();
				progDiag.dismiss();
			}
		}.execute((Void[])null);
	}
	
	private void populateAppsList()
	{
		final PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		
		ArrayList<String>_keys = new ArrayList<String>();
		ArrayList<String>_vals = new ArrayList<String>();
		
		for (ApplicationInfo pkg : packages) 
		{
			if ((pkg.flags & ApplicationInfo.FLAG_SYSTEM) !=1)
			{
				_keys.add(pkg.packageName);
				_vals.add((String) pm.getApplicationLabel(pkg));
			}
		} 

		ListPreference lp = (ListPreference)this.findPreference("launchoice");
		CharSequence[]keys = new CharSequence[_keys.size()];
		CharSequence[]vals = new CharSequence[_vals.size()];
		
		for(int i=0; i < keys.length; i ++)keys[i] = _keys.get(i);
		for(int i=0; i < vals.length; i ++)vals[i] = _vals.get(i);

		lp.setEntryValues(keys);
		lp.setEntries(vals);
	}
	
}


























