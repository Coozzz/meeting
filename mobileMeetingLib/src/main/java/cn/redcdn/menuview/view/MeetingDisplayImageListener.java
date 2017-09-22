package cn.redcdn.menuview.view;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import cn.redcdn.util.MResource;

import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MeetingDisplayImageListener  extends SimpleImageLoadingListener {

	public static  List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

	private Context mContext;
	
  public MeetingDisplayImageListener(Context context) {
    mContext = context;
  }
	
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		if (loadedImage != null) {
			ImageView imageView = (ImageView) view;
			boolean firstDisplay = !displayedImages.contains(imageUri);
			if (firstDisplay) {
				FadeInBitmapDisplayer.animate(imageView, 500);
				displayedImages.add(imageUri);
			}
		}
		else{
	//		CustomLog.e("DisplayImageListener","没有找到图片，使用默认头像");
			ImageView imageView = (ImageView) view;
			imageView.setImageResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_person_default_icon"));
		}
	}

	
}
