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

public class ParticipantorList {
  private final String TAG = "ParticipantorList";
  private List<Person> mTXLList = new ArrayList<Person>();
  private static List<Person> participantorList = new ArrayList<Person>();
  private WorkHandlerThread mWorkHandlerThread = new WorkHandlerThread(
      "workThreaed");
  private Handler notifyWorkHandler;
  private DataChangedListener mDataChangedListener;
  private PersonInTxlListener mPersonInTxlListener;
  private boolean havePersonNotInTxl = false;
  private String mAccountId;

  private Handler mainHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case 0:
        if (mDataChangedListener != null) {
          mDataChangedListener.onDataChanged((List<Person>) msg.obj, null);
        }
        break;
      case 1:
        if (mPersonInTxlListener != null) {
          CustomLog.d(TAG, "检查结果：" + havePersonNotInTxl);
          mPersonInTxlListener.onDataChanged(havePersonNotInTxl);
        }
        break;
      default:
        break;
      }

    }
  };

  public ParticipantorList(String accountId) {
    mAccountId = accountId;
    mWorkHandlerThread.start();
    notifyWorkHandler = new Handler(mWorkHandlerThread.getLooper(),
        mWorkHandlerThread);
  }    public static List<Person> getParticipatorList() {	  	return participantorList;	    }

  public void setDataChangedListener(DataChangedListener dataChangedListener) {
    mDataChangedListener = dataChangedListener;
  }

  public void setPersonInTxlListener(PersonInTxlListener personInTxlListener) {
    mPersonInTxlListener = personInTxlListener;
  }

  public void participantorsAdd(List<Person> list) {
    synchronized (participantorList) {
      participantorList.clear();
      participantorList.addAll(list);
      notifyWorkHandler.sendEmptyMessage(0);
      notifyWorkHandler.sendEmptyMessage(1);
    }
  }

  public void participantorInc(Person person) {
    synchronized (participantorList) {
      if (!participantorList.contains(person)) {        CustomLog.d(TAG, "参会方中没有此人，可以添加");
        participantorList.add(person);
        notifyWorkHandler.sendEmptyMessage(0);
        notifyWorkHandler.sendEmptyMessage(1);
      }
    }
  }

  public void participantorDec(Person person) {
    synchronized (participantorList) {
      if (participantorList.contains(person)) {
        participantorList.remove(person);
        notifyWorkHandler.sendEmptyMessage(0);
        notifyWorkHandler.sendEmptyMessage(1);
      }
    }
  }

  public void notifyTxlChanged(List<Person> txlList) {
    CustomLog.d(TAG, "收到更新通讯录通知");
    mTXLList = txlList;
  }

  public void checkPersonIfInTxl() {
    notifyWorkHandler.sendEmptyMessage(1);
  }

  @SuppressLint("NewApi")
  public void release() {
    CustomLog.d(TAG, "退出线程");
    notifyWorkHandler.removeMessages(0);
    notifyWorkHandler.removeMessages(1);
    mainHandler.removeMessages(0);
    mainHandler.removeMessages(1);
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
      CustomLog.d(TAG, "收到干活的消息，遍历参会方，如果不存在与通讯录，做标记");
      switch (msg.what) {
      case 0:
        List<Person> tmpParticipantorList = null;
        synchronized (participantorList) {
          tmpParticipantorList = new ArrayList<Person>(participantorList);
        }
        for (int i = 0; i < tmpParticipantorList.size(); i++) {
          if (mTXLList.contains(tmpParticipantorList.get(i))) {
            int idx = mTXLList.indexOf(tmpParticipantorList.get(i));
            tmpParticipantorList.remove(i);
            tmpParticipantorList.add(i, mTXLList.get(idx));
            tmpParticipantorList.get(i).setInTXL(true);            CustomLog.d(TAG, "tmpParticipantorList 11111 "+tmpParticipantorList.get(i).getAccountId());
          } else {
            tmpParticipantorList.get(i).setInTXL(false);            CustomLog.d(TAG, "tmpParticipantorList 22222 "+tmpParticipantorList.get(i).getAccountId());
          }
        }        CustomLog.d(TAG, "tmpParticipantorList size "+tmpParticipantorList.size());
        Message message = Message.obtain();
        message.what = 0;
        message.obj = tmpParticipantorList;
        mainHandler.sendMessage(message);
        CustomLog.d(TAG, "干完活，通知主线程");
        return true;
      case 1:
        CustomLog.d(TAG, "检查参会方是否存在非通讯录中的人员");
        havePersonNotInTxl = false;
        List<Person> tmpParticipantorList2 = null;
        synchronized (participantorList) {
          tmpParticipantorList2 = new ArrayList<Person>(participantorList);
        }
        CustomLog.d(TAG, "通讯录长度：" + mTXLList.size());
        for (int i = 0; i < tmpParticipantorList2.size(); i++) {
          if (!mTXLList.contains(tmpParticipantorList2.get(i))) {
            CustomLog.d(TAG, "存在非通讯录参会方："
                + tmpParticipantorList2.get(i).getAccountId());
            if (!mAccountId.equals(tmpParticipantorList2.get(i).getAccountId())) {
              CustomLog.d(TAG, "存在非通讯录参会方并且非自己："
                  + tmpParticipantorList2.get(i).getAccountId());
              havePersonNotInTxl = true;
            }
          }
        }
        Message message2 = Message.obtain();
        message2.what = 1;
        message2.obj = tmpParticipantorList2;
        mainHandler.sendMessage(message2);
      default:
        break;
      }
      return true;
    }
  }

}
