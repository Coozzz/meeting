package cn.redcdn.menuview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.constconfig.MediaType;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

public abstract class MultiSpeakerView extends BaseView {

	private final String TAG = "MultiSpeakerView";

	private MenuView.MultiSpeakerViewListener mMultiSpeakerViewListener;

	private int currentPosition = 0;

	private Context mContext;

	private ButelOpenSDK mButelOpenSDK;

	private MenuView mMenuView;

	private boolean videoOffAuto = false;

	// 记录自己的视讯号
	private String mAccountId;

	// 页面标记

	public static final int DATA_PAGE = 0;

	public static final int NORMAL_PAGE = 8;

	public static final int MULTI_PAGE = 2;

	public static final int SINGLE_BIG_PAGE = 6;

	// 放大状态
	public static final int ZOOM_OUT_TYPE = 3;

	public static final int NORMAL_TYPE = 4;

	private String masterAccountId;

	private String shareDocAccountId;

	private String zoomOutAccount;

	private int pageSize = 1;

	private int currentPage = MULTI_PAGE;

	private int pagetype;

	private SpeakerItemView shareDocView;

	private int width;

	private int height;

	private ImageView spareImg;

	private DisplayMetrics density;

	private List<SpeakerItemView> multiViewList = new ArrayList<SpeakerItemView>();

	private FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(1,
			1);

