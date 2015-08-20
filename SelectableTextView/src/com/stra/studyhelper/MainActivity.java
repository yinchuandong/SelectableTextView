package com.stra.studyhelper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.stra.studyhelper.utils.LogUtils;
import com.stra.studyhelper.widget.SelectableTextView;
import com.stra.studyhelper.widget.SelectableTextView.OnCursorStateChangedListener;
import com.stra.studyhelper.widget.SelectableTextView.SelectionInfo;

public class MainActivity extends Activity{
	private static final String TAG = "MainActivity";
	private SelectableTextView textView;
	private View menuView;
	
	private SelectMenuItemListener selectMenuItemListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initView();
		initListener();
	}
	
	
	/**
	 * 初始化监听事件
	 */
	private void initListener() {
		this.selectMenuItemListener = new SelectMenuItemListener(this, textView);
		menuView.findViewById(R.id.select_menu_copy).setOnClickListener(selectMenuItemListener);
		menuView.findViewById(R.id.select_menu_dict).setOnClickListener(selectMenuItemListener);
		menuView.findViewById(R.id.select_menu_browser).setOnClickListener(selectMenuItemListener);
		
		textView.setOnCursorStateChangedListener(new OnCursorStateChangedListener() {
			
			@Override
			public void onShowCursors(View v) {
				// TODO Auto-generated method stub
//				LogUtils.i(TAG, "onShowCursors");
			}
			
			@Override
			public void onPositionChanged(View v, int x, int y, int oldx, int oldy) {
				// TODO Auto-generated method stub
//				LogUtils.i(TAG, "onPositionChanged");
			}
			
			@Override
			public void onHideCursors(View v) {
				// TODO Auto-generated method stub
//				LogUtils.i(TAG, "onHideCursors");
			}
			
			@Override
			public void onDragStarts(View v) {
				// TODO Auto-generated method stub
//				LogUtils.i(TAG, "onDragStarts");
			}
		});
	}
	/**
	 * 初始化控件
	 */
	private void initView() {
		// make sure the TextView's BufferType is Spannable, see the main.xml
		textView = (SelectableTextView) findViewById(R.id.main_text);
		textView.setDefaultSelectionColor(0x40FF00FF);

		menuView = getLayoutInflater().inflate(R.layout.tpl_select_menu, null);
		textView.setPopupMenuView(menuView);
		
	}

}
