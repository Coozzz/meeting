package cn.redcdn.meeting.data;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.vo.Person;

public class InvitePersonList {
  private final String TAG = "InvitePersonList";
  private List<Person> mTXLList = new ArrayList<Person>();
  private List<Person> participantorList = new ArrayList<Person>();
  private WorkHandlerThread mWorkHandlerThread = new WorkHandlerThread(
      "workThreaed");
  private Handler notifyWorkHandler;
  private DataChangedListener mDataChangedListener;

  private Handler mainHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (mDataChangedListener != null) {
        mDataChangedListener.onDataChanged(mTXLList, (List<Person>) msg.obj);
      }
    }
  };

  public InvitePersonList() {
    mWorkHandlerThread.start();
    notifyWorkHandler = new Handler(mWorkHandlerThread.getLooper(),
        mWorkHandlerThread);
  }

  public void setDataChangedListener(DataChangedListener dataChangedListener) {
    mDataChangedListener = dataChangedListener;
  }

  public void participantorsAdd(List<Person> list) {
    synchronized (participantorList) {
      participantorList.clear();
      participantorList.addAll(list);
      notifyWorkHandler.sendEmptyMessage(0);
    }
  }

  public void participantorInc(Person person) {
    synchronized (participantorList) {
      if (!participantorList.contains(person)) {
        participantorList.add(person);
        notifyWorkHandler.sendEmptyMessage(0);
      }
    }
  }

  public void participantorDec(Person person) {
    synchronized (participantorList) {
      if (participantorList.contains(person)) {
        participantorList.remove(person);
        notifyWorkHandler.sendEmptyMessage(0);
      }
    }
  }

  public void notifyTxlChanged(List<Person> txlList) {
    mTXLList = txlList;
  }

  @SuppressLint("NewApi")
  public void release() {
    CustomLog.d(TAG, "退出线程");
    mainHandler.removeMessages(0);
    notifyWorkHandler.removeMessages(0);
    try {
      mWorkHandlerThread.getLooper().quit();
      notifyWorkHandler.getLooper().quit();
    } catch (Exception e) {
      CustomLog.d(TAG, "退出线程异常");
    }
  }

  private class WorkHandlerThread extends HandlerThread implements Callback {
    public WorkHandlerThread(String name) {
      super(name);
    }

    @Override
    public boolean handleMessage(Message msg) {
      Log.d(TAG, "收到干活的消息,遍历通讯录，减去参会方");
      List<Person> tmpParticipantorList = null;
      synchronized (participantorList) {
        tmpParticipantorList = new ArrayList<Person>(participantorList);
      }
      for (int i = 0; i < tmpParticipantorList.size(); i++) {
        if (mTXLList.contains(tmpParticipantorList.get(i))) {
          CustomLog.d(TAG, "发现存在通讯录中的参会方"
              + tmpParticipantorList.get(i).getAccountId() + "并删除.邀请人员列表长度："
              + mTXLList.size());
          mTXLList.remove(tmpParticipantorList.get(i));
          CustomLog.d(TAG, "发现存在通讯录中的参会方并删除结束.邀请人员列表长度：" + mTXLList.size());
        }
      }
      Message message = Message.obtain();
      message.what = 0;
      message.obj = tmpParticipantorList;
      mainHandler.sendMessage(message);
      Log.d(TAG, "干完活，通知主线程");
      return true;
    }
  }

}
