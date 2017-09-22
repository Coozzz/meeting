package cn.redcdn.util;

import android.content.Context;

public class MResource {  
  public final static String ID = "id";
  public final static String LAYOUT = "layout";
  public final static String DRAWABLE = "drawable";
  public final static String ANIM = "anim";
  public final static String STYLE = "style";
  public final static String STYLEABLE = "styleable";
  public final static String RAW = "raw";
  public static int getIdByName(Context context, String className, String name) {  
    int id = 0;  
    if (context == null || className == null || name == null) {
      return id;
    }
      String packageName = context.getPackageName();  
      Class r = null;  
      try {  
          r = Class.forName(packageName + ".R");  

          Class[] classes = r.getClasses();  
          Class desireClass = null;  

          for (int i = 0; i < classes.length; ++i) {  
              if (classes[i].getName().split("\\$")[1].equals(className)) {  
                  desireClass = classes[i];  
                  break;  
              }  
          }  

          if (desireClass != null)  
              id = desireClass.getField(name).getInt(desireClass);  
      } catch (ClassNotFoundException e) {  
          e.printStackTrace();  
      } catch (IllegalArgumentException e) {  
          e.printStackTrace();  
      } catch (SecurityException e) {  
          e.printStackTrace();  
      } catch (IllegalAccessException e) {  
          e.printStackTrace();  
      } catch (NoSuchFieldException e) {  
          e.printStackTrace();  
      }  

      return id;  
  }
  
  public static int[] getIdsByName(Context context, String className, String name) {
    int[] ids = null;
    if (context == null || className == null || name == null) {
      return ids;
    }
    String packageName = context.getPackageName();
    Class r = null;
    try {
      r = Class.forName(packageName + ".R");

      Class[] classes = r.getClasses();
      Class desireClass = null;

      for (int i = 0; i < classes.length; ++i) {
        if (classes[i].getName().split("\\$")[1].equals(className)) {
          desireClass = classes[i];
          break;
        }
      }

      if ((desireClass != null) && (desireClass.getField(name).get(desireClass) != null) && (desireClass.getField(name).get(desireClass).getClass().isArray()))
        ids = (int[])desireClass.getField(name).get(desireClass);
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    return ids;
  }

}  