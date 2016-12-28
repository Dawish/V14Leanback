/*
 * Copyright (C) 2016 Clendy <yc330483161@163.com | yc330483161@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clendy.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

import io.github.clendy.leanback.utils.LayoutManagerHelper;


/**
 * @author Clendy 2016/12/23 023 13:24
 */
public class SpanGridView extends RecyclerView implements View.OnClickListener,
        View.OnFocusChangeListener, ViewTreeObserver.OnGlobalFocusChangeListener {

    private static final String TAG = SpanGridView.class.getSimpleName();

    private int mPendingSelectionInt = NO_POSITION;

    private FocusArchivist mFocusArchivist = new FocusArchivist();

    private OnKeyInterceptListener mOnKeyInterceptListener;
    private OnItemFocusChangeListener mOnItemFocusChangeListener;
    private OnItemClickListener mOnItemClickListener;
    private RecyclerViewBring mRecyclerViewBring;
    private LayoutManagerHelper mManagerHelper;

    public interface OnKeyInterceptListener {
        boolean onInterceptKeyEvent(KeyEvent event);
    }

    public interface OnItemFocusChangeListener {
        void onItemPreSelected(SpanGridView parent, View view, int position);

        void onItemSelected(SpanGridView parent, View view, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(SpanGridView parent, View view, int position, long id);
    }

    public SpanGridView(Context context) {
        this(context, null);
    }

    public SpanGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpanGridView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(context, attrs, defStyle);
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyle) {

        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setHasFixedSize(true);
        setChildrenDrawingOrderEnabled(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setFocusable(true);
        setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewBring = new RecyclerViewBring(this);
        mManagerHelper = LayoutManagerHelper.newInstance(this);
    }

    public OnKeyInterceptListener getOnKeyInterceptListener() {
        return mOnKeyInterceptListener;
    }

    public void setOnKeyInterceptListener(OnKeyInterceptListener onKeyInterceptListener) {
        mOnKeyInterceptListener = onKeyInterceptListener;
    }

    public OnItemFocusChangeListener getOnItemFocusChangeListener() {
        return mOnItemFocusChangeListener;
    }

    public void setOnItemFocusChangeListener(OnItemFocusChangeListener onItemFocusChangeListener) {
        mOnItemFocusChangeListener = onItemFocusChangeListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setSelection(int adapterPosition) {
        mPendingSelectionInt = adapterPosition;
    }

    public int getSelectedItemPosition() {
        View focusedChild = getFocusedChild();
        return getChildAdapterPosition(focusedChild);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mPendingSelectionInt = NO_POSITION;
        mFocusArchivist = new FocusArchivist();
        super.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        Log.i(TAG, "mPendingSelectionInt:" + mPendingSelectionInt);

        if (mPendingSelectionInt != NO_POSITION) {
            setSelectionOnLayout(mPendingSelectionInt);
        } else {
            setSelectionOnFirstLayout();
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }

    private void setSelectionOnFirstLayout() {
        Log.d(TAG, "setSelectionOnFirstLayout");
        if (getChildCount() > 0) {
            RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(0);
            if (holder != null) {
                if (hasFocus() && holder.itemView != null) {
                    holder.itemView.requestFocus();
                }
            }
        }
    }

    private void setSelectionOnLayout(int position) {
        Log.d(TAG, "setSelectionOnLayout position : " + position);
        RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(position);
        if (holder != null) {
            if (hasFocus()) {
                holder.itemView.requestFocus();
            } else {
                mFocusArchivist.archiveFocus(this, holder.itemView);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewTreeObserver obs = getViewTreeObserver();
        obs.addOnGlobalFocusChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        ViewTreeObserver obs = getViewTreeObserver();
        obs.removeOnGlobalFocusChangeListener(this);
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mOnKeyInterceptListener != null && mOnKeyInterceptListener.onInterceptKeyEvent(event)
                || super.dispatchKeyEvent(event);

    }

    private boolean isClickEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER;
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View result = getLayoutManager().onInterceptFocusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        final FocusFinder ff = FocusFinder.getInstance();
        result = ff.findNextFocus(this, focused, direction);
        if (result != null) {
            return result;
        }
        return null;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            View lastFocusedView = mFocusArchivist.getLastFocus(this);
            if (lastFocusedView != null) {
                lastFocusedView.requestFocus();
            } else {
                LayoutManager layoutManager = getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    if (mPendingSelectionInt == -1) {
                        int position = ((LinearLayoutManager) layoutManager).
                                findFirstCompletelyVisibleItemPosition();
                        if (layoutManager.findViewByPosition(position) != null) {
                            layoutManager.findViewByPosition(position).requestFocus();
                        }
                    } else {
                        if (layoutManager.findViewByPosition(mPendingSelectionInt) != null) {
                            layoutManager.findViewByPosition(mPendingSelectionInt).requestFocus();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        mFocusArchivist.archiveFocus(this, child);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE) {
            View focusedChild = getFocusedChild();
            mFocusArchivist.archiveFocus(this, focusedChild);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (mOnItemClickListener != null) {
            child.setClickable(true);
        }
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (hasFocus()) {
            super.addFocusables(views, direction, focusableMode);
        } else if (isFocusable()) {
            views.add(this);
        }
    }

    @Override
    public void bringChildToFront(View child) {
        if (mRecyclerViewBring != null) {
            mRecyclerViewBring.bringChildToFront(this, child);
        } else {
            super.bringChildToFront(child);
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mRecyclerViewBring != null) {
            return mRecyclerViewBring.getChildDrawingOrder(this, childCount, i);
        } else {
            return super.getChildDrawingOrder(childCount, i);
        }
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if (childCount == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(childCount - 1));
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        super.onChildAttachedToWindow(child);
        child.setOnClickListener(this);
        child.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        fireOnItemClickEvent(v);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        setSelection(getChildAdapterPosition(v));
        fireOnItemSelectedEvent(v, hasFocus);
    }

    private void fireOnItemClickEvent(View child) {
        if (mOnItemClickListener != null) {
            int position = getChildAdapterPosition(child);
            long id = getChildItemId(child);
            mOnItemClickListener.onItemClick(this, child, position, id);
        }
    }

    private void fireOnItemSelectedEvent(View v, boolean hasFocus) {
        if (hasFocus) {
            setSelectionOnLayout(getChildLayoutPosition(v));
            smoothScrollToCenter(this, v);
            bringChildToFront(v);
            if (mOnItemFocusChangeListener != null) {
                mOnItemFocusChangeListener.onItemSelected(this, v, getChildLayoutPosition(v));
            }
        } else {
            if (mOnItemFocusChangeListener != null) {
                mOnItemFocusChangeListener.onItemPreSelected(this, v, getChildLayoutPosition(v));
            }
        }
    }

    public boolean isFocusOnLeftmostColumn() {
        if (mManagerHelper != null) {
            mManagerHelper.isFocusOnLeftmostColumn();
        }
        return false;
    }

    public boolean isFocusOnTopmostRow() {
        if (mManagerHelper != null) {
            mManagerHelper.isFocusOnTopmostRow();
        }
        return false;
    }

    public boolean isFocusOnRightmostColumn() {
        if (mManagerHelper != null) {
            mManagerHelper.isFocusOnRightmostColumn();
        }
        return false;
    }

    public boolean isFocusOnBottommostRow() {
        if (mManagerHelper != null) {
            mManagerHelper.isFocusOnBottommostRow();
        }
        return false;
    }

    private void smoothScrollToCenter(RecyclerView parent, View focusChild) {
        if (parent == null || focusChild == null) {
            return;
        }
        if (!(parent.getLayoutManager() instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int orientation = layoutManager.getOrientation();

        int currentX = (layoutManager.getDecoratedLeft(focusChild) -
                layoutManager.getDecoratedLeft(focusChild)) / 2 +
                layoutManager.getDecoratedLeft(focusChild);
        int currentY = (layoutManager.getDecoratedBottom(focusChild) -
                layoutManager.getDecoratedTop(focusChild)) / 2 +
                layoutManager.getDecoratedTop(focusChild);

        if (orientation == HORIZONTAL) {
            int offset = getWidth() / 2 - currentX;
            if (offset < 0) {
                parent.smoothScrollBy(Math.abs(offset), 0);
            } else {
                parent.smoothScrollBy(-Math.abs(offset), 0);
            }
        } else if (orientation == VERTICAL) {
            int offset = getHeight() / 2 - currentY;
            if (offset < 0) {
                parent.smoothScrollBy(0, Math.abs(offset));
            } else {
                parent.smoothScrollBy(0, -Math.abs(offset));
            }
        }
    }
}
