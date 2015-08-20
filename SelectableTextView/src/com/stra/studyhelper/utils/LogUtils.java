package com.stra.studyhelper.utils;

import android.util.Log;


public class LogUtils {
	
	
	private static boolean debug = true;
	
	/**
	 * 错误
	 * @param tag
	 * @param msg
	 */
	public static void e(String tag,String msg){
		if(debug){
			Log.e(tag, msg + "");
		}
	}
	
	/**
	 * 信息
	 * @param tag
	 * @param msg
	 */
	public static void i(String tag,String msg){
		if(debug){
			Log.i(tag, msg + "");
		}
	}
	/**
	 * 警告
	 * @param tag
	 * @param msg
	 */
	public static void w(String tag,String msg){
		if(debug){
			Log.i(tag, msg + "");
		}
	}
	
	
	
}