	public MultiSpeakerView(ButelOpenSDK butelOpenSDK, Context context,
							MenuView menuView, String accountId, DisplayMetrics density, MenuView.MultiSpeakerViewListener MultiSpeakerViewListener) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"jmeetingsdk_multi_speaker_view"));
		mContext = context;
		mButelOpenSDK = butelOpenSDK;
		mMenuView = menuView;
		mAccountId = accountId;
		zoomOutAccount = mAccountId;
		mMultiSpeakerViewListener = MultiSpeakerViewListener;
		mButelOpenSDK.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
		pagetype = NORMAL_TYPE;
		this.density = density;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		if (size.x > size.y) {
			width = size.x;
			height = size.y;
		} else {
			width = size.y;
			height = size.x;
		}
		spareImg = new ImageView(context);
		spareImg.setBackgroundResource(MResource.getIdByName(mContext,
				MResource.DRAWABLE, "meeting_room_wait_for_speak_bg"));
		spareImg.setLayoutParams(mParams);
		this.addView(spareImg);

	}

	private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
		@Override
		public void onNotify(int arg0, Object arg1) {
			Cmd respModel = null;
			switch (arg0) {
				case NotifyType.HANDLE_EXCEPTION_SUC:
					if (mButelOpenSDK != null && mButelOpenSDK.getSpeakers() != null) {
						for (int i = 0; i < mButelOpenSDK.getSpeakers().size(); i++) {
							setImageIconView(mButelOpenSDK.getSpeakers().get(i).getAccountId());
						}
					}
					break;
				case NotifyType.SPEAKER_ON_LINE:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"SPEAKER_ON_LINE "
									+" respModel.getAccountId():"+ respModel.getAccountId()
									+" respModel.getUserName():"+ respModel.getUserName());
					if(multiViewList.size()==0){
						addSpeakerView(respModel.getAccountId(),respModel.getUserName(), false);
					}else{
						addSpeakerView(respModel.getAccountId(),respModel.getUserName(), false);
					}
					break;
				case NotifyType.START_SPEAK:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"START_SPEAK " + respModel.getAccountId() + " "
									+ MeetingManager
									.getInstance().getAccountName());
					if(multiViewList.size()==0){
						addSpeakerView(respModel.getAccountId(), MeetingManager
								.getInstance().getAccountName(), true);
					}else{
						addSpeakerView(respModel.getAccountId(), MeetingManager
								.getInstance().getAccountName(), true);
					}
					break;
				case NotifyType.SPEAKER_OFF_LINE:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView", "SPEAKER_OFF_LINE "
							+" respModel.getAccountId():"+respModel.getAccountId()
							+" respModel.getUserName():"+respModel.getUserName());
					removeSpeakerView(respModel.getAccountId(),
							respModel.getUserName());
					if(multiViewList.size()>0){
						if(currentPage==DATA_PAGE){
							CustomLog.d(TAG,"DATA_PAGE,shareDocAccountId:"+shareDocAccountId+"respModel.getAccountId():"+respModel.getAccountId());
							if(shareDocAccountId.equals(respModel.getAccountId())){
								zoomOutAccount = multiViewList.get(0).getAccountId();
								currentPosition = 0;
								setPageShow(SINGLE_BIG_PAGE);
								CustomLog.d(TAG,"SPEAKER_OFF_LINE,DATA_PAGE,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
								mMultiSpeakerViewListener.update(currentPosition);
							}
						}else{
							CustomLog.d(TAG,"NOT DATA_PAGE,shareDocAccountId:"+shareDocAccountId+"respModel.getAccountId():"+respModel.getAccountId());
							if(zoomOutAccount.equals(respModel.getAccountId())){
								zoomOutAccount = multiViewList.get(0).getAccountId();
								currentPosition = 0;
								setPageShow(SINGLE_BIG_PAGE);
								CustomLog.d(TAG,"SPEAKER_OFF_LINE,NOT DATA_PAGE,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
								mMultiSpeakerViewListener.update(currentPosition);
							}
						}
					}else{
						CustomToast.show(mContext,getContext().getString(R.string.no_people_speak), Toast.LENGTH_LONG);
					}
					break;
				case NotifyType.STOP_SPEAK:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"STOP_SPEAK " + respModel.getAccountId());
					removeSpeakerView(respModel.getAccountId(), MeetingManager
							.getInstance().getAccountName());
					if(multiViewList.size()>0){
						if(currentPage==DATA_PAGE){
							CustomLog.d(TAG,"STOP_SPEAK,DATA_PAGE,shareDocAccountId:"+String.valueOf(shareDocAccountId)+" respModel.getAccountId():"+String.valueOf(respModel.getAccountId()));
							if(shareDocAccountId.equals(respModel.getAccountId())){
								zoomOutAccount = multiViewList.get(0).getAccountId();
								currentPosition = 0;
								setPageShow(SINGLE_BIG_PAGE);
								CustomLog.d(TAG,"STOP_SPEAK,DATA_PAGE,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
								mMultiSpeakerViewListener.update(currentPosition);
							}
						}else{
							if(zoomOutAccount.equals(respModel.getAccountId())){
								zoomOutAccount = multiViewList.get(0).getAccountId();
								currentPosition = 0;
								setPageShow(SINGLE_BIG_PAGE);
								CustomLog.d(TAG,"STOP_SPEAK,NOT DATA_PAGE,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
								mMultiSpeakerViewListener.update(currentPosition);
							}
						}

					}else{
						CustomToast.show(mContext,getContext().getString(R.string.no_people_speak), Toast.LENGTH_LONG);
					}
					break;
				case NotifyType.START_SHARE_DOC:
					CustomLog.d("MultiSpeakerView", "START_SHARE_DOC");
					addShareDocView(mAccountId, MeetingManager.getInstance()
							.getAccountName(), true);
					handleOpenShareDoc(mAccountId);
					break;
				case NotifyType.STOP_SHARE_DOC:
					CustomLog.d("MultiSpeakerView", "STOP_SHARE_DOC");
					removeShareDocView(mAccountId, MeetingManager.getInstance()
							.getAccountName());
					// 去掉视频窗口分享图标
					handleCloseShareDoc(mAccountId);
					if(multiViewList.size()>0){
							zoomOutAccount = multiViewList.get(0).getAccountId();
							currentPosition = 0;
							setPageShow(SINGLE_BIG_PAGE);
						CustomLog.d(TAG,"STOP_SHARE_DOC,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
							mMultiSpeakerViewListener.update(currentPosition);
					}else{
						CustomToast.show(mContext,getContext().getString(R.string.no_people_speak), Toast.LENGTH_LONG);
					}
					break;
				case NotifyType.SHARE_NAME_CHANGE:
					CustomLog.d("MultiSpeakerView", "SHARE_NAME_CHANGE");
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView", "SHARE_NAME_CHANGE cmd "
							+ respModel.toString());
					break;
				case NotifyType.SERVER_NOTICE_START_SCREEN_SHAREING:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"SERVER_NOTICE_START_SCREEN_SHAREING"
									+" respModel.getAccountId():"+respModel.getAccountId()
									+" respModel.getUserName():"+respModel.getUserName());
					addShareDocView(respModel.getAccountId(),
							respModel.getUserName(), false);
					handleOpenShareDoc(respModel.getAccountId());
					break;
				case NotifyType.SERVER_NOTICE_STOP_SCREEN_SHAREING:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"SERVER_NOTICE_STOP_SCREEN_SHAREING"
									+" respModel.getAccountId():"+respModel.getAccountId()
									+" respModel.getUserName():"+respModel.getUserName());
					removeShareDocView(respModel.getAccountId(),
							respModel.getUserName());
					// 去掉视频窗口分享图标
					handleCloseShareDoc(respModel.getAccountId());
					if(multiViewList.size()>0){
							zoomOutAccount = multiViewList.get(0).getAccountId();
							currentPosition = 0;
							setPageShow(SINGLE_BIG_PAGE);
						CustomLog.d(TAG,"SERVER_NOTICE_STOP_SCREEN_SHAREING,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
							mMultiSpeakerViewListener.update(currentPosition);
					}else{
						CustomToast.show(mContext,getContext().getString(R.string.no_people_speak), Toast.LENGTH_LONG);
					}
					break;
				case NotifyType.SERVER_NOTICE_STREAM_PUBLISH:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"SERVER_NOTICE_STREAM_PUBLISH getMediaType() "
									+ respModel.getMediaType());
					SurfaceView surfaceView = null;
					if (respModel.getMediaType() == MediaType.TYPE_DOC_VIDEO) {
						if (shareDocView == null)
							return;
						surfaceView = shareDocView.getSurfaceView();
					} else if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
						if (getItemView(respModel.getAccountId()) == null)
							return;
						surfaceView = getItemView(respModel.getAccountId())
								.getSurfaceView();
					}
					if (mAccountId != null
							&& mAccountId.equals(respModel.getAccountId())) {
						if (surfaceView != null)
							mButelOpenSDK.startLocalVideo(respModel.getMediaType(),
									surfaceView);
					} else {
						if (surfaceView != null)
							mButelOpenSDK.startRemoteVideo(
									respModel.getAccountId(),
									respModel.getMediaType(), surfaceView);
					}
					handleOpenMicOrCam(respModel.getAccountId());
					if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
						handleOpenVideo(respModel.getAccountId(), 0);
						handleVideoForNormal(respModel.getAccountId(),
								getItemView(respModel.getAccountId()));
					}
					break;
				// 通知有人静音了
				// 取消静音
				case NotifyType.SERVER_NOTICE_STREAM_UNPUBLISH:
					respModel = (Cmd) arg1;
					if (respModel == null)
						return;
					CustomLog.d("MultiSpeakerView",
							"SERVER_NOTICE_STREAM_UNPUBLISH getMediaType() "
									+ respModel.getMediaType());
					handleCloseMicOrCam(respModel.getAccountId());
					if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
						handleCloseVideo(respModel.getAccountId(), 0);
					}
					break;
				case NotifyType.SPEAKER_VIDEO_PARAM_UPDATE:
					CustomLog.d("MultiSpeakerView", "SPEAKER_VIDEO_PARAM_UPDATE ");
					String id = (String) arg1;
					if (id == null)
						return;
					SpeakerItemView view = getItemView(id);
					if (view != null) {
						handleVideoForNormal(id,view);
					}
					break;
				case NotifyType.CLOSE_CAMERA:
					CustomLog.d("MultiSpeakerView",mAccountId+" CLOSE_CAMERA ");
					CustomToast.show(mContext, getContext().getString(R.string.camera_open_fail), CustomToast.LENGTH_LONG);
					handleCloseMicOrCam(mAccountId);
					break;
				case NotifyType.CLOSE_MIC:
					CustomLog.d("MultiSpeakerView",mAccountId+" CLOSE_MIC ");
					CustomToast.show(mContext, getContext().getString(R.string.mic_open_fail), CustomToast.LENGTH_LONG);
					handleCloseMicOrCam(mAccountId);
					break;
				default:
					break;
			}
		}
	};

	@SuppressLint("NewApi")
	public void handleOpenVideo(String accountId, int type) {
		CustomLog.d("MultiSpeakerView", "handleOpenVideo " + accountId);
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		SpeakerItemView view;
		if (type == 2) {
			view = shareDocView;
		} else {
			view = getItemView(accountId);
		}
		if (view == null)
			return;
		switch (type) {
			case 0:
				CustomLog
						.d("MultiSpeakerView",
								"handleOpenVideo info.getCamStatus()"
										+ info.getCamStatus());
				if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
					view.getSurfaceView().setBackground(null);
					setImageIconView(accountId);
				}
				break;
			case 1:
				CustomLog.d(
						"MultiSpeakerView",
						"handleOpenVideo info.getVideoStatus()"
								+ info.getVideoStatus());
				if (info.getVideoStatus() == SpeakerInfo.VIDEO_OPEN) {
					view.getSurfaceView().setBackground(null);
				}
				break;
			case 2:
				CustomLog.d(
						"MultiSpeakerView",
						"handleCloseVideo info.getDocVideoStatus()"
								+ info.getDocVideoStatus());
				if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OPEN) {
					view.getSurfaceView().setBackground(null);
				}
				break;
		}

	}

	public void handleCloseVideo(String accountId, int type) {
		CustomLog.d("MultiSpeakerView", "handleCloseVideo " + accountId);
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		SpeakerItemView view;
		if (type == 2) {
			view = shareDocView;
		} else {
			view = getItemView(accountId);
		}
		if (view == null)
			return;
		switch (type) {
			case 0:
				CustomLog.d(
						"MultiSpeakerView",
						"handleCloseVideo info.getCamStatus()"
								+ info.getCamStatus());
				if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
					view.getSurfaceView().setBackgroundResource(
							MResource.getIdByName(mContext, MResource.DRAWABLE,
									"meeting_room_close_camera"));
				}
				break;
			case 1:
				CustomLog.d(
						"MultiSpeakerView",
						"handleCloseVideo info.getVideoStatus()"
								+ info.getVideoStatus());
				if (info.getVideoStatus() == SpeakerInfo.VIDEO_OFF) {
					view.getSurfaceView().setBackgroundResource(
							MResource.getIdByName(mContext, MResource.DRAWABLE,
									"close_this_video"));
				}
				break;
			case 2:
				CustomLog.d(
						"MultiSpeakerView",
						"handleCloseVideo info.getDocVideoStatus()"
								+ info.getDocVideoStatus());
				if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OFF) {
					view.getSurfaceView().setBackgroundResource(
							MResource.getIdByName(mContext, MResource.DRAWABLE,
									"close_this_video"));
				}
				break;
		}

	}

	@SuppressLint("NewApi")
	private void handleVideoForNormal(String accountId, SpeakerItemView view) {
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		CustomLog.d("MultiSpeakerView", accountId
				+ " handleVideoForNormal getCamStatus()" + info.getCamStatus()
				+ " getVideoStatus() " + info.getVideoStatus()+" getVideoOffReason "+info.getVideoOffReason());
		if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
			view.getSurfaceView().setBackgroundResource(
					MResource.getIdByName(mContext, MResource.DRAWABLE,
							"meeting_room_close_camera"));
		} else if (info.getVideoStatus() == SpeakerInfo.VIDEO_OFF) {
			if (info.getVideoOffReason() == SpeakerInfo.VIDEO_OFF_REASON_AUTO) {
				view.getSurfaceView().setBackgroundResource(
						MResource.getIdByName(mContext, MResource.DRAWABLE,
								"meeting_room_close_camera_tip"));

				if(currentPage!=DATA_PAGE){
					mMenuView.videoOffReminderInfoViewShow();
					videoOffAuto = false;
				}else{
					videoOffAuto = true;
				}

			}else{
				view.getSurfaceView().setBackgroundResource(
						MResource.getIdByName(mContext, MResource.DRAWABLE,
								"meeting_room_close_camera"));
			}
		} else {
			view.getSurfaceView().setBackground(null);
		}
	}

	@SuppressLint("NewApi")
	private void handleVideoForShare(String accountId, SpeakerItemView view) {
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		CustomLog.d(
				"MultiSpeakerView",
				"handleVideoForShare getDocVideoStatus()"
						+ info.getDocVideoStatus());
		if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OFF) {

			view.getSurfaceView().setBackgroundResource(
					MResource.getIdByName(mContext, MResource.DRAWABLE,
							"close_this_video"));

		} else {
			view.getSurfaceView().setBackground(null);
		}
	}

	private void handleOpenShareDoc(String accountId) {
		CustomLog.d("MultiSpeakerView", "handleOpenShareDoc " + accountId);
		if (getItemView(accountId) == null)
			return;
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		setImageIconView(accountId);
	}

	private void handleCloseShareDoc(String accountId) {
		CustomLog.d("MultiSpeakerView", "handleCloseShareDoc " + accountId);
		if (getItemView(accountId) == null)
			return;
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		removeImageIconView(getItemView(accountId),
				info.getScreenShareStatus(), info.getMICStatus(),
				info.getCamStatus());
	}

	public void handleOpenMicOrCam(String accountId) {
		if (accountId == null)
			return;
		CustomLog.d("MultiSpeakerView", "handleOpenMicOrCam " + accountId);
		if (getItemView(accountId) == null)
			return;
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		CustomLog.d("MultiSpeakerView", "handleOpenMicOrCam getMICStatus "
				+ info.getMICStatus());
		if (shareDocView != null && shareDocAccountId.equals(accountId)) {
			if (info.getMICStatus() == SpeakerInfo.MIC_STATUS_ON) {
				shareDocView.removeImageIcon(SpeakerItemView.MIC_TYPE);
			}
			if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
				shareDocView.removeImageIcon(SpeakerItemView.CAMERA_TYPE);
			}
		}
		removeImageIconView(getItemView(accountId),
				info.getScreenShareStatus(), info.getMICStatus(),
				info.getCamStatus());
	}

	public void handleCloseMicOrCam(String accountId) {
		if (accountId == null)
			return;
		CustomLog.d("MultiSpeakerView", "handleCloseMicOrCam " + accountId);
		if (getItemView(accountId) == null)
			return;
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		setImageIconView(accountId);
		if (shareDocView != null && shareDocAccountId.equals(accountId)) {
			if (shareDocView.isExistImageIcon()) {
				shareDocView.removeAllImageIcon();
			}
			if (info.getMICStatus() == SpeakerInfo.MIC_STATUS_OFF) {
				shareDocView.addImageIcon(SpeakerItemView.MIC_TYPE);
			}
			if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
				shareDocView.addImageIcon(SpeakerItemView.CAMERA_TYPE);
			}
		}
	}

	public void addSpeakerView(String accountId, String name,
							   boolean isNeedPublish) {
		CustomLog.d("MultiSpeakerView", "addSpeakerView " + accountId + " "
				+ name);
		if (accountId == null || getItemView(accountId) != null) {
			return;
		}
		// 新发言人产生
		String mName;
		if (name == null || name.equals("")) {
			mName = getContext().getString(R.string.unnamed);
		} else {
			mName = name;
		}

		SurfaceView view = new SurfaceView(mContext);
		CustomLog.e("MultiSpeakerView", "SurfaceView new"
				+ view.getHolder().isCreating());
		SpeakerItemView speaker = new SpeakerItemView(mContext) {

			@Override
			public void viewOnClick(View view, int type, int viewType,
									String account, float x, float y) {
				CustomLog.d("MultiSpeakerView ", "multiViewList.size() "
						+ multiViewList.size() + " type " + type + " viewType "
						+ viewType);
			}

			@Override
			public void viewFlingPageRight() {

			}

			@Override
			public void viewFlingPageLeft() {

			}

			@Override
			public void viewFlingPageUp() {
				flingPageUp();
			}

			@Override
			public void viewFlingPageDown() {
				flingPageDown();
			}

		};
		speaker.setViewType(NORMAL_PAGE);
		speaker.createView(view, accountId, mName, 0, density, true);
		// 调用sdk 订阅
		if (isNeedPublish) {
			if (mAccountId != null && mAccountId.equals(accountId)) {
				mButelOpenSDK.startLocalVideo(MediaType.TYPE_VIDEO, view);
			} else {
				mButelOpenSDK.startRemoteVideo(accountId, MediaType.TYPE_VIDEO,
						view);
			}
		}
		CustomLog.e("MultiSpeakerView", "SpeakerItemView before"
				+ speaker.getSurfaceView().getHolder().isCreating());
		multiViewList.add(speaker);
		CustomLog.e("MultiSpeakerView", "SpeakerItemView after"
				+ speaker.getSurfaceView().getHolder().isCreating());
		CustomLog.d("MultiSpeakerView", "addSpeakerView multiViewList "
				+ multiViewList.size());
		// ADD 进去
		// 如果是等屏需要立马变换位置，其他等滑动的时候在变换
		this.addView(speaker);
		if (mButelOpenSDK.getSpeakerInfoById(accountId) != null) {
			setImageIconView(accountId);
		}

		switch (currentPage) {
			case DATA_PAGE:
				mButelOpenSDK.pauseSubscribe(accountId, MediaType.TYPE_VIDEO);
				break;
			case MULTI_PAGE:
				zoomOutAccount = accountId;
				setPageShow(MULTI_PAGE);
				break;
		}

		if(multiViewList.size()==1){
			zoomOutAccount = accountId;
			setPageShow(SINGLE_BIG_PAGE);
		}

		mMenuView.hideSpeakerListInfoViewShow();
		mMenuView.speakerListInfoViewShow();

		setCurrentIndex();
		CustomLog.d(TAG,"addSpeakerView,mMultiSpeakerViewListener.add,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.add(currentPosition);
	}

	public void setImageIconView(String accountId) {
		SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
		if (info == null)
			return;
		SpeakerItemView view = getItemView(accountId);
		int shareDocType = info.getScreenShareStatus();
		int micPhoneType = info.getMICStatus();
		int camType = info.getCamStatus();
		CustomLog.d("MultiSpeakerView", "setImageIconView shareDocType "
				+ shareDocType + " micPhoneType " + micPhoneType + " camType "
				+ camType);
		if (view != null) {
			if (view.isExistImageIcon()) {
				view.removeAllImageIcon();
			}
			if (shareDocType == SpeakerInfo.SCREEN_SHARING) {
				view.addImageIcon(SpeakerItemView.SHARE_DOC_TYPE);
			}
			if (micPhoneType == SpeakerInfo.MIC_STATUS_OFF) {
				view.addImageIcon(SpeakerItemView.MIC_TYPE);
			}
			if (camType == SpeakerInfo.CAM_STATUS_CLOSE) {
				view.addImageIcon(SpeakerItemView.CAMERA_TYPE);
			}
		}
	}

	private void removeImageIconView(SpeakerItemView view, int shareDocType,
									 int micPhoneType, int camType) {
		CustomLog.d("MultiSpeakerView", "removeImageIconView " + shareDocType
				+ " " + micPhoneType + " " + camType);
		if (view != null) {
			if (shareDocType == SpeakerInfo.SCREEN_NORMAL) {
				view.removeImageIcon(SpeakerItemView.SHARE_DOC_TYPE);
			}
			if (micPhoneType == SpeakerInfo.MIC_STATUS_ON) {
				view.removeImageIcon(SpeakerItemView.MIC_TYPE);
			}
			if (camType == SpeakerInfo.CAM_STATUS_OPEN) {
				view.removeImageIcon(SpeakerItemView.CAMERA_TYPE);
			}
		}
	}

	public void addShareDocView(final String accountId, String name,
								boolean isNeedPublish) {
		CustomToast.show(mContext, name + getContext().getString(R.string.start_screen_share), CustomToast.LENGTH_SHORT);
		mMenuView.hideSwitchVideoTypeView();
		CustomLog.d("MultiSpeakerView", "addShareDocView " + accountId + " "
				+ name);
		if (shareDocView != null) {
			removeShareDocView(accountId, name);
		}
		SurfaceView view = new SurfaceView(mContext);
		shareDocView = new SpeakerItemView(mContext) {
			@Override
			public void viewOnClick(View view, int type, int viewType,
									String account, float x, float y) {
				CustomLog.e("MultiSpeakerView ", "multiViewList.size() "
						+ multiViewList.size() + " type " + type + " viewType "
						+ viewType + " x " + x + " y " + y);
				if (viewType == NORMAL_PAGE) {
					return;
				}

			}

			@Override
			public void viewFlingPageRight() {

			}

			@Override
			public void viewFlingPageLeft() {

			}

			@Override
			public void viewFlingPageUp() {
				flingPageUp();
			}

			@Override
			public void viewFlingPageDown() {
				flingPageDown();
			}

		};
		shareDocView.setViewType(DATA_PAGE);
		shareDocView.createView(view, accountId, name, 0, density, false);
		if (mButelOpenSDK.getSpeakerInfoById(accountId) != null) {
			if (mButelOpenSDK.getSpeakerInfoById(accountId).getMICStatus() == SpeakerInfo.MIC_STATUS_OFF) {
				shareDocView.addImageIcon(SpeakerItemView.MIC_TYPE);
			}
			if (mButelOpenSDK.getSpeakerInfoById(accountId).getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
				shareDocView.addImageIcon(SpeakerItemView.CAMERA_TYPE);
			}
		}
		// 调用sdk 订阅
		if (isNeedPublish) {
			if (mAccountId != null && mAccountId.equals(accountId)) {
				mButelOpenSDK.startLocalVideo(MediaType.TYPE_DOC_VIDEO, view);
			} else {
				mButelOpenSDK.startRemoteVideo(accountId,
						MediaType.TYPE_DOC_VIDEO, view);
			}
		}
		this.addView(shareDocView);
		pageSize++;
		CustomLog.d("MultiSpeakerView", "addShareDocView pageSize " + pageSize);
		shareDocAccountId = accountId;
		setPageShow(DATA_PAGE);

		setCurrentIndex();
		CustomLog.d(TAG,"addShareDocView,mMultiSpeakerViewListener.add,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.add(currentPosition);
	}

	public void removeSpeakerView(String accountId, String name) {
		if (accountId == null) {
			return;
		}
		if (shareDocView != null && shareDocAccountId != null
				&& accountId.equals(shareDocAccountId)) {
			removeShareDocView(accountId, name);
		}
		// 取消主讲发言 主讲页面，取消的是主讲，切到等屏 pageSize--;
		// 取消发言 等屏页面，补位
		if (masterAccountId != null && accountId.equals(masterAccountId)) {
			// zhujiang
			masterAccountId = null;
			if (pageSize > 1) {
				pageSize--;
			}
		}

		SpeakerItemView view = getItemView(accountId);
		if (view != null) {
			if (mAccountId != null && mAccountId.equals(accountId)) {
				mButelOpenSDK.stopLocalVideo(MediaType.TYPE_VIDEO);
			} else {
				mButelOpenSDK.stopRemoteVideo(accountId, MediaType.TYPE_VIDEO);
			}
			this.removeView(view);
			multiViewList.remove(view);
		}

		MultiSpeakerView.this.onNotify(0, null);

		switch (currentPage) {
			case DATA_PAGE:
				break;
			case MULTI_PAGE:
				setPageShow(MULTI_PAGE);
				break;
		}
		setCurrentIndex();
		CustomLog.d(TAG,"removeSpeakerView,mMultiSpeakerViewListener.remove,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.remove(currentPosition);

	}

	private void setCurrentIndex(){
		CustomLog.d(TAG,"setCurrentIndex,currentPage:"+String.valueOf(currentPage)
		+"currentPosition:"+String.valueOf(currentPosition));
		if(shareDocView!=null&&currentPage==DATA_PAGE){
			currentPosition = multiViewList.size();
		}else{
			for(int i=0;i<multiViewList.size();i++){
				if(multiViewList.get(i).getAccountId().equals(zoomOutAccount)){
					currentPosition = i;
					return;
				}
			}
		}
	}

	public void removeShareDocView(String accountId, String name) {
		CustomLog.d("MultiSpeakerView", "removeShareDocView " + accountId);
		CustomToast.show(mContext, name + getContext().getString(R.string.end_screen_share), CustomToast.LENGTH_SHORT);
		mMenuView.hideSwitchVideoTypeView();
		// 取消数据分享 数据页面，切到等屏 pageSize--;
		if (accountId == null) {
			return;
		}
		if (pageSize > 1) {
			pageSize--;
		}
		if (shareDocView != null) {
			if (mAccountId != null && mAccountId.equals(accountId)) {
				mButelOpenSDK.stopLocalVideo(MediaType.TYPE_DOC_VIDEO);
			} else {
				mButelOpenSDK.stopRemoteVideo(accountId,
						MediaType.TYPE_DOC_VIDEO);
			}

			this.removeView(shareDocView);
		}
		if (currentPage == DATA_PAGE) {
			setPageShow(MULTI_PAGE);
		}
		shareDocAccountId = null;
		shareDocView = null;

		setCurrentIndex();
		CustomLog.d(TAG,"removeShareDocView,mMultiSpeakerViewListener.remove,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.remove(currentPosition);

	}

	private SpeakerItemView getItemView(String accountId) {
		SpeakerItemView view = null;
		for (int i = 0; i < multiViewList.size(); i++) {
			if (multiViewList.get(i).getAccountId() != null
					&& multiViewList.get(i).getAccountId().equals(accountId)) {
				view = multiViewList.get(i);
			}
		}
		return view;
	}

	private void setPageShow(int pageType) {
		CustomLog.d(TAG,"setPageShow,currentPage:"+String.valueOf(currentPage)+" pageType:"+String.valueOf(pageType));
		switch (pageType) {
			case DATA_PAGE:
				// mMenuView 常显提示
				mMenuView.setPageType(1, null);
				mMenuView.hideVideoOffReminderInfoView();
				mMenuView.hideSpeakerListInfoViewShow();
				mMenuView.shareDocReminderInfoViewShow();
				if (shareDocView != null) {
					shareDocView.setSpeakerItemViewParams(width, height, 0, 0);
					mButelOpenSDK.resumeSubscribe(shareDocView.getAccountId(),
							MediaType.TYPE_DOC_VIDEO);
					handleVideoForShare(shareDocView.getAccountId(), shareDocView);
				}
				for (int i = 0; i < multiViewList.size(); i++) {
					multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0);
					mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
							.getAccountId(), MediaType.TYPE_VIDEO);
				}
				currentPage = DATA_PAGE;
				break;
			case SINGLE_BIG_PAGE:
				mMenuView.setPageType(2, zoomOutAccount);

				mMenuView.hideShareDocReminderInfoView();
				if(videoOffAuto){
					videoOffAuto = false;
					mMenuView.videoOffReminderInfoViewShow();
				}
				mMenuView.speakerListInfoViewShow();
				if (shareDocView != null) {
					shareDocView.setSpeakerItemViewParams(1, 1, 0, 0);
					mButelOpenSDK.pauseSubscribe(shareDocView.getAccountId(),
							MediaType.TYPE_DOC_VIDEO);
				}
				SpeakerItemView view = null;
				for (int i = 0; i < multiViewList.size(); i++) {
					multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0);
					if (zoomOutAccount != null
							&& multiViewList.get(i).getAccountId()
							.equals(zoomOutAccount)) {
						view = multiViewList.get(i);
						if (view != null) {
							view.setSpeakerItemViewParams(width, height, 0, 0);
							mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
									.getAccountId(), MediaType.TYPE_VIDEO);
						}
					} else {
						mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
								.getAccountId(), MediaType.TYPE_VIDEO);
					}
				}
				currentPage = SINGLE_BIG_PAGE;
				break;

			case MULTI_PAGE:
				if(zoomOutAccount == null ){
					zoomOutAccount = mAccountId;
				}
				setPageShow(SINGLE_BIG_PAGE);
				break;
		}
	}

	public void setMultiViewListBg() {
		if (multiViewList == null)
			return;
		for (int i = 0; i < multiViewList.size(); i++) {
			handleVideoForNormal(multiViewList.get(i).getAccountId(),
					multiViewList.get(i));
		}
	}

	public void flingPageLeft() {
		CustomLog.d("MultiSpeakerView", "flingPageLeft ");
		CustomLog.d(TAG,"pagetype:"+String.valueOf(pagetype)
				+"pageSize:"+String.valueOf(pageSize)
				+"currentPage:"+String.valueOf(currentPage));
		if (pageSize == 1)
			return;
		switch (currentPage) {

			case SINGLE_BIG_PAGE:
				if(pageSize == 2){
					if (shareDocView != null) {
						// 切数据
						setPageShow(DATA_PAGE);
					}
				}
				break;

			case DATA_PAGE:
				if(pageSize == 2){
					if(pagetype==ZOOM_OUT_TYPE){
						setPageShow(SINGLE_BIG_PAGE);
					} else {
						setPageShow(MULTI_PAGE);
					}
				}
				break;
			case MULTI_PAGE:
				if(pageSize == 2){
					if (shareDocView != null) {
						// 切数据
						setPageShow(DATA_PAGE);
					}
				}
				break;
		}
	}

	public void flingPageRight() {
		CustomLog.d("MultiSpeakerView", "flingPageRight ");
		CustomLog.d(TAG,"pagetype:"+String.valueOf(pagetype)
				+"pageSize:"+String.valueOf(pageSize)
				+"currentPage:"+String.valueOf(currentPage));
		if (pageSize == 1)
			return;
		switch (currentPage) {

			case SINGLE_BIG_PAGE:
				if(pageSize == 2){
					if (shareDocView != null) {
						// 切数据
						setPageShow(DATA_PAGE);
					}
				}
				break;

			case DATA_PAGE:
				if (pageSize == 2) {

					if(pagetype==ZOOM_OUT_TYPE){
						setPageShow(SINGLE_BIG_PAGE);
					} else {
						setPageShow(MULTI_PAGE);
					}

				}
				break;

			case MULTI_PAGE:
				if (pageSize == 2) {
					if (shareDocView != null) {
						// 切数据
						setPageShow(DATA_PAGE);
					}
				}
				break;
		}
	}

	public void flingPageUp() {
		CustomLog.d("MultiSpeakerView", "flingPageUp ");
		CustomLog.d(TAG,"pagetype:"+String.valueOf(pagetype)
				+"pageSize:"+String.valueOf(pageSize)
				+"currentPage:"+String.valueOf(currentPage));

		if(currentPosition == multiViewList.size()-1){
			if(shareDocView != null){
				currentPosition = multiViewList.size();
				setPageShow(DATA_PAGE);
			}else{
				currentPosition = 0;
				zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
				setPageShow(SINGLE_BIG_PAGE);
			}
		}else if(currentPosition == multiViewList.size() && shareDocView != null){
			currentPosition = 0;
			zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
			setPageShow(SINGLE_BIG_PAGE);
		}
		else{
			currentPosition++;
			zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
			setPageShow(SINGLE_BIG_PAGE);
		}
		CustomLog.d(TAG,"flingPageUp,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.update(currentPosition);
	}

	public void flingPageDown() {
		CustomLog.d("MultiSpeakerView", "flingPageDown ");
		CustomLog.d(TAG,"pagetype:"+String.valueOf(pagetype)
				+"pageSize:"+String.valueOf(pageSize)
				+"currentPage:"+String.valueOf(currentPage));

		if(currentPosition == 0){
			if(shareDocView!=null){
				currentPosition = multiViewList.size();
				setPageShow(DATA_PAGE);
			}else{
				currentPosition = multiViewList.size()-1;
				zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
				setPageShow(SINGLE_BIG_PAGE);
			}
		}else if(currentPosition == multiViewList.size() && shareDocView != null){
			currentPosition = multiViewList.size()-1;
			zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
			setPageShow(SINGLE_BIG_PAGE);
		}else{
			currentPosition--;
			zoomOutAccount = multiViewList.get(currentPosition).getAccountId();
			setPageShow(SINGLE_BIG_PAGE);
		}
		CustomLog.d(TAG,"flingPageDown,mMultiSpeakerViewListener.update,currentPosition:"+String.valueOf(currentPosition));
		mMultiSpeakerViewListener.update(currentPosition);
	}

}
