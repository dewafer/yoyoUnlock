package wyq.android.yoyounlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
		Intent start = new Intent(context, ShakeDetector.class);
		context.startService(start);

	}

}
