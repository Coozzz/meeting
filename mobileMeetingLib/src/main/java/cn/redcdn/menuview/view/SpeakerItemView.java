package cn.redcdn.menuview.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.redcdn.util.MResource;

public abstract class SpeakerItemView extends FrameLayout {
	private final String TAG = "SpeakerItemView";

	private Context mContext;

	private SurfaceView mSurfaceView;

	private LinearLayout mLinearLayout;

	private TextView mTextView;

	private ImageView shareDocType;

	private ImageView micType;

	private ImageView cameraType;

	private String accountId;

	private String name;

	private int speakerType;

	private int pagetype;//放大，普通

	private int viewType;//数据view，普通view

	public static final int SHARE_DOC_TYPE = 1;

	public static final int MIC_TYPE = 2;

	public static final int CAMERA_TYPE = 3;

	private int mLimitScrollWidth = 50;

	private int mLimitScrollHeight = 50;
	float downX = 0, downY = 0;
	float upX, upY;
	float x_limit = mLimitScrollWidth;
	float y_limit = mLimitScrollHeight;

	// 放大状态
	private FrameLayout.LayoutParams zeroParentLP = new FrameLayout.LayoutParams(
			1, 1);

	private FrameLayout.LayoutParams matchParentLP = new FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.MATCH_PARENT,
			FrameLayout.LayoutParams.MATCH_PARENT);

	// private FrameLayout.LayoutParams mParams=new FrameLayout.LayoutParams(1,
	// 1);
	/*
	 * private View.OnClickListener viewOnClickListener = new OnClickListener()
	 * {
	 *
	 * @Override public void onClick(View v) { viewOnClick(SpeakerItemView.this,
	 * pagetype, accountId); } };
	 */

	private View.OnTouchListener viewOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// CustomLog.e("SpeakerItemView", "onTouch" + event);

			// float y_limit = mLimitScrollHeight;
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				downX = event.getX();
				downY = event.getY();
				// 取得按下时的坐标x
				// CustomLog.e("SpeakerItemView", "ACTION_DOWN " +
				// downX+" "+downY);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				upX = event.getX(); // 取得松开时的坐标x;
				upY = event.getY();

				float x = upX - downX;

				float y = upY - downY;

				// 限制必须得划过屏幕的1/4才能算划过

				float x_abs = Math.abs(x);

				float y_abs = Math.abs(y);
				// /CustomLog.e("SpeakerItemView", "ACTION_UP upX " +
				// upX+" upY "+upY+" x "+x+" y "+y+" x_abs "+x_abs+" y_abs "+y_abs);

				if (x_abs >= y_abs) {

					// gesture left or right

					if (x > x_limit || x < -x_limit) {

						if (x > 0) {
							// CustomLog.e("SpeakerItemView", "onTouch right ");
							// right

							viewFlingPageRight();

						} else if (x <= 0) {

							// left
							// CustomLog.e("SpeakerItemView", "onTouch left ");
							viewFlingPageLeft();

						}

					} else {
						viewOnClick(SpeakerItemView.this, pagetype,viewType, accountId,upX,upY);
					}

				} else if(x_abs < y_abs){

					// gesture up or down

					if (y > y_limit || y < -y_limit) {

						if (y > 0) {

							viewFlingPageDown();

						} else if (y <= 0) {

							viewFlingPageUp();

						}

					} else {
						viewOnClick(SpeakerItemView.this, pagetype,viewType, accountId,upX,upY);
					}



				}

				return true;
			}

			return false;
		}
	};

	public SpeakerItemView(Context context) {
		super(context);
		mContext = context;
		pagetype = MultiSpeakerView.NORMAL_TYPE;
		viewType = MultiSpeakerView.NORMAL_PAGE;
		this.setLayoutParams(zeroParentLP);
	}

	public void createView(SurfaceView surfaceView, String nube, String name,
						   int speakerType, DisplayMetrics metrics,boolean isShowName) {
		this.mSurfaceView = surfaceView;
		this.accountId = nube;
		this.name = name;
		this.speakerType = speakerType;
		// DisplayMetrics metric = new DisplayMetrics();
		// float
		// density=mContext.getWindowManager().getDefaultDisplay().getMetrics(metric).density;
		FrameLayout.LayoutParams matchParentLP = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		// CustomLog.e("SpeakerItemView", "mSurfaceView before" +
		// mSurfaceView.getHolder().isCreating());
		this.addView(mSurfaceView, matchParentLP);
		// CustomLog.e("SpeakerItemView", "mSurfaceView after" +
		// mSurfaceView.getHolder().isCreating());
		mLinearLayout = new LinearLayout(mContext);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

		FrameLayout.LayoutParams lllp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lllp.gravity = Gravity.CENTER_VERTICAL;
		float d = metrics.density;
		lllp.setMargins((int) (22 * d), (int) (36 * d), 0, 0);
		mLinearLayout.setLayoutParams(lllp);
		lllp.gravity = Gravity.CENTER_VERTICAL;
		FrameLayout.LayoutParams mediaViewBgLP = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		mediaViewBgLP.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
//		mediaViewBgLP.leftMargin = 10;
		mediaViewBgLP.topMargin = 15;
		this.addView(mLinearLayout, mediaViewBgLP);
		mTextView = new TextView(mContext);
		mTextView.setText(name);
		float value = metrics.scaledDensity;
		mTextView.setTextSize(32 / value);
		mLinearLayout.setBackgroundResource(MResource.getIdByName(mContext,
				MResource.DRAWABLE, "meeting_room_media_view_account_name_bg"));
		mLinearLayout.addView(mTextView);

		mSurfaceView.setOnTouchListener(viewOnTouchListener);
		if(isShowName){
			mLinearLayout.setVisibility(View.VISIBLE);
		}else{
			mLinearLayout.setVisibility(View.INVISIBLE);
		}
		// mSurfaceView.setOnClickListener(viewOnClickListener);

	}

	public void setSpeakerItemViewParams(int width, int height, int leftMargin,
										 int topMargin) {
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(1, 1);
		mParams.width = width;
		mParams.height = height;
		mParams.leftMargin = leftMargin;
		mParams.topMargin = topMargin;

		// mParams.gravity = Gravity.CENTER_VERTICAL;

		SpeakerItemView.this.setLayoutParams(mParams);
	}

	public void setSpeakerItemViewCenterParams(int width, int height,
											   int leftMargin, int topMargin) {
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(1, 1);
		mParams.width = width;
		mParams.height = height;
		mParams.leftMargin = leftMargin;
		mParams.topMargin = topMargin;

		mParams.gravity = Gravity.CENTER_VERTICAL;

		SpeakerItemView.this.setLayoutParams(mParams);
	}

	public void addImageIcon(int iconType) {
		switch (iconType) {
			case SHARE_DOC_TYPE:
				shareDocType = new ImageView(mContext);
				shareDocType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_share_doc_img"));
				LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				mLayoutParams.leftMargin = 8;
				mLayoutParams.gravity = Gravity.CENTER;
				shareDocType.setLayoutParams(mLayoutParams);
				mLinearLayout.addView(shareDocType);
				break;
			case MIC_TYPE:
				micType = new ImageView(mContext);
				LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				mParams.leftMargin = 8;
				mParams.gravity = Gravity.CENTER;
				micType.setLayoutParams(mParams);
				micType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_speaker_mic_off"));
				mLinearLayout.addView(micType);
				break;
			case CAMERA_TYPE:
				cameraType = new ImageView(mContext);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin = 8;
				params.gravity = Gravity.CENTER;
				cameraType.setLayoutParams(params);
				cameraType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_speaker_cam_off"));
				mLinearLayout.addView(cameraType);
				break;
			default:
				break;
		}
	}

	public boolean isExistImageIcon() {
		if (mLinearLayout != null && mLinearLayout.getChildCount() > 0) {
			return true;
		}
		return false;
	}

	public void removeAllImageIcon() {
		if (micType != null) {
			mLinearLayout.removeView(micType);
			micType = null;
		}
		if (shareDocType != null) {
			mLinearLayout.removeView(shareDocType);
			shareDocType = null;
		}
		if (cameraType != null) {
			mLinearLayout.removeView(cameraType);
			cameraType = null;
		}
	}

	public void removeImageIcon(int iconType) {
		switch (iconType) {
			case SHARE_DOC_TYPE:
				if (shareDocType != null) {
					mLinearLayout.removeView(shareDocType);
					shareDocType = null;
				}
				break;
			case MIC_TYPE:
				if (micType != null) {
					mLinearLayout.removeView(micType);
					micType = null;
				}
				break;
			case CAMERA_TYPE:
				if (cameraType != null) {
					mLinearLayout.removeView(cameraType);
					cameraType = null;
				}
				break;
			default:
				break;
		}
	}

	public void setName(String name) {
		this.name = name;
		mTextView.setText(name);
	}

	public String getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}

	public void setPageType(int type) {
		pagetype = type;
	}

	public void setViewType(int viewType) {
		this.viewType = viewType;
	}

	public SurfaceView getSurfaceView() {
		return mSurfaceView;
	}

	public abstract void viewOnClick(View view, int type,int viewType, String account,float x,float y);

	public abstract void viewFlingPageRight();

	public abstract void viewFlingPageLeft();

	public abstract void viewFlingPageDown();

	public abstract void viewFlingPageUp();

}
