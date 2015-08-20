package com.stra.studyhelper;

import com.stra.studyhelper.widget.SelectableTextView;
import com.stra.studyhelper.widget.SelectableTextView.SelectionInfo;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;


public class SelectMenuItemListener implements OnClickListener{
	
	private Activity context;
	private ClipboardManager clipboard;
	private SelectableTextView textView;
	
	public SelectMenuItemListener(Context contenxt, SelectableTextView textView){
		this.context = (Activity)contenxt;
		this.clipboard = (ClipboardManager)contenxt.getSystemService(Context.CLIPBOARD_SERVICE);
		this.textView = textView;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		SelectionInfo info = textView.getCursorSelection();
		// 用户选中的文字
		String selectText = info.getSelectedText().toString();

		switch (id) {
		case R.id.select_menu_copy:// 复制
			clipboard.setPrimaryClip(ClipData.newPlainText("selectedText", selectText));
			break;
			
		case R.id.select_menu_dict:// 查询字典
			Intent intent2 = new Intent();
			intent2.setClassName("com.stra.stradict", "com.stra.stradict.FanchaActivity");
			intent2.putExtra("WordFancha", selectText);
			context.startActivity(intent2);
			break;
			
		case R.id.select_menu_browser:// 浏览器
			Intent intent = new Intent();
			Uri content_url = Uri.parse("http://www.baidu.com/s?wd=" + selectText);
	        intent.setAction("android.intent.action.VIEW");
	        intent.setData(content_url);
	        context.startActivity(intent);
			break;
		}
		textView.hideCursor();
	}

}
