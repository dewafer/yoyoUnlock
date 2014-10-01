package wyq.android.yoyounlock;

import java.util.Random;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 这个类负责锁屏（还未实现）或亮屏
 * 
 * @author dewafer
 * 
 */
public class LockAndUnlock extends IntentService {

	private static final String TAG = "LockAndUnlock";
	// 应该放values里的，懒得搞了这里先临时性地放一下
	private static final String[] DONT_SHAKE_MESSAGES = { "别摇了！劳资醒着呢！",
			"别摇了，没看见屏幕亮着么！", "摇你妹啊！", "摇什么摇？你以为劳资是摇摇乐么？", "别摇！我要是可乐就喷你一脸！",
			"摇也摇不出个500万", "摇出个天摇出个地，摇出个娃娃乱放屁", "摇啊摇，摇到外婆桥",
			"让我们一起摇啊摇啊摇啊摇,让这个世界从此不再有烦恼", "让我们一起摇啊摇啊摇啊摇,自由自在才是我们的目标",
			"骚年你是撸多了臂力无处释放是么？" };
	private Random random = new Random();

	private Handler mHandler = new Handler();

	public LockAndUnlock() {
		super("LockAndUnlockService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// 收到服务启动的消息...
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		Log.d(TAG, "isScreenOn:" + String.valueOf(powerManager.isScreenOn()));
		if (powerManager.isScreenOn()) {
			// 屏幕亮着的花就锁屏
			// TODO 实装休眠方法，临时性地用一个恶搞代替一下～
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(
							getApplicationContext(),
							DONT_SHAKE_MESSAGES[random
									.nextInt(DONT_SHAKE_MESSAGES.length)],
							Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			// 否则的化就唤醒
			wakeUp();
		}
	}

	/**
	 * 这个方法唤醒手机
	 */
	private void wakeUp() {

		// 这是SDK leve 17之前的唤醒方法
		// 17之后怎么唤醒？
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// 醒醒吧！娃纸！
		@SuppressWarnings("deprecation")
		WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, TAG);
		lock.acquire(0);
		Log.d(TAG, lock.toString());
	}

	// SDK 17之后FULL_WAKE_LOCK唤醒被deprecated了....
	// 但是PowerManager.wakeUp方法又是必须有系统权限才能调用
	// 所以下面这段很好的方法只能先注释掉了...
	/*
	 * @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) private void wakeUp() {
	 * PowerManager powerManager = (PowerManager)
	 * getSystemService(Context.POWER_SERVICE); if
	 * (getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.JELLY_BEAN)
	 * { long time = SystemClock.uptimeMillis(); powerManager.wakeUp(time);
	 * Log.d(TAG, "wakup:" + time); } else {
	 * 
	 * @SuppressWarnings("deprecation") WakeLock lock =
	 * powerManager.newWakeLock( PowerManager.FULL_WAKE_LOCK |
	 * PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
	 * lock.acquire(0); Log.d(TAG, lock.toString()); } }
	 */
}
