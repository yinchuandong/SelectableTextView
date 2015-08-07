package com.zyz.mobile.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private SelectableTextView mTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// make sure the TextView's BufferType is Spannable, see the main.xml
		mTextView = (SelectableTextView) findViewById(R.id.main_text);
		mTextView.setDefaultSelectionColor(0x40FF00FF);
		
		View menuView = getLayoutInflater().inflate(R.layout.select_menu, null);
		mTextView.setPopupMenuView(menuView);
		
		
		//给自定义弹出菜单绑定事件
		TextView menuCopy = (TextView)menuView.findViewById(R.id.menu_copy);
		menuCopy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SelectionInfo info = mTextView.getCursorSelection();
				String str = info.getSelectedText().toString();
				Log.d("copy button click", "------:" + str);
			}
		});
	}
}
