package com.sobinary.clockplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.sobinary.clockplus.R;
import com.sobinary.clockplus.work.MinuteService;

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
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;




public class PreferencesActivity extends FragmentActivity 
{	
	@Override
	public void onCreate(Bundle sis)
	{
		super.onCreate(sis);
		
        SettingsFragment frag = new SettingsFragment();
        frag.setShouldLoad(getIntent().getExtras() == null);

        getFragmentManager().beginTransaction()
        .replace(android.R.id.content, frag)
        .commit();
        
        if(getIntent().getExtras() != null) startTutorial();
	}
  
	@Override
	public void onStop()
	{
		super.onStop();
        MinuteService.tic(this);
	}
  	
	private void startTutorial()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Just One Quick Thing!");
		builder.setMessage(R.string.tutorial);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
	            int mAppWidgetId = getIntent().getExtras().getInt( 
	            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	            setResult(RESULT_OK, resultValue);
	            finish();
				dialog.dismiss();
			}
        });
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static class SettingsFragment extends PreferenceFragment
	{
		private ProgressDialog progDiag;
		private boolean shouldLoad;
		public void onCreate(Bundle sis)
		{
			super.onCreate(sis);
			addPreferencesFromResource(R.xml.preferences);
			if(shouldLoad) setupAppsList();
		}

		public void setShouldLoad(boolean shouldLoad){
			this.shouldLoad = shouldLoad;
		}
		
		private void setupAppsList()
		{
			new AsyncTask<Void, Void, Void>()
			{
				@Override
			    protected void onPreExecute() 
				{
			        progDiag = ProgressDialog.show(getActivity(), "", "Getting apps...", true);
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
			final PackageManager pm = getActivity().getPackageManager();
			List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
			ArrayList<String> pairs = new ArrayList<String>();
			
			for (ApplicationInfo pkg : packages) 
			{
//				if ((pkg.flags & ApplicationInfo.FLAG_SYSTEM) !=1)
				{
					pairs.add(pm.getApplicationLabel(pkg) + ":" + pkg.packageName);
				}
			} 
			
			Collections.sort(pairs);
			ListPreference listPref = (ListPreference)this.findPreference("launchoice");
			CharSequence[]keys = new CharSequence[pairs.size()];
			CharSequence[]vals = new CharSequence[pairs.size()];

			for(int i=0; i < pairs.size(); i++)
			{
				String[]pair = pairs.get(i).split(":");
				vals[i] = pair[0];
				keys[i] = pair[1];
			}

			listPref.setEntryValues(keys);
			listPref.setEntries(vals);
		}
		
	}
}


























