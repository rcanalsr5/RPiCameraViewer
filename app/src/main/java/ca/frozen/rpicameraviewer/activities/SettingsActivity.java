// Copyright © 2016-2017 Shawn Baker using the MIT License.
package ca.frozen.rpicameraviewer.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import ca.frozen.library.classes.Log;
import ca.frozen.rpicameraviewer.App;
import ca.frozen.rpicameraviewer.classes.Settings;
import ca.frozen.rpicameraviewer.classes.Source;
import ca.frozen.rpicameraviewer.classes.Utils;
import ca.frozen.rpicameraviewer.R;

public class SettingsActivity extends AppCompatActivity
{
	// public constants
	public final static int FILTERED_CAMERAS = 0;
	public final static int ALL_CAMERAS = 1;

	// local constants
	private final static int EDIT_SOURCE = 1;

	// instance variables
	private EditText cameraName;
	private Spinner showCameras;
	private EditText scanTimeout;
	private Settings settings;

	//******************************************************************************
	// onCreate
	//******************************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// configure the activity
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// initialize the logger
		Utils.initLogFile(getClass().getSimpleName());

		// get the settings
		settings = (savedInstanceState == null)
						? new Settings(Utils.getSettings())
						: (Settings) savedInstanceState.getParcelable("settings");

		// set the views
		cameraName = (EditText) findViewById(R.id.settings_camera_name);
		cameraName.setText(settings.cameraName);

		showCameras = (Spinner) findViewById(R.id.settings_show_cameras);
		showCameras.setSelection(settings.showAllCameras ? ALL_CAMERAS : FILTERED_CAMERAS);

		scanTimeout = (EditText) findViewById(R.id.settings_scan_timeout);
		scanTimeout.setText(Integer.toString(settings.scanTimeout));

		Button button = (Button) findViewById(R.id.settings_tcp_ip);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startSourceActivity(settings.rawTcpIpSource);
			}
		});

		button = (Button) findViewById(R.id.settings_http);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startSourceActivity(settings.rawHttpSource);
			}
		});

		button = (Button) findViewById(R.id.settings_multicast);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startSourceActivity(settings.rawMulticastSource);
			}
		});
	}

	//******************************************************************************
	// onSaveInstanceState
	//******************************************************************************
	@Override
	protected void onSaveInstanceState(Bundle state)
	{
		settings.cameraName = cameraName.getText().toString().trim();
		settings.showAllCameras = showCameras.getSelectedItemPosition() == ALL_CAMERAS;
		String timeout = scanTimeout.getText().toString();
		settings.scanTimeout = (timeout.length() > 0) ? Integer.parseInt(scanTimeout.getText().toString()) : Settings.DEFAULT_TIMEOUT;
		state.putParcelable("settings", settings);
		super.onSaveInstanceState(state);
	}

	//******************************************************************************
	// onActivityResult
	//******************************************************************************
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == EDIT_SOURCE)
		{
			if (resultCode == RESULT_OK)
			{
				Source source = data.getParcelableExtra(SourceActivity.SOURCE);
				switch (source.connectionType)
				{
					case RawTcpIp:
						settings.rawTcpIpSource = source;
						break;
					case RawHttp:
						settings.rawHttpSource = source;
						break;
					case RawMulticast:
						settings.rawMulticastSource = source;
						break;
				}
			}
		}
	}

	//******************************************************************************
	// onCreateOptionsMenu
	//******************************************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_save, menu);
		return true;
	}

	//******************************************************************************
	// onOptionsItemSelected
	//******************************************************************************
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		// save the camera
		if (id == R.id.action_save)
		{
			if (getAndCheckSettings())
			{
				Log.info("menu: save " + settings.toString());
				Utils.setSettings(settings);
				Utils.saveData();
				finish();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//******************************************************************************
	// getAndCheckSettings
	//******************************************************************************
	private boolean getAndCheckSettings()
	{
		// get and check the camera name
		settings.cameraName = cameraName.getText().toString().trim();
		if (settings.cameraName.isEmpty())
		{
			App.error(this, R.string.error_no_camera_name);
			return false;
		}

		// get and check the scan timeout
		String timeout = scanTimeout.getText().toString();
		settings.scanTimeout = (timeout.length() > 0) ? Integer.parseInt(scanTimeout.getText().toString()) : (Settings.MAX_TIMEOUT + 1);
		if (settings.scanTimeout < Settings.MIN_TIMEOUT || settings.scanTimeout > Settings.MAX_TIMEOUT)
		{
			App.error(this, String.format(getString(R.string.error_bad_timeout), Settings.MIN_TIMEOUT, Settings.MAX_TIMEOUT));
			return false;
		}

		// get the show all cameras flag
		settings.showAllCameras = showCameras.getSelectedItemPosition() == ALL_CAMERAS;

		// indicate success
		return true;
	}

	//******************************************************************************
	// startSourceActivity
	//******************************************************************************
	private void startSourceActivity(Source source)
	{
		Log.info("startSourceActivity: " + source.toString());
		Intent intent = new Intent(getApplicationContext(), SourceActivity.class);
		intent.putExtra(SourceActivity.SOURCE, source);
		startActivityForResult(intent, EDIT_SOURCE);
	}
}