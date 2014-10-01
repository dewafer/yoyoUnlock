package wyq.android.yoyounlock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 感知摇一摇的服务
 * 
 * @author dewafer
 * 
 */
public class ShakeDetector extends Service implements ShakeHandler {

	private static final String TAG = "ShakeDetector";
	private ShakeListener mShakeListener;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private boolean started;

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 服务器动啦～
		if (!started) {
			started = mSensorManager.registerListener(mShakeListener, mSensor,
					SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "ShakeDetector.start:" + started);
			if (started) {

				ShakeDetectorNotification.notify(this, "唤醒已启动");

				Toast.makeText(this,
						R.string.shakeDetector_service_started_notice,
						Toast.LENGTH_SHORT).show();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeListener = new ShakeListener(this);
	}

	@Override
	public void onDestroy() {
		// 服务停止，再见！
		if (started) {
			mSensorManager.unregisterListener(mShakeListener);
			started = false;
			Log.d(TAG, "ShakeDetector.stopped(started=" + started + ")");
			Toast.makeText(getApplicationContext(),
					R.string.shakeDetector_service_stopped_notice,
					Toast.LENGTH_SHORT).show();
			ShakeDetectorNotification.cancel(this);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public boolean isStarted() {
		return started;
	}

	/**
	 * 
	 * 这个类是用来给其他对象绑定用的，参考
	 * http://developer.android.com/guide/components/bound-services.html#Binding
	 * 
	 * @author dewafer
	 * 
	 */
	class LocalBinder extends Binder {
		public ShakeDetector getService() {
			return ShakeDetector.this;
		}
	}

	/**
	 * 大爷被你摇醒了啦！
	 */
	@Override
	public void shakeDetected() {
		Log.d(TAG, "Wake UP!");
		Intent wakeUp = new Intent(this, LockAndUnlock.class);
		startService(wakeUp);
	}
}
