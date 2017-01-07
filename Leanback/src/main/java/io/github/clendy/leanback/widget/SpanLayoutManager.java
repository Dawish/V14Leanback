///*
// * Copyright (C) 2016 Clendy <yc330483161@163.com | yc330483161@outlook.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.github.clendy.leanback.widget;
//
//import android.content.Context;
//import android.graphics.PointF;
//import android.os.Bundle;
//import android.os.Parcel;
//import android.os.Parcelable;
//import android.support.v4.view.ViewCompat;
//import android.support.v7.widget.GridLayoutManager;
//import android.support.v7.widget.LinearSmoothScroller;
//import android.support.v7.widget.OrientationHelper;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//
//import static android.support.v7.widget.RecyclerView.HORIZONTAL;
//import static android.support.v7.widget.RecyclerView.NO_ID;
//import static android.support.v7.widget.RecyclerView.NO_POSITION;
//import static android.support.v7.widget.RecyclerView.VERTICAL;
//
//
//public class SpanLayoutManager extends GridLayoutManager {
//
//    private static final String TAG = SpanLayoutManager.class.getSimpleName();
//
//    private final RecyclerView mBaseGridView;
//    private int mSpanCount = 1;
//
//    private Context mContext;
//
//    private int mFocusPosition = NO_POSITION;
//    private int mFocusPositionOffset = 0;
//
//    private final ViewsStateBundle mChildrenStates = new ViewsStateBundle();
//
//    private final static int MAX_PENDING_MOVES = 10;
//
//    private int mOrientation = HORIZONTAL;
//    private OrientationHelper mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
//
//    private RecyclerView.State mState;
//    private RecyclerView.Recycler mRecycler;
//
//    private boolean mInLayout;
//    private boolean mInScroll;
//    private boolean mInFastRelayout;
//
//    private boolean mInLayoutSearchFocus;
//    private boolean mInSelection = false;
//
//    private OnChildSelectedListener mChildSelectedListener = null;
//    private OnChildLaidOutListener mChildLaidOutListener = null;
//
//    private PendingMoveSmoothScroller mPendingMoveSmoothScroller;
//
//    private int mPrimaryScrollExtra;
//
//    private boolean mForceFullLayout;
//
//    private boolean mLayoutEnabled = true;
//
//    private int mChildVisibility = -1;
//
//    private int mScrollOffsetPrimary;
//    private int mScrollOffsetSecondary;
//    private int mRowSizeSecondaryRequested;
//    private int mFixedRowSizeSecondary;
//    private int[] mRowSizeSecondary;
//    private boolean mRowSecondarySizeRefresh;
//    private int mMaxSizeSecondary;
//    private int mGravity = Gravity.START | Gravity.TOP;
//    private int mFocusScrollStrategy = BaseGridView.FOCUS_SCROLL_ALIGNED;
//    private final WindowAlignment mWindowAlignment = new WindowAlignment();
//    private final ItemAlignment mItemAlignment = new ItemAlignment();
//    private int mSizePrimary;
//    private boolean mFocusOutFront;
//    private boolean mFocusOutEnd;
//    private boolean mFocusSearchDisabled;
//    private boolean mPruneChild = true;
//    private boolean mScrollEnabled = true;
//    private static int[] sTwoInts = new int[2];
//    private boolean mReverseFlowPrimary = false;
//    private boolean mReverseFlowSecondary = false;
//    private int[] mMeasuredDimension = new int[2];
//
//    abstract class GridLinearSmoothScroller extends LinearSmoothScroller {
//
//        GridLinearSmoothScroller() {
//            super(mContext);
//        }
//
//        @Override
//        protected void onStop() {
//            // onTargetFound() may not be called if we hit the "wall" first.
//            View targetView = findViewByPosition(getTargetPosition());
//            if (hasFocus() && targetView != null) {
//                mInSelection = true;
//                targetView.requestFocus();
//                mInSelection = false;
//            }
//            if (needsDispatchChildSelectedOnStop()) {
//                dispatchChildSelected();
//            }
//            super.onStop();
//        }
//
//        boolean needsDispatchChildSelectedOnStop() {
//            return true;
//        }
//
//        @Override
//        protected void onTargetFound(View targetView,
//                                     RecyclerView.State state, Action action) {
//            if (getScrollPosition(targetView, null, sTwoInts)) {
//                int dx, dy;
//                if (mOrientation == HORIZONTAL) {
//                    dx = sTwoInts[0];
//                    dy = sTwoInts[1];
//                } else {
//                    dx = sTwoInts[1];
//                    dy = sTwoInts[0];
//                }
//                final int distance = (int) Math.sqrt(dx * dx + dy * dy);
//                final int time = calculateTimeForDeceleration(distance);
//                action.update(dx, dy, time, mDecelerateInterpolator);
//            }
//        }
//    }
//
//    final class PendingMoveSmoothScroller extends GridLinearSmoothScroller {
//        // -2 is a target position that LinearSmoothScroller can never find until
//        // consumePendingMovesXXX() sets real targetPosition.
//        final static int TARGET_UNDEFINED = -2;
//        // whether the grid is staggered.
//        private final boolean mStaggeredGrid;
//        // Number of pending movements on primary direction, negative if PREV_ITEM.
//        private int mPendingMoves;
//
//        PendingMoveSmoothScroller(int initialPendingMoves, boolean staggeredGrid) {
//            mPendingMoves = initialPendingMoves;
//            mStaggeredGrid = staggeredGrid;
//            setTargetPosition(TARGET_UNDEFINED);
//        }
//
//        void increasePendingMoves() {
//            if (mPendingMoves < MAX_PENDING_MOVES) {
//                mPendingMoves++;
//                if (mPendingMoves == 0) {
//                    dispatchChildSelected();
//                }
//            }
//        }
//
//        void decreasePendingMoves() {
//            if (mPendingMoves > -MAX_PENDING_MOVES) {
//                mPendingMoves--;
//                if (mPendingMoves == 0) {
//                    dispatchChildSelected();
//                }
//            }
//        }
//
//        /**
//         * Called before laid out an item when non-staggered grid can handle pending movements
//         * by skipping "mNumRows" per movement;  staggered grid will have to wait the item
//         * has been laid out in consumePendingMovesAfterLayout().
//         */
//        void consumePendingMovesBeforeLayout() {
//            if (mStaggeredGrid || mPendingMoves == 0) {
//                return;
//            }
//            View newSelected = null;
//            int startPos = mPendingMoves > 0 ? mFocusPosition + mNumRows :
//                    mFocusPosition - mNumRows;
//            for (int pos = startPos; mPendingMoves != 0;
//                 pos = mPendingMoves > 0 ? pos + mNumRows : pos - mNumRows) {
//                View v = findViewByPosition(pos);
//                if (v == null) {
//                    break;
//                }
//                if (!canScrollTo(v)) {
//                    continue;
//                }
//                newSelected = v;
//                mFocusPosition = pos;
//                mSubFocusPosition = 0;
//                if (mPendingMoves > 0) {
//                    mPendingMoves--;
//                } else {
//                    mPendingMoves++;
//                }
//            }
//            if (newSelected != null && hasFocus()) {
//                mInSelection = true;
//                newSelected.requestFocus();
//                mInSelection = false;
//            }
//        }
//
//        /**
//         * Called after laid out an item.  Staggered grid should find view on same
//         * Row and consume pending movements.
//         */
//        void consumePendingMovesAfterLayout() {
//            if (mStaggeredGrid && mPendingMoves != 0) {
//                // consume pending moves, focus to item on the same row.
//                final int focusedRow = mGrid != null && mFocusPosition != NO_POSITION ?
//                        mGrid.getLocation(mFocusPosition).row : NO_POSITION;
//                View newSelected = null;
//                for (int i = 0, count = getChildCount(); i < count && mPendingMoves != 0; i++) {
//                    int index = mPendingMoves > 0 ? i : count - 1 - i;
//                    final View child = getChildAt(index);
//                    if (!canScrollTo(child)) {
//                        continue;
//                    }
//                    int position = getPositionByIndex(index);
//                    Grid.Location loc = mGrid.getLocation(position);
//                    if (focusedRow == NO_POSITION || (loc != null && loc.row == focusedRow)) {
//                        if (mFocusPosition == NO_POSITION) {
//                            mFocusPosition = position;
//                            mSubFocusPosition = 0;
//                            newSelected = child;
//                        } else if ((mPendingMoves > 0 && position > mFocusPosition)
//                                || (mPendingMoves < 0 && position < mFocusPosition)) {
//                            mFocusPosition = position;
//                            mSubFocusPosition = 0;
//                            if (mPendingMoves > 0) {
//                                mPendingMoves--;
//                            } else {
//                                mPendingMoves++;
//                            }
//                            newSelected = child;
//                        }
//                    }
//                }
//                if (newSelected != null && hasFocus()) {
//                    mInSelection = true;
//                    newSelected.requestFocus();
//                    mInSelection = false;
//                }
//                if (mPendingMoves == 0) {
//                    dispatchChildSelected();
//                }
//            }
//            if (mPendingMoves == 0 || (mPendingMoves > 0 && hasCreatedLastItem())
//                    || (mPendingMoves < 0 && hasCreatedFirstItem())) {
//                setTargetPosition(mFocusPosition);
//                stop();
//            }
//        }
//
//        @Override
//        protected void updateActionForInterimTarget(Action action) {
//            if (mPendingMoves == 0) {
//                return;
//            }
//            super.updateActionForInterimTarget(action);
//        }
//
//        @Override
//        public PointF computeScrollVectorForPosition(int targetPosition) {
//            if (mPendingMoves == 0) {
//                return null;
//            }
//            int direction = (mReverseFlowPrimary ? mPendingMoves > 0 : mPendingMoves < 0) ?
//                    -1 : 1;
//            if (mOrientation == HORIZONTAL) {
//                return new PointF(direction, 0);
//            } else {
//                return new PointF(0, direction);
//            }
//        }
//
//        @Override
//        boolean needsDispatchChildSelectedOnStop() {
//            return mPendingMoves != 0;
//        }
//
//        @Override
//        protected void onStop() {
//            super.onStop();
//            // if we hit wall,  need clear the remaining pending moves.
//            mPendingMoves = 0;
//            mPendingMoveSmoothScroller = null;
//            View v = findViewByPosition(getTargetPosition());
//            if (v != null) scrollToView(v, true);
//        }
//    }
//
//    public SpanLayoutManager(Context context, RecyclerView recyclerView, int spanCount) {
//        super(context, spanCount);
//        mContext = context;
//        mBaseGridView = recyclerView;
//        mSpanCount = spanCount;
//        setOrientation(VERTICAL);
//        setReverseLayout(false);
//    }
//
//    public SpanLayoutManager(Context context, RecyclerView recyclerView, int spanCount,
//                             int orientation, boolean reverseLayout) {
//        super(context, spanCount, orientation, reverseLayout);
//        mContext = context;
//        mBaseGridView = recyclerView;
//        mSpanCount = spanCount;
//        setOrientation(orientation);
//    }
//
//    @Override
//    public void setOrientation(int orientation) {
//        if (orientation != HORIZONTAL && orientation != VERTICAL) {
//            return;
//        }
//
//        mOrientation = orientation;
//        mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
//        mWindowAlignment.setOrientation(orientation);
//        mItemAlignment.setOrientation(orientation);
//        mForceFullLayout = true;
//    }
//
//    public void onRtlPropertiesChanged(int layoutDirection) {
//        if (mOrientation == HORIZONTAL) {
//            mReverseFlowPrimary = layoutDirection == View.LAYOUT_DIRECTION_RTL;
//            mReverseFlowSecondary = false;
//        } else {
//            mReverseFlowSecondary = layoutDirection == View.LAYOUT_DIRECTION_RTL;
//            mReverseFlowPrimary = false;
//        }
//        mWindowAlignment.horizontal.setReversedFlow(layoutDirection == View.LAYOUT_DIRECTION_RTL);
//    }
//
//    public int getFocusScrollStrategy() {
//        return mFocusScrollStrategy;
//    }
//
//    public void setFocusScrollStrategy(int focusScrollStrategy) {
//        mFocusScrollStrategy = focusScrollStrategy;
//    }
//
//    public void setWindowAlignment(int windowAlignment) {
//        mWindowAlignment.mainAxis().setWindowAlignment(windowAlignment);
//    }
//
//    public int getWindowAlignment() {
//        return mWindowAlignment.mainAxis().getWindowAlignment();
//    }
//
//    public void setWindowAlignmentOffset(int alignmentOffset) {
//        mWindowAlignment.mainAxis().setWindowAlignmentOffset(alignmentOffset);
//    }
//
//    public int getWindowAlignmentOffset() {
//        return mWindowAlignment.mainAxis().getWindowAlignmentOffset();
//    }
//
//    public void setWindowAlignmentOffsetPercent(float offsetPercent) {
//        mWindowAlignment.mainAxis().setWindowAlignmentOffsetPercent(offsetPercent);
//    }
//
//    public float getWindowAlignmentOffsetPercent() {
//        return mWindowAlignment.mainAxis().getWindowAlignmentOffsetPercent();
//    }
//
//    public void setItemAlignmentOffset(int alignmentOffset) {
//        mItemAlignment.mainAxis().setItemAlignmentOffset(alignmentOffset);
//        updateChildAlignments();
//    }
//
//    public int getItemAlignmentOffset() {
//        return mItemAlignment.mainAxis().getItemAlignmentOffset();
//    }
//
//    public void setItemAlignmentOffsetWithPadding(boolean withPadding) {
//        mItemAlignment.mainAxis().setItemAlignmentOffsetWithPadding(withPadding);
//        updateChildAlignments();
//    }
//
//    public boolean isItemAlignmentOffsetWithPadding() {
//        return mItemAlignment.mainAxis().isItemAlignmentOffsetWithPadding();
//    }
//
//    public void setItemAlignmentOffsetPercent(float offsetPercent) {
//        mItemAlignment.mainAxis().setItemAlignmentOffsetPercent(offsetPercent);
//        updateChildAlignments();
//    }
//
//    public float getItemAlignmentOffsetPercent() {
//        return mItemAlignment.mainAxis().getItemAlignmentOffsetPercent();
//    }
//
//    public void setItemAlignmentViewId(int viewId) {
//        mItemAlignment.mainAxis().setItemAlignmentViewId(viewId);
//        updateChildAlignments();
//    }
//
//    public int getItemAlignmentViewId() {
//        return mItemAlignment.mainAxis().getItemAlignmentViewId();
//    }
//
//    public void setFocusOutAllowed(boolean throughFront, boolean throughEnd) {
//        mFocusOutFront = throughFront;
//        mFocusOutEnd = throughEnd;
//    }
//
//    public void setGravity(int gravity) {
//        mGravity = gravity;
//    }
//
//    public void setOnChildSelectedListener(OnChildSelectedListener listener) {
//        mChildSelectedListener = listener;
//    }
//
//    void setOnChildLaidOutListener(OnChildLaidOutListener listener) {
//        mChildLaidOutListener = listener;
//    }
//
//    @SuppressWarnings("deprecation")
//    private int getPositionByView(View view) {
//        if (view == null) {
//            return NO_POSITION;
//        }
//        LayoutParams params = (LayoutParams) view.getLayoutParams();
//        if (params == null || params.isItemRemoved()) {
//            return NO_POSITION;
//        }
//        return params.getViewPosition();
//    }
//
//    private int getSubPositionByView(View view, View childView) {
//        if (view == null || childView == null) {
//            return 0;
//        }
//        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
//        final ItemAlignmentFacet facet = lp.getItemAlignmentFacet();
//        if (facet != null) {
//            final ItemAlignmentFacet.ItemAlignmentDef[] defs = facet.getAlignmentDefs();
//            if (defs.length > 1) {
//                while (childView != view) {
//                    int id = childView.getId();
//                    if (id != View.NO_ID) {
//                        for (int i = 1; i < defs.length; i++) {
//                            if (defs[i].getItemAlignmentFocusViewId() == id) {
//                                return i;
//                            }
//                        }
//                    }
//                    childView = (View) childView.getParent();
//                }
//            }
//        }
//        return 0;
//    }
//
//    private void dispatchChildSelected() {
//
//        View view = mFocusPosition == NO_POSITION ? null : findViewByPosition(mFocusPosition);
//        if (view != null) {
//            RecyclerView.ViewHolder vh = mBaseGridView.getChildViewHolder(view);
//            if (mChildSelectedListener != null) {
//                mChildSelectedListener.onChildSelected(mBaseGridView, view, mFocusPosition,
//                        vh == null ? NO_ID : vh.getItemId());
//            }
//        } else {
//            if (mChildSelectedListener != null) {
//                mChildSelectedListener.onChildSelected(mBaseGridView, null, NO_POSITION, NO_ID);
//            }
//        }
//
//        if (!mInLayout && !mBaseGridView.isLayoutRequested()) {
//            int childCount = getChildCount();
//            for (int i = 0; i < childCount; i++) {
//                if (getChildAt(i).isLayoutRequested()) {
//                    forceRequestLayout();
//                    break;
//                }
//            }
//        }
//    }
//
//    @Override
//    public boolean canScrollHorizontally() {
//        return mOrientation == HORIZONTAL || mSpanCount > 1;
//    }
//
//    @Override
//    public boolean canScrollVertically() {
//        return mOrientation == VERTICAL || mSpanCount > 1;
//    }
//
//    @Override
//    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
//        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//    }
//
//    @Override
//    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attrs) {
//        return new io.github.clendy.leanback.widget.GridLayoutManager.LayoutParams(context, attrs);
//    }
//
//    @Override
//    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
//        if (lp instanceof LayoutParams) {
//            return new io.github.clendy.leanback.widget.GridLayoutManager.LayoutParams((LayoutParams) lp);
//        } else if (lp instanceof RecyclerView.LayoutParams) {
//            return new io.github.clendy.leanback.widget.GridLayoutManager.LayoutParams((RecyclerView.LayoutParams) lp);
//        } else if (lp instanceof ViewGroup.MarginLayoutParams) {
//            return new io.github.clendy.leanback.widget.GridLayoutManager.LayoutParams((ViewGroup.MarginLayoutParams) lp);
//        } else {
//            return new io.github.clendy.leanback.widget.GridLayoutManager.LayoutParams(lp);
//        }
//    }
//
//    final int getOpticalLeft(View v) {
//        return ((LayoutParams) v.getLayoutParams()).getOpticalLeft(v);
//    }
//
//    final int getOpticalRight(View v) {
//        return ((LayoutParams) v.getLayoutParams()).getOpticalRight(v);
//    }
//
//    final int getOpticalTop(View v) {
//        return ((LayoutParams) v.getLayoutParams()).getOpticalTop(v);
//    }
//
//    final int getOpticalBottom(View v) {
//        return ((LayoutParams) v.getLayoutParams()).getOpticalBottom(v);
//    }
//
//    private int getViewMin(View v) {
////        return (mOrientation == HORIZONTAL) ? getOpticalLeft(v) : getOpticalTop(v);
//        return mOrientationHelper.getDecoratedStart(v);
//    }
//
//    private int getViewMax(View v) {
////        return (mOrientation == HORIZONTAL) ? getOpticalRight(v) : getOpticalBottom(v);
//        return mOrientationHelper.getDecoratedEnd(v);
//    }
//
//    private int getViewPrimarySize(View view) {
//        LayoutParams p = (LayoutParams) view.getLayoutParams();
//        return mOrientation == HORIZONTAL ? p.getOpticalWidth(view) : p.getOpticalHeight(view);
//    }
//
//    private int getViewCenter(View view) {
//        return (mOrientation == HORIZONTAL) ? getViewCenterX(view) : getViewCenterY(view);
//    }
//
//    private int getViewCenterSecondary(View view) {
//        return (mOrientation == HORIZONTAL) ? getViewCenterY(view) : getViewCenterX(view);
//    }
//
//    private int getViewCenterX(View v) {
//        LayoutParams p = (LayoutParams) v.getLayoutParams();
//        return p.getOpticalLeft(v) + p.getAlignX();
//    }
//
//    private int getViewCenterY(View v) {
//        LayoutParams p = (LayoutParams) v.getLayoutParams();
//        return p.getOpticalTop(v) + p.getAlignY();
//    }
//
//    // // TODO: 2017/1/7 007
//
//
//    private void forceRequestLayout() {
//        ViewCompat.postOnAnimation(mBaseGridView, mRequestLayoutRunnable);
//    }
//
//    private final Runnable mRequestLayoutRunnable = new Runnable() {
//        @Override
//        public void run() {
//            requestLayout();
//        }
//    };
//
//    private int getPositionByIndex(int index) {
//        return getPositionByView(getChildAt(index));
//    }
//
//
//    int getSelection() {
//        return mFocusPosition;
//    }
//
//    int getChildDrawingOrder(RecyclerView recyclerView, int childCount, int i) {
//        View view = findViewByPosition(mFocusPosition);
//        if (view == null) {
//            return i;
//        }
//        int focusIndex = recyclerView.indexOfChild(view);
//        // supposely 0 1 2 3 4 5 6 7 8 9, 4 is the center item
//        // drawing order is 0 1 2 3 9 8 7 6 5 4
//        if (i < focusIndex) {
//            return i;
//        } else if (i < childCount - 1) {
//            return focusIndex + childCount - 1 - i;
//        } else {
//            return focusIndex;
//        }
//    }
//
//
//    @Override
//    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
//        if (oldAdapter != null) {
//            mFocusPosition = NO_POSITION;
//            mFocusPositionOffset = 0;
//        }
//        super.onAdapterChanged(oldAdapter, newAdapter);
//    }
//
//    private final static class SavedState implements Parcelable {
//
//        int index;
//        Bundle childStates = Bundle.EMPTY;
//
//        @Override
//        public void writeToParcel(Parcel out, int flags) {
//            out.writeInt(index);
//            out.writeBundle(childStates);
//        }
//
//        @SuppressWarnings("hiding")
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//                    @Override
//                    public SavedState createFromParcel(Parcel in) {
//                        return new SavedState(in);
//                    }
//
//                    @Override
//                    public SavedState[] newArray(int size) {
//                        return new SavedState[size];
//                    }
//                };
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        SavedState(Parcel in) {
//            index = in.readInt();
//            childStates = in.readBundle(io.github.clendy.leanback.widget.GridLayoutManager.class.getClassLoader());
//        }
//
//        SavedState() {
//
//        }
//    }
//
//    @Override
//    public Parcelable onSaveInstanceState() {
//        SavedState ss = new SavedState();
//        ss.index = getSelection();
//        Bundle bundle = mChildrenStates.saveAsBundle();
//        for (int i = 0, count = getChildCount(); i < count; i++) {
//            View view = getChildAt(i);
//            int position = getPositionByView(view);
//            if (position != NO_POSITION) {
//                bundle = mChildrenStates.saveOnScreenView(bundle, view, position);
//            }
//        }
//        ss.childStates = bundle;
//        return ss;
//    }
//
//    void onChildRecycled(RecyclerView.ViewHolder holder) {
//        final int position = holder.getAdapterPosition();
//        if (position != NO_POSITION) {
//            mChildrenStates.saveOffscreenView(holder.itemView, position);
//        }
//    }
//
//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//        if (!(state instanceof SavedState)) {
//            return;
//        }
//        SavedState loadingState = (SavedState) state;
//        mFocusPosition = loadingState.index;
//        mFocusPositionOffset = 0;
//        mChildrenStates.loadFromBundle(loadingState.childStates);
//        mForceFullLayout = true;
//        requestLayout();
//    }
//
//    static class LayoutParams extends RecyclerView.LayoutParams {
//
//        // For placement
//        private int mLeftInset;
//        private int mTopInset;
//        private int mRightInset;
//        private int mBottomInset;
//
//        // For alignment
//        private int mAlignX;
//        private int mAlignY;
//        private int[] mAlignMultiple;
//        private ItemAlignmentFacet mAlignmentFacet;
//
//        public LayoutParams(Context c, AttributeSet attrs) {
//            super(c, attrs);
//        }
//
//        public LayoutParams(int width, int height) {
//            super(width, height);
//        }
//
//        public LayoutParams(ViewGroup.MarginLayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(ViewGroup.LayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(RecyclerView.LayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(LayoutParams source) {
//            super(source);
//        }
//
//        int getAlignX() {
//            return mAlignX;
//        }
//
//        int getAlignY() {
//            return mAlignY;
//        }
//
//        int getOpticalLeft(View view) {
//            return view.getLeft() + mLeftInset;
//        }
//
//        int getOpticalTop(View view) {
//            return view.getTop() + mTopInset;
//        }
//
//        int getOpticalRight(View view) {
//            return view.getRight() - mRightInset;
//        }
//
//        int getOpticalBottom(View view) {
//            return view.getBottom() - mBottomInset;
//        }
//
//        int getOpticalWidth(View view) {
//            return view.getWidth() - mLeftInset - mRightInset;
//        }
//
//        int getOpticalHeight(View view) {
//            return view.getHeight() - mTopInset - mBottomInset;
//        }
//
//        int getOpticalLeftInset() {
//            return mLeftInset;
//        }
//
//        int getOpticalRightInset() {
//            return mRightInset;
//        }
//
//        int getOpticalTopInset() {
//            return mTopInset;
//        }
//
//        int getOpticalBottomInset() {
//            return mBottomInset;
//        }
//
//        void setAlignX(int alignX) {
//            mAlignX = alignX;
//        }
//
//        void setAlignY(int alignY) {
//            mAlignY = alignY;
//        }
//
//        void setItemAlignmentFacet(ItemAlignmentFacet facet) {
//            mAlignmentFacet = facet;
//        }
//
//        ItemAlignmentFacet getItemAlignmentFacet() {
//            return mAlignmentFacet;
//        }
//
//        void calculateItemAlignments(int orientation, View view) {
//            ItemAlignmentFacet.ItemAlignmentDef[] defs = mAlignmentFacet.getAlignmentDefs();
//            if (mAlignMultiple == null || mAlignMultiple.length != defs.length) {
//                mAlignMultiple = new int[defs.length];
//            }
//            for (int i = 0; i < defs.length; i++) {
//                mAlignMultiple[i] = ItemAlignmentFacetHelper
//                        .getAlignmentPosition(view, defs[i], orientation);
//            }
//            if (orientation == HORIZONTAL) {
//                mAlignX = mAlignMultiple[0];
//            } else {
//                mAlignY = mAlignMultiple[0];
//            }
//        }
//
//        int[] getAlignMultiple() {
//            return mAlignMultiple;
//        }
//
//        void setOpticalInsets(int leftInset, int topInset, int rightInset, int bottomInset) {
//            mLeftInset = leftInset;
//            mTopInset = topInset;
//            mRightInset = rightInset;
//            mBottomInset = bottomInset;
//        }
//
//    }
//}
