package cn.redcdn.util;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import cn.redcdn.log.CustomLog;

public class SystemManger {

	
	private String TAG = "SystemManager";
	
	private WakeLock mWakeLock;
	private WakeLock mScreenOffWakeLock;
	private KeyguardLock mKeyguardLock;
	
	PowerManager mPowerManager;
	KeyguardManager mKeyguardManager;
	AudioManager mAudioManager;
	
	public SystemManger(Context context) {
		mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void acquireWakeLock() {
		if (mWakeLock == null) {
			if (!mPowerManager.isScreenOn()) {
				
				CustomLog.v(TAG, "mWakeLock == null && !mPowerManager.isScreenOn()");
				mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, 
						this.getClass().getCanonicalName());
				mWakeLock.setReferenceCounted(false);
				mWakeLock.acquire();
			} else {
				CustomLog.v(TAG, "phone screen is on, don't need to wake up!!!");
			}
				
		}else{
			CustomLog.v(TAG,  "acquireWakeLock error");
		}
	}
	
	public void releaseWakeLock() {
		if (mWakeLock != null) {
			CustomLog.v(TAG, "mWakeLock != null");
			if (mWakeLock.isHeld()) {
				mWakeLock.release();
			}
			mWakeLock = null;
		}
	}
	
	/**
	 * 关闭屏幕
	 * @author majj
	 */
	public void acquireScreenOffWakeLock() {
		if (mScreenOffWakeLock == null && mPowerManager.isScreenOn()) {
			CustomLog.v(TAG,"mScreenOffWakeLock==null && isScreenOn()");
			int PROXITIMY_SCREEN_OFF_WAKE_LOCK = 32;
			mScreenOffWakeLock = mPowerManager.newWakeLock(
					PROXITIMY_SCREEN_OFF_WAKE_LOCK, this.getClass()
							.getCanonicalName());
			mScreenOffWakeLock.setReferenceCounted(false);
			if (!mScreenOffWakeLock.isHeld()) {
				mScreenOffWakeLock.acquire();
			}
		}
	}
	
	/**
	 * 释放关屏幕锁
	 */
	public void releaseScreenOffWakeLock() {
		if (mScreenOffWakeLock != null) {
			CustomLog.v(TAG, "mScreenOffWakeLock != null");
			if (mScreenOffWakeLock.isHeld()) {
				mScreenOffWakeLock.release();
			}
			mScreenOffWakeLock = null;
		}
	}
	
	public void disableKeyguard() {
		if (mKeyguardLock == null) {
			if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
				CustomLog.v(TAG, "mKeyguardLock == null && inKeyguardRestrictedInputMode()");
				mKeyguardLock = mKeyguardManager.newKeyguardLock("unlock");
				mKeyguardLock.disableKeyguard();
			} else {
				CustomLog.v(TAG,"phone keyguard is unlock, don't need to unlock again!!!");
			}

		}	
	}
	public void reenableKeyguard() {
		if (mKeyguardLock != null) {
			CustomLog.v(TAG,"mKeyguardLock != null");
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}	
	}
	
	/**
	 * 获取或释放音频焦点，控制通话中其他应用的音乐播放问题。
	 */
	public void pauseMusic(boolean isPause) {
		if (mAudioManager == null) {
			return;
		}
		CustomLog.v(TAG,"isPause ="+isPause);
		boolean bool = false;
		if (isPause) {
			int result = mAudioManager.requestAudioFocus(null,
					AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			bool = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
		} else {
			int result = mAudioManager.abandonAudioFocus(null);
			bool = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
		}
		CustomLog.v(TAG,"is success  ="+bool);
	}
	

}
