package wyq.android.yoyounlock;

import wyq.android.yoyounlock.ShakeDetector.LocalBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;

/**
 * 
 * 主窗口的Activity，里面有个开关按钮和一个你看不到的按钮
 * 
 * @author dewafer
 * 
 */
public class MainActivity extends Activity implements OnClickListener,
		ServiceConnection {

	protected ShakeDetector mService;
	private Switch switchBtn;
	private boolean mBound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 开关按钮，刚起来的时候先禁用掉
		switchBtn = (Switch) findViewById(R.id.shakeSwitch);
		switchBtn.setOnClickListener(this);

		// 第一次启动时，设置默认值
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to ShakeDetector
		// 当程序启动的时候，绑定ShakeDetector
		Intent intent = new Intent(this, ShakeDetector.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onClick(View v) {

		Intent service = new Intent(this, ShakeDetector.class);
		boolean isChecked = switchBtn.isChecked();
		if (isChecked) {
			// 启动ShakeDetector
			bindService(service, this, Context.BIND_AUTO_CREATE);
			startService(service);
		} else {
			// 停止ShakeDetector
			stopService(service);
			if (mBound) {
				unbindService(this);
				mBound = false;
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			// 解绑服务
			unbindService(this);
			mBound = false;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// 成功绑定服务，取得ShakeDetector并激活开关
		LocalBinder binder = (LocalBinder) service;
		mService = binder.getService();
		switchBtn.setChecked(mService.isStarted());
		mBound = true;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// 解绑服务时触发
		mBound = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String url;
		switch (item.getItemId()) {
		case R.id.itemAbout:
			url = "https://github.com/dewafer/yoyoUnlock";
			confirmFireMissiles("更多信息请访问：" + url, url);
			return true;
		case R.id.itemReportIssue:
			url = "https://github.com/dewafer/yoyoUnlock/issues";
			confirmFireMissiles("请访问这个网址报告错误：" + url, url);
			return true;
		case R.id.itemSettings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openWebPage(String url) {
		Uri webpage = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		startActivity(intent);
	}

	public void confirmFireMissiles(String message, final String url) {
		DialogFragment newFragment = new FireMissilesDialogFragment(message,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (DialogInterface.BUTTON_POSITIVE == which) {
							openWebPage(url);
						}
					}
				});
		newFragment.show(getFragmentManager(), "missiles");
	}

	public class FireMissilesDialogFragment extends DialogFragment {

		String dialogMessage;
		DialogInterface.OnClickListener listener;

		public FireMissilesDialogFragment(String dialogMessage,
				DialogInterface.OnClickListener listener) {
			this.dialogMessage = dialogMessage;
			this.listener = listener;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(dialogMessage)
					.setPositiveButton(R.string.open, listener)
					.setNegativeButton(android.R.string.cancel, listener);
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
}
