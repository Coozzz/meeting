package cn.redcdn.meetinginforeport;

import java.util.Date;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cn.redcdn.datacenter.meetingmanage.ClientReportInfo;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.log.CustomLog;

/**
 * manage meeting information report
 * 
 * @author dshy1234
 * 
 */
public class InfoReportManager {

  public enum JoinMeetingStyle {
    Convene("召开", 1), InputId("输入会议号", 2), FromList("列表会议", 3), Invited("被邀请",
        4), Linked("链接", 5), Others("其他", 6);
    // 成员变量
    public String name;
    public int index;

    // 构造方法
    private JoinMeetingStyle(String name, int index) {
      this.name = name;
      this.index = index;
    }

    // 覆盖方法
    @Override
    public String toString() {
      return this.index + "_" + this.name;
    }

  }

  public final static String PCShareEvent = "cn.redcdn.PCShare";

  private static String TAG = "cn.redcdn.meetinginforeport.InfoReportManager";
  /**
   * 设备号
   */
  private static String deviceId;

  /**
   * 视讯号
   */
  private static String videoId;

  /**
   * 会议号
   */
  private static int meetingId;

  /**
   * 参会方式: 1召开 2输入会议号 3列表会议 4 被邀请 5链接 6 其他
   */
  private static JoinMeetingStyle joinStyle;

  /**
   * 参会开始时间与1970-1-1相差的秒数（UTC时区）
   */
  private static String joinStartTime;

  /**
   * 参会时长,单位秒
   */
  private static int joinTimeSpan;

  /**
   * 参会方数
   */
  private static int maxJoinNum;

  /**
   * PC主动分享次数
   */
  private static int PCShareNum;

  /**
   * 上行带宽最大值 KByte
   */
  private static int maxUpNet;

  /**
   * 上行带宽最大值 KByte
   */
  private static int maxDownNet;

  /**
   * 上行带宽最小值 KByte
   */
  private static int minUpNet;

  /**
   * 上行带宽最小值 KByte
   */
  private static int minDownNet;

  /**
   * 会议网络中断次数
   */
  private static int netBreakNum;

  /**
   * 自己发言次数
   */
  private static int selfSpeakNum;

  /**
   * 切换窗口次数
   */
  private static int changeWindowsNum;

  /**
   * 邀请次数
   */
  private static int inviteOtherNum;

  /**
   * 添加联系人次数
   */
  private static int addOtherToAddrList;

  /**
   * 丢包率
   */
  private static int packLossRate;

  /**
   * 丢帧次数
   */
  private static int frameLossNum;

  /**
   * FEC恢复成功次数
   */
  private static int fecSuccNum;

  /**
   * FEC恢复失败次数
   */
  private static int fecFailNum;

  private static BroadcastReceiver mReceiver = null;
  private static boolean mIsStarted = false;

  public static void setDeviceId(String id) {
    deviceId = id;
  }

  public static void setVideoId(String id) {
    videoId = id;
  }

