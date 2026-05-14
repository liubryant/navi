//package cn.navibeidou.beidou.Util;
//
//import android.app.Activity;
//import android.content.Context;
//
//import cn.navibeidou.beidou.ApplicationShared;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.util.Map;
//
//import cn.pedant.SweetAlert.SweetAlertDialog;
//
//public class SweetAlertUtil {
//
//    //只显示标题：
//    public static void showMessage(String msg) {
//        new SweetAlertDialog(ApplicationShared.getContext())
//                .setTitleText(msg)
//                .show();
//    }
//
//    //显示标题和内容：
//    public static void showMessage(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext())
//                .setTitleText(msg)
//                .setContentText(detail)
//                .show();
//    }
//
//    //显示异常样式：
//    public static void showError(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDiaLog.dRROR_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .show();
//    }
//
//    //显示警告样式：
//    public static void showWarning(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDialog.WARNING_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .setConfirmText("Yes,delete it!")
//                .show();
//    }
//
//    //显示成功完成样式：
//    public static void showSuccess(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDialog.SUCCESS_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .show();
//    }
//
//    //自定义头部图像：
//    public static void showCustomImage(String msg, String detail, int image) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .setCustomImage(image)//R.drawable.custom_img)
//                .show();
//    }
//
//    //确认事件绑定：
//    public static void showWarningPro(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDialog.WARNING_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .setConfirmText("Yes,delete it!")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        sDialog.dismissWithAnimation();
//                    }
//                })
//                .show();
//    }
//
//    //显示取消按钮及事件绑定：
//    public static void showWarningPro2(Context context, String msg, String detail) {
//        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .setCancelText("No,cancel plx!")
//                .setConfirmText("Yes,delete it!")
//                .showCancelButton(true)
//                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        sDialog.cancel();
//                    }
//                })
//                .show();
//    }
//
//    //确认后切换对话框样式：
//    public static void showWarningPro3(String msg, String detail) {
//        new SweetAlertDialog(ApplicationShared.getContext(), SweetAlertDialog.WARNING_TYPE)
//                .setTitleText(msg)
//                .setContentText(detail)
//                .setConfirmText("Yes,delete it!")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        sDialog
//                                .setTitleText("Deleted!")
//                                .setContentText("Your imaginary file has been deleted!")
//                                .setConfirmText("OK")
//                                .setConfirmClickListener(null)
//                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                    }
//                })
//                .show();
//    }
//
//    public static Activity getCurrentActivity () {
//        try {
//            Class activityThreadClass = Class.forName("android.app.ActivityThread");
//            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
//                    null);
//            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
//            activitiesField.setAccessible(true);
//            Map activities = (Map) activitiesField.get(activityThread);
//            for (Object activityRecord : activities.values()) {
//                Class activityRecordClass = activityRecord.getClass();
//                Field pausedField = activityRecordClass.getDeclaredField("paused");
//                pausedField.setAccessible(true);
//                if (!pausedField.getBoolean(activityRecord)) {
//                    Field activityField = activityRecordClass.getDeclaredField("activity");
//                    activityField.setAccessible(true);
//                    Activity activity = (Activity) activityField.get(activityRecord);
//                    return activity;
//                }
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
