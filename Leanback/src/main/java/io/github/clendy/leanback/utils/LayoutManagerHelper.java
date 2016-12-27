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

package io.github.clendy.leanback.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Clendy
 */
public final class LayoutManagerHelper {

    private RecyclerView mRecyclerView;

    private RecyclerView.LayoutManager mLayoutManager;

    private LayoutManagerHelper(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        if (recyclerView != null) {
            this.mLayoutManager = recyclerView.getLayoutManager();
        }
    }

    public static LayoutManagerHelper newInstance(RecyclerView recyclerView) {
        return new LayoutManagerHelper(recyclerView);
    }

    public boolean isFocusOnLeftmostColumn() {
        if (mRecyclerView == null || mLayoutManager == null) {
            return false;
        }
        View child = mRecyclerView.getFocusedChild();
        int position = mRecyclerView.getChildLayoutPosition(child);
        if (mLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();
            if (orientation == GridLayoutManager.HORIZONTAL) {
                if (position < layoutManager.getSpanCount()) {
                    return true;
                }
            } else if (orientation == GridLayoutManager.VERTICAL) {
                if (position % layoutManager.getSpanCount() == 0) {
                    return true;
                }
            }
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();
            if (orientation == LinearLayoutManager.HORIZONTAL) {
                if (position == 0) {
                    return true;
                }
            } else if (orientation == LinearLayoutManager.VERTICAL) {
                return true;
            }
        }
        return false;
    }

    public boolean isFocusOnRightmostColumn() {
        if (mRecyclerView == null || mLayoutManager == null) {
            return false;
        }
        View child = mRecyclerView.getFocusedChild();
        int position = mRecyclerView.getChildLayoutPosition(child);
        if (mLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();
            if (orientation == GridLayoutManager.HORIZONTAL) {
                int rowCount = layoutManager.getItemCount() / layoutManager.getSpanCount();
                int rowNum = position / layoutManager.getSpanCount();
                if (rowNum == rowCount - 1) {
                    return true;
                }
            } else if (orientation == GridLayoutManager.VERTICAL) {
                if (position % layoutManager.getSpanCount() == layoutManager.getSpanCount() - 1) {
                    return true;
                } else if (position == layoutManager.getItemCount() - 1) {
                    return true;
                }
            }
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();
            if (orientation == LinearLayoutManager.HORIZONTAL) {
                if (position == layoutManager.getItemCount() - 1) {
                    return true;
                }
            } else if (orientation == LinearLayoutManager.VERTICAL) {
                return true;
            }
        }
        return false;
    }

    public boolean isFocusOnTopmostRow() {
        if (mRecyclerView == null || mLayoutManager == null) {
            return false;
        }
        View child = mRecyclerView.getFocusedChild();
        int position = mRecyclerView.getChildLayoutPosition(child);
        if (mLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();

            if (orientation == GridLayoutManager.HORIZONTAL) {
                if (position % layoutManager.getItemCount() == 0) {
                    return true;
                }

            } else if (orientation == GridLayoutManager.VERTICAL) {
                if (position < layoutManager.getSpanCount()) {
                    return true;
                }
            }

        } else if (mLayoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();

            if (orientation == LinearLayoutManager.HORIZONTAL) {
                return true;

            } else if (orientation == LinearLayoutManager.VERTICAL) {
                if (position == 0) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isFocusOnBottommostRow() {
        if (mRecyclerView == null || mLayoutManager == null) {
            return false;
        }
        View child = mRecyclerView.getFocusedChild();
        int position = mRecyclerView.getChildLayoutPosition(child);
        if (mLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();

            if (orientation == GridLayoutManager.HORIZONTAL) {
                if (position % layoutManager.getSpanCount() == layoutManager.getSpanCount() - 1) {
                    return true;
                } else if (position == layoutManager.getItemCount() - 1) {
                    return true;
                }

            } else if (orientation == GridLayoutManager.VERTICAL) {
                int rowCount = layoutManager.getItemCount() / layoutManager.getSpanCount();
                int rowNum = position / layoutManager.getSpanCount();
                if (rowNum == rowCount - 1) {
                    return true;
                }
            }

        } else if (mLayoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
            int orientation = layoutManager.getOrientation();

            if (orientation == LinearLayoutManager.HORIZONTAL) {
                return true;

            } else if (orientation == LinearLayoutManager.VERTICAL) {
                if (position == layoutManager.getItemCount() - 1) {
                    return true;
                }
            }

        }
        return false;
    }

}
