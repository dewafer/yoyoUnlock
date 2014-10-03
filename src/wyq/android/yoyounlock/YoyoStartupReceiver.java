package wyq.android.yoyounlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * 这个家伙应该能处理开机自动启动。
 * 
 * @author dewafer
 * 
 */
public class YoyoStartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		// throw new UnsupportedOperationException("Not yet implemented");
		boolean autoStartup = PreferenceManager.getDefaultSharedPreferences(
				context).getBoolean(
				context.getString(R.string.auto_startup_key), true);
		if (autoStartup) {
			Intent start = new Intent(context, ShakeDetector.class);
			context.startService(start);
		}

	}

}
