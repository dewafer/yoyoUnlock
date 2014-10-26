package wyq.android.yoyounlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * 这个类是从下面这个地方抄过来的。。。
 * https://github.com/johnhiott/shake/blob/master/src/com/oolcay
 * /magicDecide/ShakeListener.java
 * 
 * @author dewafer
 * 
 */
public class ShakeListener implements SensorEventListener,
		OnSharedPreferenceChangeListener {

	private static final String TAG = "ShakeListener";

	private float[] mGravity = { 0.0f, 0.0f, 0.0f };
	private float[] mLinearAcceleration = { 0.0f, 0.0f, 0.0f };
	private GravityFilter mGravityFilter = new GravityFilter(mGravity,
			mLinearAcceleration);
	private int mNumShakes = 0;
	private long mFirstShake = 0;
	private int mThresholdAxis = -1;
	private WakeLock mWakeLock;

	private ShakeHandler mShakeInterface;
	private Context mContext;

	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int MIN_ACCELERATION_DEFAULT = 14;
	public static int MIN_ACCELERATION = MIN_ACCELERATION_DEFAULT;
	public static final long MAX_SHAKE_SEPARATION = 800; // milli
	public static final int NUM_OF_SHAKES = 4;

	public ShakeListener(ShakeHandler shakeInterface, Context context) {
		this.mShakeInterface = shakeInterface;
		this.mContext = context;
	}

	public void onStart() {
		updateMinAcceleration(mContext);
		keepWake();
	}

	public void onStop() {
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	private void keepWake() {
		boolean isKeepWake = PreferenceManager.getDefaultSharedPreferences(
				mContext).getBoolean(
				mContext.getString(R.string.keep_wake_def_key), false);

		if (isKeepWake) {
			PowerManager powerManager = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = powerManager.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, TAG);
			mWakeLock.setReferenceCounted(false);
			mWakeLock.acquire();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		mGravityFilter.removeGravity(event);

		// detect if the shake was hard enough
		if (isThreshold()) {

			long now = System.currentTimeMillis();

			if (mFirstShake == 0) {
				mFirstShake = now;
			}

			long timeBetweenShakes = now - mFirstShake;
			Log.d(TAG, "timeBetweenShakes=" + timeBetweenShakes);

			if (timeBetweenShakes > MAX_SHAKE_SEPARATION) {
				reset();
			} else {
				mNumShakes++;
				Log.d(TAG, "mNumShakes=" + mNumShakes);

				if (mNumShakes >= NUM_OF_SHAKES) {
					Log.d(TAG, "shakeDetected=" + mLinearAcceleration);
					mShakeInterface.shakeDetected();
					reset();
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// left blank
	}

	/**
	 * 探测晃动是否超过阈值
	 * 
	 * @return
	 */
	private boolean isThreshold() {
		boolean isThreshold = false;

		if ((mThresholdAxis < 0 || mThresholdAxis == X)
				&& Math.abs(mLinearAcceleration[X]) > MIN_ACCELERATION) {
			// 之前没有晃动过 或 之前是沿X轴晃动的，并且晃动值大于阈值
			mThresholdAxis = X;
			isThreshold = true;
		} else if ((mThresholdAxis < 0 || mThresholdAxis == Y)
				&& Math.abs(mLinearAcceleration[Y]) > MIN_ACCELERATION) {
			// 之前没有晃动过 或 之前是沿Y轴晃动的，并且晃动值大于阈值
			mThresholdAxis = Y;
			isThreshold = true;
		} else {
			// 不符合探测条件，清除之前晃动记录的轴
			mThresholdAxis = -1;
		}

		// debug
		if (isThreshold) {
			Log.d(TAG, "Threshold:mL" + (mThresholdAxis == X ? "x" : "y") + "="
					+ mLinearAcceleration[mThresholdAxis]);
		}

		return isThreshold;
	}

	private void reset() {
		mFirstShake = 0;
		mNumShakes = 0;
		mThresholdAxis = -1;
	}

	public static class GravityFilter {

		private float[] mGravity;
		private float[] mLinearAcceleration;

		public GravityFilter(float[] mGravity, float[] mLinearAcceleration) {
			this.mGravity = mGravity;
			this.mLinearAcceleration = mLinearAcceleration;
		}

		public GravityFilter() {
			this.mGravity = new float[] { 0.0f, 0.0f, 0.0f };
			this.mLinearAcceleration = new float[] { 0.0f, 0.0f, 0.0f };
		}

		public void removeGravity(SensorEvent event) {

			// Code below found at
			// http://developer.android.com/guide/topics/sensors/sensors_motion.html

			// the lower the alpha is, the faster the filter will effect
			// final float alpha = 0.8f;
			final float alpha = 0.7f;

			// Isolate the force of gravity with the low-pass filter.
			mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
			mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
			mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];

			// Remove the gravity contribution with the high-pass filter.
			mLinearAcceleration[0] = event.values[0] - mGravity[0];
			mLinearAcceleration[1] = event.values[1] - mGravity[1];
			mLinearAcceleration[2] = event.values[2] - mGravity[2];

		}

		public float[] getLinearAcceleration() {
			return mLinearAcceleration;
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		String minAccerKey = mContext
				.getString(R.string.seek_bar_preference_key);
		String useCustomKey = mContext.getString(R.string.use_custom_def_key);

		if (minAccerKey.equals(key) || useCustomKey.equals(key)) {
			updateMinAcceleration(mContext);
		}

		String keepWakeKey = mContext.getString(R.string.keep_wake_def_key);
		if (keepWakeKey.equals(key)) {
			if (sharedPreferences.getBoolean(
					mContext.getString(R.string.keep_wake_def_key), false)) {
				keepWake();
			} else {
				if (mWakeLock != null) {
					mWakeLock.release();
					mWakeLock = null;
				}
			}
		}

	}

	public static int updateMinAcceleration(Context context) {

		String useCustomKey = context.getString(R.string.use_custom_def_key);
		String key = context.getString(R.string.seek_bar_preference_key);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (preferences.getBoolean(useCustomKey, false)) {
			ShakeListener.MIN_ACCELERATION = preferences.getInt(key,
					MIN_ACCELERATION_DEFAULT);
		} else {
			ShakeListener.MIN_ACCELERATION = ShakeListener.MIN_ACCELERATION_DEFAULT;
		}

		return ShakeListener.MIN_ACCELERATION;
	}
}