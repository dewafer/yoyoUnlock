package wyq.android.yoyounlock;

import wyq.android.yoyounlock.tool.SeekBarPreferences;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

	private OnSharedPreferenceChangeListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SettingsFragment settings = new SettingsFragment();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, settings).commit();

		listener = settings;

		// To enable the app icon as an Up button, call
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	protected void onPause() {
		super.onPause();

		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public static class SettingsFragment extends PreferenceFragment implements
			OnSharedPreferenceChangeListener {

		private PreferenceScreen preferenceScreen;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			preferenceScreen = getPreferenceScreen();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "p=" + sharedPreferences.toString() + " nv=" + key);

			SeekBarPreferences seekBarPref = (SeekBarPreferences) preferenceScreen
					.findPreference(getString(R.string.seek_bar_preference_key));
			seekBarPref.setSummary(seekBarPref.getSummary());

		}

	}

	private static final String TAG = "SettingsActivity";

}
