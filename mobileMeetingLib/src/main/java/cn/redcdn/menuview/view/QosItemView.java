package cn.redcdn.menuview.view;

import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QosItemView extends LinearLayout {
  private Context mContext;

  private String mTitle;

  private String mContent;

  private TextView titletTv;

  private TextView contentTv;

  public QosItemView(Context context, String title, String content) {
    super(context);
    //CustomLog.d("QosItemView", "初始化");
    LayoutInflater.from(context).inflate(
        MResource.getIdByName(context, MResource.LAYOUT, "meeting_room_menu_qos_item_view"), this, true);
    mContext = context;
    mTitle = title;
    mContent = content;
    titletTv = (TextView) findViewById(MResource.getIdByName(mContext, MResource.ID, "qosInfoTitle"));
    contentTv = (TextView) findViewById(MResource.getIdByName(mContext, MResource.ID, "qosInfoTitleTextView"));
    titletTv.setText(title);
    contentTv.setText(content);
  }

}
