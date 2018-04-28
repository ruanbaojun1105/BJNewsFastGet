package com.bj.newsfastget.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bj on 2017/9/14.
 */

public class ViewUtil {

    public <T> T $(View view,int viewID)
    {
        return (T)view.findViewById(viewID);
    }

    private static View getLinItemView(int resid,Context context){
        return LayoutInflater.from(context).inflate(resid, null);
    }

    /**
     * ViewGroup多个子控件的通用复用优化
     */
    public static void setLinGreat(Context context, ViewGroup rootview, final Object[] datas, ViewReUseFaceListener listener){
        if (listener==null||datas==null||datas.length==0)
            return;
        if (rootview.getChildCount()==0){
            for (int y=0; y < datas.length; y++) {
                rootview.addView(listener.backView(context) == null ? getLinItemView(listener.backViewRes(),context) : listener.backView(context));
            }
        }else {
            int oldViewCount = rootview.getChildCount();
            int newViewCount = datas.length;
            if (oldViewCount > newViewCount) {
                rootview.removeViews(newViewCount - 1, oldViewCount - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = 0; i < newViewCount - oldViewCount; i++) {
                    rootview.addView(listener.backView(context) == null ? getLinItemView(listener.backViewRes(),context) : listener.backView(context));
                }
            }
        }
        int linCount = rootview.getChildCount();
        for (int i = 0; i <linCount; i++) {
            try {
                listener.justItemToDo(datas[i], rootview.getChildAt(i), i,context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Created by bj on 2016/6/2 0002.
     * 控件复用接口
     */
    public interface ViewReUseFaceListener {

        int backViewRes();
        View backView(Context context);
        void justItemToDo(Object data, View itemView, int position, Context context);
    }

    public static  String newActionLink(String[][] strings,String actionType){
        String link = actionType;
        if (strings!=null&&strings.length>0) {
            for (int i = 0; i < strings.length; i++){
                link+=(i==0?"?":"&")+strings[i][0]+"="+strings[i][1];
            }
        }else LogUtils.e("link creat fail!");
        return  link;
    }

    /**
     * 方法名称:transStringToMap
     * 传入参数:mapString 形如 username=chenziwen&password=1234
     * 返回值:Map
     */
    public static Map<String, Object> getHeader(String url) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //url = URLDecoder.decode(url, "UTF-8");//后一步解码
            int start = 0;
            try {
                start = url.indexOf("?");
                if (url.length() - start >= 0) {
                    String str = url.substring(start + 1);
                    String[] paramsArr = str.split("&");
                    if (paramsArr != null && paramsArr.length > 0) {
                        for (String param : paramsArr) {
                            String[] temp = param.split("=");
                            if (temp != null ){
                                if (temp.length > 1)
                                    map.put(temp[0], URLDecoder.decode(param.substring(temp[0].length()+1), "UTF-8"));//此处取所有参数
                                    //map.put(temp[0], temp[1]);
                                else map.put(temp[0], "");
                            }
                            else
                                map.put(param, param);
                        }
                    } else {
                        String[] temp = str.split("=");
                        if (temp != null && temp.length > 0)
                            map.put(temp[0], "");
                        else
                            map.put(str, str);
                    }
                }
            } catch (Exception e) {
                map.put(url, url);
            }
            return map;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return map;
        }
    }




    /**
     * 根据坐标获取相对应的子控件<br>
     * 在Activity使用
     *
     * @param x 坐标
     * @param y 坐标
     * @return 目标View
     */
    public static View getViewAtActivity(Activity activity,int x, int y) {
        // 从Activity里获取容器
        View root = activity.getWindow().getDecorView();
        return findViewByXY(root, x, y);
    }

    /**
     * 根据坐标获取相对应的子控件<br>
     * 在重写ViewGroup使用
     *
     * @param x 坐标
     * @param y 坐标
     * @return 目标View
     */
    public static View getViewAtViewGroup(View v,int x, int y) {
        return findViewByXY(v, x, y);
    }

    private static View findViewByXY(View view, int x, int y) {
        View targetView = null;
        if (view instanceof ViewGroup) {
            // 父容器,遍历子控件
            ViewGroup v = (ViewGroup) view;
            for (int i = 0; i < v.getChildCount(); i++) {
                targetView = findViewByXY(v.getChildAt(i), x, y);
                if (targetView != null) {
                    break;
                }
            }
        } else {
            targetView = getTouchTarget(view, x, y);
        }
        return targetView;

    }

    private static View getTouchTarget(View view, int x, int y) {
        View targetView = null;
        // 判断view是否可以聚焦
        ArrayList<View> TouchableViews = view.getTouchables();
        for (View child : TouchableViews) {
            if (isTouchPointInView(child, x, y)) {
                targetView = child;
                break;
            }
        }
        return targetView;
    }

    private static boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (view.isClickable() && y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }
//-----------------------------------------------------------------------算法
//    /**
//
//     交互两个数据
//
//     */
//
//     void swap(int *a,int *b) {
//        int temp;
//        temp = *a;
//*a = *b;
//*b = temp;
//    }
//
//
//
//    /**
//     冒泡排序
//     */
//    void bubbleSort(int len,int table[]) {
//        for (int i = 0; i < len - 1; i++) {
//            for (int j = 0; j < len -1- i; j++) {
//                if (table[j] > table[j + 1]) {
//                    swap(&table[j],&table[j+1]);
//                }
//            }
//        }
//    }
//
//    /**
//     选择排序
//     */
//    void selectSort(int len, int table[]) {
//        for (int i = 0; i < len; i++) {
//            int k = i;
//            for (int j = i + 1; j < len; j++) {
//                if (table[j] < table[k]) {
//                    k = j;
//                }
//            }
//            if (k != i) {
//                swap(&table[i], &table[k]);
//            }
//        }
//    }
//
//
//    /*
//    插入排序
//    */
//    void insertSort(int len, int table[]) {
//        for (int i = 1; i < len; i++) {
//            int j = i - 1;
//            int temp = table[i];
//            while (j >= 0 && table[j] > temp) {
//                table[j + 1] = table[j];
//                j--;
//            }
//            table[j + 1] = temp;
//        }
//    }
//
//
//
//
//
//
//    /*
//    快速排序
//    */
//    void quickSort(int left,int right, int table[]) {
//        if (left < right) {
//            int key = table[left];
//            int l = left, r = right;
//            while (l < r) {
//                while (l<r && table[r]>key) {
//                    r--;
//                }
//                swap(&table[l], &table[r]);
//                while (l<r && table[l] < key) {
//                    l++;
//                }
//                swap(&table[l], &table[r]);
//            }
//            quickSort(left, l - 1, table);
//            quickSort(l+1, right, table);
//        }
}