  public static void startMeeting(JoinMeetingStyle style, int meetingID) {
    mIsStarted = true;
    joinStyle = style;
    meetingId = meetingID;
    joinStartTime = String.valueOf(new Date().getTime() / 1000);
    if (mReceiver == null) {
      IntentFilter mFilter = new IntentFilter();
      mFilter.addAction(PCShareEvent);
      mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
          // TODO Auto-generated method stub
          notifyPCShare();
        }
      };
    //  this.registerReceiver(mReceiver, mFilter);
    }

    resetData();
  }

  public static void setJoinNum(int num) {
    maxJoinNum = maxJoinNum > num ? maxJoinNum : num;
  }

  public static void notifyPCShare() {
    PCShareNum++;
  }

  public static void setUpNet(int num) {

    maxUpNet = num > maxUpNet ? num : maxUpNet;
    minUpNet = minUpNet <= 0 ? num : minUpNet;
    minUpNet = num < minUpNet ? num : minUpNet;
  }

  public static void setDownNet(int num) {
    maxDownNet = num > maxDownNet ? num : maxDownNet;
    minDownNet = minDownNet <= 0 ? num : minDownNet;
    minDownNet = num < minDownNet ? num : minDownNet;
  }

  public static void notifyNetBreak() {
    netBreakNum++;
  }

  public static void notifySelfSpeak() {
    selfSpeakNum++;
  }

  public static void notifyChangeWindows() {
    changeWindowsNum++;
  }

  public static void notifyInviteOther() {
    inviteOtherNum++;
  }

  public static void notifyAddOtherToAddrList() {
    addOtherToAddrList++;
  }

  public static void setPackLossRate(int num) {
    packLossRate = num;
  }

  public static void setFrameLossNum(int num) {
    frameLossNum = num;
  }

  public static void setFecSuccNum(int num) {
    fecSuccNum = num;
  }

  public static void setFecFailNum(int num) {
    fecFailNum = num;
  }

  public static void stopMeetingAndReport() {
    if (mIsStarted == false) {
      CustomLog.e(TAG, "未启动，不能汇报运维信息");
      return;
    }
    mIsStarted = false;

    long current = new Date().getTime() / 1000;
    joinTimeSpan = (int) (current - Long.parseLong(joinStartTime));
    CustomLog.i(TAG,
        "stopMeetingAndReport JsonString:" + InfoReportManager.getJsonString());
    if (mReceiver != null) {
    //  MeetingApplication.shareInstance().unregisterReceiver(mReceiver);
      mReceiver = null;
    }

    ClientReportInfo clientReportInfo = new ClientReportInfo() {
      @Override
      protected void onSuccess(ResponseEmpty responseContent) {
        CustomLog.d(TAG, "上传运维信息完成");

      }

      @Override
      protected void onFail(int statusCode, String statusInfo) {
        CustomLog.e(TAG, "上传运维信息失败 code：" + statusCode + " info " + statusInfo);

      }
    };
    clientReportInfo.submitInfo(InfoReportManager.getJsonString());
  }

  public static String getJsonString() {
    try {
      JSONObject json = new JSONObject();
      json.put("deviceId", deviceId);
      json.put("videoId", videoId);
      json.put("meetingId", meetingId);
      json.put("joinStyle", joinStyle.index);
      json.put("joinStartTime", joinStartTime);
      json.put("joinTimeSpan", joinTimeSpan);
      json.put("maxJoinNum", maxJoinNum);
      json.put("PCShareNum", PCShareNum);
      json.put("maxUpNet", maxUpNet / 1000);
      json.put("maxDownNet", maxDownNet / 1000);
      json.put("minUpNet", minUpNet / 1000);
      json.put("minDownNet", minDownNet / 1000);
      json.put("netBreakNum", netBreakNum);
      json.put("selfSpeakNum", selfSpeakNum);
      json.put("changeWindowsNum", changeWindowsNum);
      json.put("inviteOtherNum", inviteOtherNum);
      json.put("addOtherToAddrList", addOtherToAddrList);
      json.put("packLossRate", packLossRate);
      json.put("frameLossNum", frameLossNum);
      json.put("fecSuccNum", fecSuccNum);
      json.put("fecFailNum", fecFailNum);
      return json.toString();
    } catch (Exception e) {
    }
    return "";
  }

  private static void resetData() {
    addOtherToAddrList = 0;
    changeWindowsNum = 0;
    setFecFailNum(0);
    setFecSuccNum(0);
    setFrameLossNum(0);
    inviteOtherNum = 0;
    joinTimeSpan = 0;
    maxDownNet = 0;
    maxJoinNum = 0;
    maxUpNet = 0;
    minDownNet = 0;
    minUpNet = 0;
    netBreakNum = 0;
    setPackLossRate(0);
    PCShareNum = 0;
    selfSpeakNum = 0;
  }
}
