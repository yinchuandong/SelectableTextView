package com.stra.studyhelper.widget;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stra.studyhelper.R;
import com.stra.studyhelper.widget.ObservableScrollView.OnScrollChangedListener;

/**
 * User: ray Date: 2013-02-01
 * <p/>
 * WARNING: SelectableTextView is designed to be used inside a ScrollView. The
 * selection functionality have only been tested under the assumption that the
 * parent of the view is a ScrollView.
 * <p/>
 * The functionality WILL PROBABLY breaks if the TextView is not inside a
 * ScrollView.
 */
@SuppressLint("NewApi")
public class SelectableTextView extends TextView implements OnClickListener,
		OnLongClickListener {

	private final static int DEFAULT_SELECTION_LEN = 5;
	private final int SPACE_CODE = 0x20;
	
	private int mDefaultSelectionColor;
	private int mTouchX;
	private int mTouchY;

	
	/**
	 * the selection information used by the cursor
	 */
	private SelectionInfo mCursorSelection;


	private final int[] mTempCoords = new int[2];


	private OnCursorStateChangedListener mOnCursorStateChangedListener;

	/**
	 * DONT ACCESS DIRECTLY, use getSelectionController() instead
	 */
	private SelectionCursorController mSelectionController;
	
	/**
	 * the position of ObservableScrollView
	 */
	private Rect mParentRect;
	

	@SuppressWarnings("unused")
	public SelectableTextView(Context context) {
		super(context);
		init();
	}

	@SuppressWarnings("unused")
	public SelectableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	@SuppressWarnings("unused")
	public SelectableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mParentRect = new Rect();
		mCursorSelection = new SelectionInfo();
		mSelectionController = new SelectionCursorController();

		final ViewTreeObserver observer = getViewTreeObserver();
		if (observer != null) {
			observer.addOnTouchModeChangeListener(mSelectionController);
		}
		
		setOnClickListener(this);
		setOnLongClickListener(this);

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// TextView will take care of the color span of the text when it's being scroll by
		// its parent ScrollView. But the cursors position is not handled. Calling snapToSelection
		// will move the cursors along with the selection.
		if (getParent() instanceof ObservableScrollView) {
			((ObservableScrollView) getParent()).addOnScrollChangedListener(new OnScrollChangedListener() {
				@Override
				public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
//					mSelectionController.snapToSelection();
					//滚动scrollview的时候取消反选
					hideCursor();
				}
			});
		}
	}
	
	

	@Override
	protected void onSizeChanged(int w1, int h1, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w1, h1, oldw, oldh);
		
		//获得父类scrollview的边界
		ObservableScrollView parent = (ObservableScrollView) getParent();
		mParentRect.left = parent.getLeft();
		mParentRect.top = parent.getTop();
		mParentRect.right = parent.getRight();
		mParentRect.bottom = parent.getBottom();
		
	}

	public void setDefaultSelectionColor(int color) {
		mDefaultSelectionColor = color;
	}
	
	/**
	 * 设置弹出菜单，自定义view
	 * @param view
	 */
	public void setPopupMenuView(View view){
		this.mSelectionController.setPopupMenuView(view);
	}

	/**
	 * set the selection beginning at {@code start} with the specified {@code length} for the given
	 * {@code duration}
	 *
	 * @param color    the color of the selection
	 * @param start    the start offset
	 * @param length   the length of the selection
	 * @param duration the duration the selection, in second, to be last, -1 to indicate forever
	 */
	public void setSelection(int color, int start, int length, int duration) {

		// make sure we are not selecting beyond the text
		int end = start + length >= getText().length() ? getText().length() - 1 : start + length;

		if (start + length > getText().length() || end <= start) {
			return ;
		}

		/* My Note
         have to create a new one each time instead of using the {@code set} method.
         if I use set, will mess up the removeSelection function as the content of the
         reference has changed
		 */
		mCursorSelection = new SelectionInfo(getText(), new BackgroundColorSpan(color), start, end);
		mCursorSelection.select();
		removeSelection(duration);
	}

	/**
	 * set the selection beginning at {@code start} with the specified {@code length} for the given
	 * {@code duration} with the default color
	 *
	 * @param start    the start offset
	 * @param length   the length of the selection
	 * @param duration the duration of the selection to be last, -1 to indicate forever
	 */
	public void setSelection(int start, int length, int duration) {
		setSelection(mDefaultSelectionColor, start, length, duration);
	}

	/**
	 * set the selection beginning at {@code start} with the spcified {@code length}
	 *
	 * @param start  the starting offset
	 * @param length the length of the selection
	 */
	public void setSelection(int start, int length) {
		setSelection(start, length, -1);
	}

	/**
	 * removes the current selection immediately
	 */
	public void removeSelection() {
		mCursorSelection.remove();
	}

	public void removeSelection(int delay) {
		removeSelection(mCursorSelection, delay);
	}

	/**
	 * remove the given span style from the main text after the specified time
	 *
	 * @param selection the span style to be removed
	 * @param delay     the milliseconds to wait before remove the style
	 */
	public void removeSelection(final SelectionInfo selection, int delay) {
		if (delay >= 0) {
			Runnable runnable = new Runnable() {
				public void run() {
					selection.remove();
				}
			};
			postDelayed(runnable, delay);
		}
	}


	/**
	 * show the selection cursors and select the text between the specified offset
	 *
	 * @param start the start offset
	 * @param end   the end offset
	 */
	public void showSelectionControls(int start, int end) {
		assert (start >= 0);
		assert (end >= 0);
		assert (start < getText().length());
		assert (end < getText().length());

		mSelectionController.show(start, end);
	}

	/**
	 * 外部接口，获得选中的文字等信息，注意是charesequence类的
	 * @return the current selection information
	 */
	public SelectionInfo getCursorSelection() {
		return mCursorSelection;
	}


	/**
	 * set the OnCursorHiddenListner when then the cursors hide
	 *
	 * @param onCursorStateChangedListener the OnCursorVisibilityChangedListener
	 */
	public void setOnCursorStateChangedListener(OnCursorStateChangedListener onCursorStateChangedListener) {
		mOnCursorStateChangedListener = onCursorStateChangedListener;
	}

	/**
	 * @return the y position
	 */
	private int getScrollYInternal() {
		int y = this.getScrollY();

		// a TextView inside a ScrollView is not scrolled, so getScrollY() returns 0.
		// We must use getScrollY() from the ScrollView instead
		if (this.getParent() instanceof ScrollView) {
			y += ((ScrollView) this.getParent()).getScrollY();

			// do this to compensate for the height of the status bar
			final int[] coords = mTempCoords;
			((ScrollView) this.getParent()).getLocationInWindow(coords);
			y -= coords[1];
            
			/* My Note
             getLocationInWindow(coords) for the TextView will return a negative
             value if we scroll down as the top the TextView is beyong the top
             of the screen.
             
             So maybe we could replace SrollView.getScrollY() with getWindowsInLocation(coords)
			 */
		}

		return y;
	}

	/**
	 * the current x position of the TextView taking into account the scroll (if it's inside a
	 * ScrollView) and the padding of the scrollview
	 *
	 * @return the x position
	 */
	private int getScrollXInternal() {
		int x = this.getScrollX();

		// a TextView inside a ScrollView is not scrolled, so getScrollX() returns 0.
		// We must use getScrollX() from the ScrollView instead
		if (this.getParent() instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) this.getParent();
            
			x += scrollView.getScrollX();
            
			final int[] coords = mTempCoords;
			scrollView.getLocationInWindow(coords);
			x -= coords[0];
			x -= scrollView.getPaddingLeft();
		}
		return x;
	}

	/**
	 * Gets the character offset of (x, y). If (x, y) lies on the right half of the character, it
	 * returns the offset of the next character. If (x, y) lies on the left half of the character, it
	 * returns the offset of this character.
	 *
	 * @param x x coordinate relative to this TextView
	 * @param y y coordinate relative to this TextView
	 * @return the offset at (x,y), -1 if error occurs
	 */
	public int getOffset(int x, int y) {
		Layout layout = getLayout();
		int offset = -1;

		if (layout != null) {
			int topVisibleLine = layout.getLineForVertical(y);
			offset = layout.getOffsetForHorizontal(topVisibleLine, x);
		}

		return offset;
	}

	/**
	 * Gets the character offset where (x, y) is pointing to.
	 *
	 * @param x x coordinate relative to this TextView
	 * @param y y coordinate relative to this TextView
	 * @return the offset at (x, y), -1 if error occurs
	 *
	 * @see {@link #getOffset(int, int)}
	 */
	public int getPreciseOffset(int x, int y) {
		Layout layout = getLayout();

		if (layout != null) {
			int topVisibleLine = layout.getLineForVertical(y);
			int offset = layout.getOffsetForHorizontal(topVisibleLine, x);

			int offset_x = (int) layout.getPrimaryHorizontal(offset);
			if (offset_x > x) {
				return layout.getOffsetToLeftOf(offset);
			}
		}
		return getOffset(x, y);
	}

	////////////////////////////////////////////////
	// copied & modified from Android source code //
	////////////////////////////////////////////////

	/**
	 * Get the offset character closest to the specified absolute position.
	 *
	 * @param x The horizontal absolute position of a point on screen
	 * @param y The vertical absolute position of a point on screen
	 * @return the character offset for the character whose position is closest to the specified
	 *         position. Returns -1 if there is no layout.
	 */
    //	@SuppressWarnings("unused")
    //	public int getOffsetFromRaw(int x, int y) {
    //		if (getLayout() == null) return -1;
    //
    //		y -= getTotalPaddingTop();
    //		// Clamp the position to inside of the view.
    //		y = Math.max(0, y);
    //		y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
    //		y += getScrollY();
    //
    //		// if the textview is inside a scrollview, the above getScrollY() will return 0
    //		// even if it's scrolled. Must do getScrollY() from the ScrollView instead.
    //		if (this.getParent() instanceof ScrollView) {
    //			ScrollView scrollView = (ScrollView) this.getParent();
    //			y += scrollView.getScrollY();
    //
    //			// do this to compensate for the height of the status bar
    //			final int[] coords = mTempCoords;
    //			scrollView.getLocationInWindow(coords);
    //			y -= coords[1];
    //		}
    //
    //		final int line = getLayout().getLineForVertical(y);
    //		final int offset = getOffsetForHorizontal(line, x);
    //		return offset;
    //	}
    
	////////////////////////////////////////////////
	// copied & modified from Android source code //
	////////////////////////////////////////////////

	/**
	 * Get the offset character closest to the specified absolute position. If the resulting offset is
	 * too close to the previous offset, return the previous offset instead.
	 *
	 * @param x              raw x
	 * @param y              raw y
	 * @param previousOffset previous offset
	 * @return offset of the specified (x,y)
	 */
	private int getHysteresisOffset(int x, int y, int previousOffset) {
		final Layout layout = getLayout();
		if (layout == null) return -1;

        
		y += getScrollYInternal();
		x += getScrollXInternal();
        
		int line = getLayout().getLineForVertical(y);
        
		// The "HACK BLOCK"S in this function is required because of how Android Layout for
		// TextView works - if 'offset' equals to the last character of a line, then
		//
		// * getLineForOffset(offset) will result the NEXT line
		// * getPrimaryHorizontal(offset) will return 0 because the next insertion point is on the next line
		// * getOffsetForHorizontal(line, x) will not return the last offset of a line no matter where x is
		// These are highly undesired and is worked around with the HACK BLOCK
		//
		// @see Moon+ Reader/Color Note - see how it can't select the last character of a line unless you move
		// the cursor to the beginning of the next line.
		//
		////////////////////HACK BLOCK////////////////////////////////////////////////////
		if (isEndOfLineOffset(previousOffset)) {
			// we have to minus one from the offset so that the code below to find
			// the previous line can work correctly.
			int left = (int) layout.getPrimaryHorizontal(previousOffset - 1);
			int right = (int) layout.getLineRight(line);
			int threshold = (right - left) / 2; // half the width of the last character
			if (x > right - threshold) {
				previousOffset -= 1;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////

		final int previousLine = layout.getLineForOffset(previousOffset);
		final int previousLineTop = layout.getLineTop(previousLine);
		final int previousLineBottom = layout.getLineBottom(previousLine);
		final int hysteresisThreshold = (previousLineBottom - previousLineTop) / 2;

		// If new line is just before or after previous line and y position is less than
		// hysteresisThreshold away from previous line, keep cursor on previous line.
		if (((line == previousLine + 1) && ((y - previousLineBottom) < hysteresisThreshold)) ||
            ((line == previousLine - 1) && ((previousLineTop - y) < hysteresisThreshold)))
		{
			line = previousLine;
		}

		int offset = layout.getOffsetForHorizontal(line, x);


		// This allow the user to select the last character of a line without moving the
		// cursor to the next line. (As Layout.getOffsetForHorizontal does not return the
		// offset of the last character of the specified line)
		//
		// But this function will probably get called again immediately, must decrement the offset
		// by 1 to compensate for the change made below. (see previous HACK BLOCK)
		/////////////////////HACK BLOCK///////////////////////////////////////////////////
		if (offset < getText().length() - 1) {
			if (isEndOfLineOffset(offset + 1)) {
				int left = (int) layout.getPrimaryHorizontal(offset);
				int right = (int) layout.getLineRight(line);
				int threshold = (right - left) / 2; // half the width of the last character
				if (x > right - threshold) {
					offset += 1;
				}
			}
		}
		//////////////////////////////////////////////////////////////////////////////////

		return offset;
	}


	/**
	 * Checks whether the specified offset is at the end of a line.
	 * <p/>
	 * PRECONDITION: assumes layout exists and is valid
	 *
	 * @param offset the offset to check
	 * @return true if the offset is at the end of a line, false otherwise.
	 */
	private boolean isEndOfLineOffset(int offset) {
		if (offset > 0) {
			return getLayout().getLineForOffset(offset) ==
					  getLayout().getLineForOffset(offset - 1) + 1;
		}
		return false;
	}

	////////////////////////////////////////////////
	// copied & modified from Android source code //
	////////////////////////////////////////////////
    
	/**
	 * get the offset given the line and the raw x
	 *
	 * @param line the line
	 * @param x    raw x
	 * @return the offset
	 */
	private int getOffsetForHorizontal(int line, int x) {
		x -= getTotalPaddingLeft();
		// Clamp the position to inside of the view.
		x = Math.max(0, x);
		x = Math.min(getWidth() - getTotalPaddingRight() - 1, x);
		x += getScrollXInternal();
        
		return getLayout().getOffsetForHorizontal(line, x);
	}

	/**
	 * get the (x,y) screen coordinates from the specified offset.
	 *
	 * @param offset   the offset
	 * @param scroll_x the horizontal scroll distance to take away
	 * @param scroll_y the horizontal scroll distance to take away
	 * @param coords   the returned x, y coordinate array, must have a length of 2
	 */
	private void getXY(int offset, int scroll_x, int scroll_y, int[] coords) {
		assert (coords.length >= 2);

		coords[0] = coords[1] = -1;
		Layout layout = getLayout();

		if (layout != null) {
			int line = layout.getLineForOffset(offset);
			int base = layout.getLineBottom(line);

			coords[0] = (int) layout.getPrimaryHorizontal(offset) - scroll_x; // x
			coords[1] = base - scroll_y; // y
		}
	}

	/**
	 * Get the (x,y) screen coordinate from the specified offset. If the specified offset is beyond the
	 * end of the line, move the offset to the beginning of the next line.
	 *
	 * @param offset   the offset
	 * @param scroll_x the horizontal scroll distance to take away
	 * @param scroll_y the horizontal scroll distance to take away
	 * @param coords   the returned x, y coordinate array, muust have a length of 2
	 */
	private void getAdjusteStartXY(int offset, int scroll_x, int scroll_y, int[] coords) {
		if (offset < getText().length()) {
			final Layout layout = getLayout();
			if (layout != null) {
				if (isEndOfLineOffset(offset + 1)) {
					float a = layout.getPrimaryHorizontal(offset);
					float b = layout.getLineRight(layout.getLineForOffset(offset));
					if (a == b) {
						// this means the we encounter a new line character, i think.
						offset += 1;
					}
				}
			}
		}
		getXY(offset, scroll_x, scroll_y, coords);
	}

	/**
	 * Get the (x,y) screen coordinate from the specified offset. If the offset is the at the end of a
	 * wrapped line, return the (x,y) at the end of that line instead of the (x, y) at the beginning of
	 * the next line (which is the default behaviour for Android)
	 *
	 * @param offset   the offset
	 * @param scroll_x the horizontal scroll distance to take away
	 * @param scroll_y the horizontal scroll distance to take away
	 * @param coords   the returned x, y coordinate array, must have a length of 2
	 */
	private void getAdjustedEndXY(int offset, int scroll_x, int scroll_y, int[] coords) {
		if (offset > 0) {
			final Layout layout = getLayout();
			if (layout != null) {
				//if (this_line > prev_line) {
				if (isEndOfLineOffset(offset)) {
					// if we are at the end of a line, calculate the X using getLineRight instead of
					// getPrimaryHorizontal.
					// (Because getPrimaryHorizontal returns 0 for offset sitting at the end of a line.
					// getPrimaryHorizontal returns the next insertion point, which will be the next line)
					int prev_line = layout.getLineForOffset(offset - 1);
					float right = layout.getLineRight(prev_line);
					int y = layout.getLineBottom(prev_line);
					coords[0] = (int) right - scroll_x;
					coords[1] = y - scroll_y;
					return;
				}
			}
		}
		getXY(offset, scroll_x, scroll_y, coords);
	}

	
	@Override
	public void onClick(View v) {
		hideCursor();
	}


	public boolean onLongClick(View v) {
//		Log.d("inner long click", "true");
		if(mSelectionController.isShowing()){
			hideCursor();
		}
		showSelectionCursors();
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
//		Log.d("inner touch event", "true");
		mTouchX = (int) event.getX();
		mTouchY = (int) event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			int left = getLeft();
			int right = getRight();
			int top = getTop();
			int bottom = getBottom();
			Log.d("onTouchEvent", "up");

			
			
			break;

		default:
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * hide the cursors of text selector
	 */
	public void hideCursor() {
		mSelectionController.hide();
	}
	
	/**
	 * show the cursors of text selector
	 */
	public void showSelectionCursors() {
		String text = getText().toString();
		char[] charArr = text.toCharArray();
		int start = getPreciseOffset(mTouchX, mTouchY);
		
		int end = start + 1;
		
		if (start <= -1 || end > text.length() || start > text.length()) {
			return;
		}
		
		if(charArr[start] == SPACE_CODE){//跳过空格
			start++;
		}
		
		if(!isEnglishChar(charArr[start])){
			//如果不是英文字母，则默认选中指定个数
			end = start + DEFAULT_SELECTION_LEN;
			if (end >= getText().length()) {
				end = getText().length() - 1;
			}
		}else{
			//如果是英文字母，则选中该单词
			for(int i = start; i >= 0; i--){
				if(!isEnglishChar(charArr[i])){
					break;
				}
				start--;
			}
			start++; //跳过空格
			for(int i = end; i < text.length(); i++){
				if(!isEnglishChar(charArr[i])){
					break;
				}
				end++;
			}
		}
		
		showSelectionControls(start, end);
	}
	
	public boolean isEnglishChar(char ch)
	{
		if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '\'' && ch <= '-')
		{
			return true;
		}
		return false;
	}

	
    
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////////INNER CLASSES//////////////////////////////////
	///////// copied, heavily modified and simplified from TextView ////////////
	////////////////////////////////////////////////////////////////////////////





	/**
	 * Manages the two cursors that control the selection. Internal used only. For outsider, please use
	 * SelectionModifier
	 */
	private class SelectionCursorController implements ViewTreeObserver.OnTouchModeChangeListener {

		/**
		 * The two cursor handle that controls the selection. Note that the two cursors are allowed to
		 * swap positions and thus the name of the handle has no bearing on the relative position of the
		 * handle to each other. (e.g. mStartHandle can be positioned leagally at an offset greater than
		 * the offset mEndHandle is resting on)
		 */
		private CursorHandle mStartHandle;
		private CursorHandle mEndHandle;
		private MenuHandle mMenuHandle;


		/**
		 * whether the selection controller is displaying on the screen
		 */
		private boolean mIsShowing;


		public SelectionCursorController() {
			mStartHandle = new CursorHandle(this);
			mEndHandle = new CursorHandle(this);
			mMenuHandle = new MenuHandle();
		}
		
		public void setPopupMenuView(View view){
			this.mMenuHandle.setContentView(view);
		}


		/**
		 * snap the cursors to the current selection
		 */
		public void snapToSelection() {

			if (mIsShowing) {
				int a = SelectableTextView.this.getCursorSelection().getStart();
				int b = SelectableTextView.this.getCursorSelection().getEnd();

				int start = Math.min(a, b);
				int end = Math.max(a, b);

				// find the corresponding handle for the start/end calculated above
				CursorHandle startHandle = start == a ? mStartHandle : mEndHandle;
				CursorHandle endHandle = end == b ? mEndHandle : mStartHandle;

				final int[] coords = mTempCoords;
				int scroll_y = SelectableTextView.this.getScrollYInternal();
				int scroll_x = SelectableTextView.this.getScrollXInternal();

				SelectableTextView.this.getAdjusteStartXY(start, scroll_x, scroll_y, coords);
				startHandle.pointTo(coords[0], coords[1]);
				final int[] startCoords = coords.clone();

				SelectableTextView.this.getAdjustedEndXY(end, scroll_x, scroll_y, coords);
				endHandle.pointTo(coords[0], coords[1]);
				
				//设置menu
				int menuX = (coords[0] + startCoords[0] - mMenuHandle.getWidth()) / 2;
				int menuY = Math.min(coords[1], startCoords[1]) - mMenuHandle.getHeight() - getLineHeight();
				mMenuHandle.pointTo(menuX, menuY);
			}
		}
        
		/**
		 * show the selection cursor at the specified offsets and select the text between the specified
		 * offsets.
		 *
		 * @param start the offset of the first cursor
		 * @param end   the offset of the second cursor
		 */
		public void show(int start, int end) {

			int a = Math.min(start, end);
			int b = Math.max(start, end);

			final int[] coords = mTempCoords;
			int scroll_y = SelectableTextView.this.getScrollY();
			int scroll_x = SelectableTextView.this.getScrollX();

			SelectableTextView.this.getAdjusteStartXY(a, scroll_x, scroll_y, coords);
			mStartHandle.show(coords[0], coords[1]);
			//save the coords of startHandle
			final int[] startCoords = coords.clone();

			SelectableTextView.this.getAdjustedEndXY(b, scroll_x, scroll_y, coords);
			mEndHandle.show(coords[0], coords[1]);

			mIsShowing = true;
			select(a, b);


			int menuX = (coords[0] + startCoords[0] - mMenuHandle.getWidth()) / 2;
			int menuY = Math.min(coords[1], startCoords[1]) - mMenuHandle.getHeight() - getLineHeight();
			mMenuHandle.show(menuX, menuY);
			
			if (mOnCursorStateChangedListener != null) {
				mOnCursorStateChangedListener.onShowCursors(SelectableTextView.this);
		    }
		}


		/**
		 * hide the cursor and selection
		 */
		public void hide() {
			if (mIsShowing) {
				SelectableTextView.this.removeSelection();
				mStartHandle.hide();
				mEndHandle.hide();
				mMenuHandle.hide();
				
				mIsShowing = false;
                
				if (mOnCursorStateChangedListener != null) {
					mOnCursorStateChangedListener.onHideCursors(SelectableTextView.this);
				}
			}
		}


		/**
		 * redraw the moving cursor and update the selection if required
		 *
		 * @param cursorHandle the CursorHandle (LEFT or RIGHT)
		 * @param x            the x coordinate the cursor is pointing to on the screen (raw)
		 * @param y            the y coordinate the cursor is pointing to on the screen (raw)
		 * @param oldx         the previous x position the cursor pointed to
		 * @param oldy         the previous y position the cursor pointed to
		 */
		public void updatePosition(CursorHandle cursorHandle, int x, int y, int oldx, int oldy) {
			if (!mIsShowing) {
				return;
			}

			int old_offset =
            cursorHandle == mStartHandle ?
            SelectableTextView.this.getCursorSelection().getStart() :
            SelectableTextView.this.getCursorSelection().getEnd();
            
			int offset = SelectableTextView.this.getHysteresisOffset(x, y, old_offset);

			if (offset != old_offset) {

				if (cursorHandle == mStartHandle) {
					SelectableTextView.this.getCursorSelection().setStart(offset);
				}
				else {
					SelectableTextView.this.getCursorSelection().setEnd(offset);
				}
				SelectableTextView.this.getCursorSelection().select();
			}
            
			cursorHandle.pointTo(x, y);
			
			do{
				//set the position of menu
				int menuX = (mStartHandle.getCurX() + mEndHandle.getCurX() - mMenuHandle.getWidth()) / 2;
				int menuY = Math.min(mStartHandle.getCurY(), mEndHandle.getCurY()) - mMenuHandle.getHeight() - getLineHeight();
				mMenuHandle.pointTo(menuX, menuY);
				
//				Log.d("x:y", mStartHandle.getCurX() + "-" + mEndHandle.getCurY());
			}while(false);
            
			if (mOnCursorStateChangedListener != null) {
				mOnCursorStateChangedListener.onPositionChanged(SelectableTextView.this, x, y, oldx, oldy);
			}
		}
        
		/**
		 * Select the textview between start and end.
		 * <p/>
		 * Precondition: start, end must be valid
		 *
		 * @param start the starting offset
		 * @param end   the ending offset
		 */
		private void select(int start, int end) {
			SelectableTextView.this.setSelection(Math.min(start, end), Math.abs(end - start));
		}
        
		@SuppressWarnings("unused")
		public boolean isShowing() {
			return mIsShowing;
		}
        
		/**
		 * Called when the view is detached from window. Perform house keeping task, such as stopping
		 * Runnable thread that would otherwise keep a reference on the context, thus preventing the
		 * activity from being recycled.
		 */
		@SuppressWarnings("unused")
		public void onDetached() {
			// don't know what to do here
		}
        
        
		@Override
		public void onTouchModeChanged(boolean isInTouchMode) {
			if (!isInTouchMode) {
				hide();
			}
		}
	}
  
	/**
	 * represents a single cursor
	 */
	private class CursorHandle extends View {
        
		/**
		 * the {@link PopupWindow} containing the cursor drawable
		 */
		private final PopupWindow mContainer;
        
		/**
		 * the drawble of the cursor
		 */
		private Drawable mDrawable;
        
		/**
		 * whether the user is dragging the cursor
		 */
		@SuppressWarnings("unused")
		private boolean mIsDragging;
        
		/**
		 * the controller that's controlling the cursor
		 */
		private SelectionCursorController mController;
        
		/**
		 * the height of the cursor
		 */
		private int mHeight;
        
		/**
		 * the width of the cursor
		 */
		private int mWidth;
        
		/**
		 * the x coordinate of the "pointer" of the cursor
		 */
		private int mHotspotX;
        
		/**
		 * the y coordinate of the "pointer" of the cursor which is usually the top, so it's zero.
		 */
		private int mHotspotY;
        
        
		/**
		 * Adjustment to add to the Raw x, y coordinate of the touch position to get the location of where
		 * the cursor is pointing to
		 */
		private int mAdjustX;
		private int mAdjustY;
        
		private int mOldX;
		private int mOldY;
		
		private int mCurX;
		private int mCurY;
        
		public CursorHandle(SelectionCursorController controller) {
			super(SelectableTextView.this.getContext());
            
			mController = controller;
            
			mDrawable = getResources().getDrawable(R.drawable.select_cursor);
            
			/* My Note
             At first I tried using mContainer = new PopupWindow(SelectableTextView.this.getContext())
             and mContainer.setContentView(this) in the show() method AND FAILED to draw the
             PopupWindow properly (e.g. PopupWindow can't contain the whole drawable, background
             of the PopupWindow is not transparent. I think it's because calling
             new PopupWindow(context) uses the internal's default style which messes up
			 */
			mContainer = new PopupWindow(this);
			// mContainer.setSplitTouchEnabled(true);
			mContainer.setClippingEnabled(false);
            
			/* My Note
             getIntrinsicWidth() returns the width of the drawable after it has been
             scaled to the current device's density
             e.g. if the drawable is a 15 x 20 image and we load the image on a Nexus 4 (which
             has a density of 2.0), getIntrinsicWidth() shall return 15 * 2 = 30
			 */
			mHeight = mDrawable.getIntrinsicHeight() * 2;
			mWidth = mDrawable.getIntrinsicWidth() * 2;
            
			// the PopupWindow has an initial dimension of (0, 0)
			// must set the width/height of the popupwindow in order for it to be drawn
			mContainer.setWidth(mWidth);
			mContainer.setHeight(mHeight);
            
			// this is the location of where the pointer is relative to the cursor itself
			// if the left and right cursor are different, mHotspotX will need to be calculated
			// differently for each cursor. Currently, I'm using the same left and right cursor
			mHotspotX = mWidth / 2;
			mHotspotY = 0;
            
			invalidate();
		}
        
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(mWidth, mHeight);
		}
        
		@Override
		protected void onDraw(Canvas canvas) {
			mDrawable.setBounds(0, 0, mWidth, mHeight);
			mDrawable.draw(canvas);
		}
		

		@Override
		public boolean onTouchEvent(MotionEvent event) {
            
			int rawX = (int) event.getRawX();
			int rawY = (int) event.getRawY();
            
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// calculate distance from the (x,y) of the finger to where the cursor
					// points to
					mAdjustX = mHotspotX - (int) event.getX();
					mAdjustY = mHotspotY - (int) event.getY();
					mOldX = mAdjustX + rawX;
					mOldY = mAdjustY + rawY;
                    
					mIsDragging = true;
					if (SelectableTextView.this.mOnCursorStateChangedListener != null) {
						SelectableTextView.this.mOnCursorStateChangedListener.onDragStarts(SelectableTextView.this);
					}
					break;
				}
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					mIsDragging = false;
					mController.snapToSelection();
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// calculate the raw (x, y) the cursor is POINTING TO
					
					if (mParentRect.left <= rawX && rawX <= mParentRect.right 
						&& mParentRect.top + this.mHeight <= rawY && rawY <= mParentRect.bottom + this.mHeight) {
						mController.updatePosition(this, mCurX, mCurY, mOldX, mOldY);
						mCurX = mAdjustX + rawX;
						mCurY = mAdjustY + rawY;
						mOldX = mCurX;
						mOldY = mCurY;
					}
					
					break;
				}
			}
			return true; // consume the event
		}
        
		public int getCursorHeight(){
			return this.mHeight;
		}
		
		public int getCursorWidth(){
			return this.mWidth;
		}
		
		public int getCurX(){
			return mCurX;
		}
		
		public int getCurY(){
			return mCurY;
		}
		
        
		public boolean isShowing() {
			return mContainer.isShowing();
		}
        
		/**
		 * Show the cursor pointing to the specified point.
		 *
		 * @param x the x coordinate of the point relative to the TextView
		 * @param y the y coordinate of the point relative to the TextView
		 */
		public void show(int x, int y) {
			final int[] coords = mTempCoords;
			SelectableTextView.this.getLocationInWindow(coords);
            
			coords[0] += x - mHotspotX;
			coords[1] += y - mHotspotY;
			this.mCurX = coords[0];
			this.mCurY = coords[1];
			mContainer.showAtLocation(SelectableTextView.this, Gravity.NO_GRAVITY, coords[0], coords[1]);
		}
        
		/**
		 * move the cursor to point the (x,y) location on the screen.
		 *
		 * @param x the x coordinate on the screen
		 * @param y the y coordinate on the screen
		 */
		private void pointTo(int x, int y) {
			if (isShowing()) {
				mContainer.update(x - mHotspotX, y - mHotspotY, -1, -1);
			}
		}
        
        
		/**
		 * hide this cursor
		 */
		public void hide() {
			mIsDragging = false;
			mContainer.dismiss();
		}
        
		@SuppressWarnings("unused")
		public void dismiss() {
			mContainer.dismiss();
			onDetached();
		}
        
		private void onDetached() {
			// hide action windows
            
			// This is not used but kept for future reference.
			// Since no action windows is associate with the cursor, nothing is removed.
			// I've made the action window a task of the View using SelectableTextView instead of
			// embedding it in here.
		}
        
	}

	public interface OnCursorStateChangedListener {
		/**
		 * What to do when the cursors is hidden from the view
		 *
		 * @param v the view the cursor belongs to
		 */
		public void onHideCursors(View v);

		/**
		 * What to do when the cursors show
		 *
		 * @param v the view the cursor belongs to
		 */
		public void onShowCursors(View v);

		/**
		 * What to do when the drag begins
		 *
		 * @param v the view the cursor belongs to
		 */
		public void onDragStarts(View v);

		public void onPositionChanged(View v, int x, int y, int oldx, int oldy);
	}
	
	
	private class MenuHandle{

		/**
		 * the {@link PopupWindow} containing the cursor drawable
		 */
		private PopupWindow mContainer;
		
		private int width = 0;
		private int height = 0;
        
        
		public MenuHandle() {
			mContainer = new PopupWindow();

		}
		
		public void setContentView(View view){
			mContainer.setContentView(view);
			mContainer.setWidth(LayoutParams.WRAP_CONTENT);
			mContainer.setHeight(LayoutParams.WRAP_CONTENT);
			mContainer.setClippingEnabled(false);
			//测量view的宽高，否则之后无法获取
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			this.width = view.getMeasuredWidth();
			this.height = view.getMeasuredHeight();
		}
		
		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		} 
        
		public boolean isShowing() {
			return mContainer.isShowing();
		}
        
		/**
		 * Show the cursor pointing to the specified point.
		 *
		 * @param x the x coordinate of the point relative to the TextView
		 * @param y the y coordinate of the point relative to the TextView
		 */
		public void show(int x, int y) {
			View view = mContainer.getContentView();
			if(view == null){
				return;
			}
			int cw = SelectableTextView.this.getMeasuredWidth();
			int iw = view.getMeasuredWidth();
			x = x + iw >= cw ? cw - iw : x;
			x = x < 0 ? 0 : x;
			mContainer.showAtLocation(SelectableTextView.this, Gravity.NO_GRAVITY, x, y);
		}
        
		/**
		 * move the cursor to point the (x,y) location on the screen.
		 *
		 * @param x the x coordinate on the screen
		 * @param y the y coordinate on the screen
		 */
		private void pointTo(int x, int y) {
			if (isShowing()) {
				View view = mContainer.getContentView();
				if(view == null){
					return;
				}
				
				int cw = SelectableTextView.this.getMeasuredWidth();
				int iw = view.getMeasuredWidth();
//				Log.d("widht ----- ", cw + ":" + iw);
				Log.d("x : iw----- ", x + ":" + iw);

				x = x + iw >= cw ? cw - iw : x;
				x = x < 0 ? 0 : x;
				mContainer.update(x, y, -1, -1);
			}
		}
        
        
		/**
		 * hide this cursor
		 */
		public void hide() {
			mContainer.dismiss();
		}
        
		@SuppressWarnings("unused")
		public void dismiss() {
			mContainer.dismiss();
			onDetached();
		}
        
		private void onDetached() {
			// hide action windows
		}

	}
	public class SelectionInfo {
	    
		private Object mSpan;
		private int mStart;
		private int mEnd;
		private Spannable mSpannable;
	    
		public SelectionInfo() {
			clear();
		}
	    
		public SelectionInfo(CharSequence text, Object span, int start, int end) {
			set(text, span, start, end);
		}
	    
		/**
		 * select the {@link #getSpannable()} between the offsets {@link this.getStart()} and
		 * {@link #getEnd()}
		 */
		public void select() {
			select(mSpannable);
		}
	    
		public void select(Spannable text) {
			if (text != null) {
				text.removeSpan(mSpan);
				text.setSpan(mSpan, Math.min(mStart, mEnd), Math.max(mStart, mEnd), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			}
		}
	    
		/**
		 * remove the the selection
		 */
		public void remove() {
			remove(mSpannable);
		}
	    
		public void remove(Spannable text) {
			if (text != null) {
				text.removeSpan(mSpan);
			}
		}
	    
		public void clear() {
			mSpan = null;
			mSpannable = null;
			mStart = 0;
			mEnd = 0;
		}
	    
		public void set(Object span, int start, int end) {
			mSpan = span;
			mStart = start;
			mEnd = end;
		}
	    
		public void set(CharSequence text, Object span, int start, int end) {
			if (text instanceof Spannable) {
				mSpannable = (Spannable) text;
			}
			set(span, start, end);
		}
	    
		public CharSequence getSelectedText() {
			if (mSpannable != null) {
				int start = Math.min(mStart, mEnd);
				int end = Math.max(mStart, mEnd);
	            
				if (start >= 0 && end < mSpannable.length()) {
					return mSpannable.subSequence(start, end);
				}
			}
			return "";
		}
	    
		public Object getSpan() {
			return mSpan;
		}
	    
		public void setSpan(CharacterStyle span) {
			mSpan = span;
		}
	    
	    
		/**
		 * get the starting offset of the selection. Note the the starting offset is
		 * not necessarily smaller than the ending offset
		 * @return the starting offset of the selection
		 */
		public int getStart() {
			return mStart;
		}
	    
		/**
		 * set the starting offset of the selection (inclusive)
		 * @param start the starting offset. It can be larger than {@link #getEnd()}
		 */
		public void setStart(int start) {
			assert (start >= 0);
			mStart = start;
		}
	    
		/**
		 * get the ending offset of the selection. Note the the ending offset is
		 * not necessarily larger than the starting offset
		 * @return the ending offset of the selection
		 */
		public int getEnd() {
			return mEnd;
		}
	    
		/**
		 * set the ending offset of the selection (exclusive)
		 * @param end the ending offset. It can be smaller than {@link #getStart()}
		 */
		public void setEnd(int end) {
			assert (end >= 0);
			mEnd = end;
		}
	    
		public Spannable getSpannable() {
			return mSpannable;
		}
	    
		public void setSpannable(Spannable spannable) {
			mSpannable = spannable;
		}
	    
		/**
		 * Checks the weather the specified offset is within the range of the selection
		 * @param offset the offset to check
		 * @return true if the offset is within the range of the selection, false otherwise.
		 */
		public boolean offsetInSelection(int offset) {
			return (offset >= mStart && offset <= mEnd) ||
	        (offset >= mEnd && offset <= mStart);
		}
	}

}
