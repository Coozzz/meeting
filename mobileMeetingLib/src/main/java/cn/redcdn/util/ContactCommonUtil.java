package cn.redcdn.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import cn.redcdn.log.CustomLog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.TextView;

public class ContactCommonUtil {
  private static final String TAG = "CommonUtil";

  // 获得当前登录账号
  public static String getUesrAccount(Context context) {
    String account = "";
    AccountManager am = AccountManager.get(context);
    Account[] accounts = am.getAccountsByType("com.channelsoft");
    if (accounts != null && accounts.length != 0) {
      account = accounts[0].name;
    }
    return account;
  }

  /*
   * Create UUID
   */
  public static String getUUIDString() {
    String s = UUID.randomUUID().toString();
    return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
        + s.substring(19, 23) + s.substring(24);
  }

  public static String getNumberStrByLetStr(String letStr) {
    String number = "";

    for (int i = 0; i < letStr.length(); i++) {
      number += getNumberByChar(letStr.charAt(i));
    }

    return number;
  }

  private static String getNumberByChar(char letter) {
    String number = "";

    switch (letter) {
    case '1':
      number = "1";
      break;
    case 'a':
    case 'b':
    case 'c':
    case '2':
      number = "2";
      break;
    case 'd':
    case 'e':
    case 'f':
    case '3':
      number = "3";
      break;
    case 'g':
    case 'h':
    case 'i':
    case '4':
      number = "4";
      break;
    case 'j':
    case 'k':
    case 'l':
    case '5':
      number = "5";
      break;
    case 'm':
    case 'n':
    case 'o':
    case '6':
      number = "6";
      break;
    case 'p':
    case 'q':
    case 'r':
    case 's':
    case '7':
      number = "7";
      break;
    case 't':
    case 'u':
    case 'v':
    case '8':
      number = "8";
      break;
    case 'x':
    case 'w':
    case 'y':
    case 'z':
    case '9':
      number = "9";
      break;
    case '0':
      number = "0";
      break;
    }

    return number;
  }

  /**
   * 放大缩小图片
   * 
   * @param bitmap
   * @param w
   * @param h
   * @return
   */
  public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    Matrix matrix = new Matrix();
    float scaleWidht = ((float) w / width);
    float scaleHeight = ((float) h / height);
    matrix.postScale(scaleWidht, scaleHeight);
    Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
        true);
    return newbmp;
  }

  public static byte[] getBytes(String filePath) {
    // Constant.LOG_D("yangjs", "getBytes,filePath=" + filePath);
    byte[] buffer = null;
    try {
      File file = new File(filePath);
      if (file.exists()) {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = fis.read(b)) != -1) {
          bos.write(b, 0, n);
        }
        fis.close();
        bos.close();
        buffer = bos.toByteArray();
      }
    } catch (FileNotFoundException e) {
      // Constant.LOG_E("yangjs",
      // "getBytes,FileNotFoundException: " + e.getMessage());
    } catch (IOException e) {
      // Constant.LOG_E("yangjs", "getBytes,IOException: " + e.getMessage());
    }
    return buffer;
  }

  /**
   * 加载图片，进行伸缩
   * 
   * @param data
   *          图像数据
   * @param textView
   *          控件
   * @return
   */
  public static Bitmap loadBitmap(byte[] data, TextView textView) {
    Bitmap linkmanPhoto = null;
    if (data == null || textView == null) {
      return null;
    }
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    linkmanPhoto = BitmapFactory.decodeByteArray(data, 0, data.length, options);
    int height = textView.getHeight();
    int width = textView.getWidth();
    CustomLog.e(TAG, "TextView宽高：" + height + " " + width);
    options.inSampleSize = options.outHeight / height;
    CustomLog.e(TAG, "缩放比例：" + options.inSampleSize);
    options.outHeight = height;
    options.outWidth = width;
    options.inJustDecodeBounds = false;
    linkmanPhoto = BitmapFactory.decodeByteArray(data, 0, data.length, options);
    return linkmanPhoto;
  }

  public static int StringToInt(String str, int def) {
    int intRet = def;
    try {
      if (str == null || str.trim().equals(""))
        str = def + "";
      intRet = Integer.parseInt(str);
    } catch (NumberFormatException e) {
      CustomLog.e(TAG, "NumberFormatException "+e);
    }
    return intRet;
  }
}
