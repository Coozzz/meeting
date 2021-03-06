package cn.redcdn.jmeetingsdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CommonUtil;

public class BaseActivity extends Activity {
  protected final String TAG = getClass().getName();
  private Dialog mLoadingDialog = null;

  
  /* 标示退出应用，在某些页面需要点击两次直接退出应用 */
  private boolean isExit = false;

  private boolean twiceToExit = false;
  private static final int MSG_EXIT = 0x00101010;

  private boolean isHandleEvent = false;
  public View.OnClickListener mbtnHandleEventListener = null;
  private static final int IsHandleMsg = 99;
 
		  
  private Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
      case MSG_EXIT:
        isExit = false;
        break;
      }
    }
  };

  
  
  @SuppressWarnings("deprecation")
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getPointerCount() > 1) {
      if (ev.getAction() != MotionEvent.ACTION_POINTER_1_DOWN
          && ev.getAction() != MotionEvent.ACTION_POINTER_1_UP
          && ev.getAction() != MotionEvent.ACTION_DOWN
          && ev.getAction() != MotionEvent.ACTION_UP
          && ev.getAction() != MotionEvent.ACTION_MOVE
          && ev.getAction() != MotionEvent.ACTION_CANCEL) {
        return true;
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  
    CustomLog.d(TAG, "onCreate:" + this.toString());
    super.onCreate(savedInstanceState);
    
    mbtnHandleEventListener = new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isHandleEvent == true){
					System.out.println("触摸过快,返回");
					return;
				}
				else{
					 System.out.println("触摸成功,isHandleEvent = true");
					 todoClick(v.getId());
					 isHandleEvent = true;
				     Message msg = Message.obtain();
				     msg.what = IsHandleMsg;
				     msg.obj = v.getId();
				     isHandleEventhandler.sendMessageDelayed(msg, 200);
				}
			}
			  
		  };
		  
  }
  
	public  void todoClick(int i) {
		
	}
	
	Handler isHandleEventhandler = new Handler() {
		    @Override
		    public void handleMessage(Message msg) {
		      super.handleMessage(msg);
		      if (msg.what == IsHandleMsg) {
		    	  isHandleEvent = false;
		    	  System.out.println("200ms到时，isHandleEvent = false");
		      }
		    }
		  };
		  

  @Override
  protected void onStart() {
    CustomLog.d(TAG, "onStart:" + this.toString());
    super.onStart();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
  }

  @Override
  protected void onPause() {
    CustomLog.d(TAG, "onPause:" + this.toString());
    super.onPause();
  }

  @Override
  protected void onResume() {
    CustomLog.d(TAG, "onResume:" + this.toString());
    super.onResume();
  }

  @Override
  protected void onStop() {
    CustomLog.d(TAG, "onStop:" + this.toString());
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    CustomLog.d(TAG, "onDestroy:" + this.toString());
    super.onDestroy();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    CustomLog.d(TAG, "onNewIntent:" + this.toString());
    super.onNewIntent(intent);
  }

  @Override
  public void onBackPressed() {
    if (twiceToExit) {
      exit();
    } else {
      super.onBackPressed();
    }
  }

  public void allowTwiceToExit() {
    twiceToExit = true;
  }

  private void exit() {
    if (!isExit) {
      isExit = true;
//      CustomToast.show(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT);
      mHandler.sendEmptyMessageDelayed(MSG_EXIT, 2000);
    } else {
//      MeetingApplication.shareInstance().exit();
    }
  }

  protected void onMenuBtnPressed() {
    CustomLog.d(TAG, "BaseActivity::onMenuBtnPressed()");
  }

  protected void switchToMeetingRoomActivity(int meetingId, String adminId) {
    CustomLog.d(TAG,
        "BaseActivity::switchToMeetingRoomActivity() 切换到会议室页面. meetingId: "
            + meetingId + " | adminId: " + adminId);
    Intent i = new Intent();
    i.setClass(this, MeetingRoomActivity.class);
    i.putExtra(ConstConfig.MEETING_ID, meetingId);
    i.putExtra(ConstConfig.ADMIN_PHONE_ID, adminId);
    startActivity(i);
  }

  protected void showLoadingView(String message) {
    CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
    if (mLoadingDialog != null) {
      mLoadingDialog.dismiss();
    }
    mLoadingDialog = CommonUtil.createLoadingDialog(this, message);
    try {
      mLoadingDialog.show();
    } catch (Exception ex) {
      CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
    }
  }

  protected void showLoadingView(String message,
      DialogInterface.OnCancelListener listener) {
    CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
    if (mLoadingDialog != null) {
      mLoadingDialog.dismiss();
    }
    mLoadingDialog = CommonUtil.createLoadingDialog(this, message, listener);
    try {
      mLoadingDialog.show();
    } catch (Exception ex) {
      CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
    }
  }

  protected void showLoadingView(String message,
      final DialogInterface.OnCancelListener listener, boolean cancelAble) {
    CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
    if (mLoadingDialog != null) {
      mLoadingDialog.dismiss();
    }
    mLoadingDialog = CommonUtil.createLoadingDialog(this, message, listener);
    mLoadingDialog.setCancelable(cancelAble);
    mLoadingDialog.setOnKeyListener(new OnKeyListener() {

      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          listener.onCancel(dialog);
        }
        return false;
      }
    });
    try {
      mLoadingDialog.show();
    } catch (Exception ex) {
      CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
    }
  }

  protected void removeLoadingView() {
    CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");
    if (mLoadingDialog != null) {
      mLoadingDialog.dismiss();
      mLoadingDialog = null;
    }
  }

}
