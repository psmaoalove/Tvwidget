
package com.stv.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

public class GridRecyclerView extends RecyclerView {
    private static final String TAG = GridRecyclerView.class.getSimpleName();
    private View mNextFocused = null;
    private View mFocusedView;
    private int mItemSpaceLarge = 0;
    private int mItemHeightMin = 0;
    private int mItemSpace = 0;
    private GridLayoutManager mLayoutManager;

    public GridRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int getFocusSearchDirection(int keyCode) {
        int direction = View.FOCUS_DOWN;
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            direction = View.FOCUS_DOWN;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            direction = View.FOCUS_UP;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            direction = View.FOCUS_LEFT;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            direction = View.FOCUS_RIGHT;
        }
        return direction;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (event.getRepeatCount() % 2 != 0) {
                        consumed = true;
                    } else {
                        consumed = handleDpadDirectionKeyEvent(event);
                    }
                    break;
            }
        }

        return consumed ? true : super.dispatchKeyEvent(event);
    }

    public boolean handleDpadDirectionKeyEvent(KeyEvent event) {
        boolean consumed = false;
        int keyCode = event.getKeyCode();

        mFocusedView = mLayoutManager.getFocusedChild();
        if (mFocusedView != null) {
            int direction = getFocusSearchDirection(keyCode);
            View srcFocusView = mFocusedView;
            if (mFocusedView.isFocused()) {
                mNextFocused = focusSearch(mFocusedView, direction);
            } else if (mFocusedView.hasFocus()) {
                if (mFocusedView instanceof ViewGroup) {
                    srcFocusView = ((ViewGroup) mFocusedView).findFocus();
                    mNextFocused = focusSearch(srcFocusView, direction);
                }

                if (mNextFocused == null && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    scrollToPosition(0);
                }
            }

            if (mNextFocused != null) {
                if (mNextFocused.isFocusable()) {
                    consumed = doScrollLogic(srcFocusView, mNextFocused, true, keyCode);
                    if (!consumed) {
                        // no need scroll
                        if (mNextFocused != null) {
                            mNextFocused.requestFocus();
                        }
                        consumed = true;
                    }
                }
            }
        }
        return consumed;
    }

    public boolean doScrollLogic(View srcFocusView, View dstFocusView, boolean requestFocus, int keyCode) {
        if (srcFocusView == null || dstFocusView == null) {
            return false;
        }

        int[] focusViewLocation = new int[2];
        srcFocusView.getLocationInWindow(focusViewLocation);
        int[] nextFocusViewLocation = new int[2];
        dstFocusView.getLocationInWindow(nextFocusViewLocation);

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            boolean doScroll = false;
            int scrollY = 0;
            if (srcFocusView == dstFocusView) {
                scrollY = dstFocusView.getHeight() + mItemSpace;
                doScroll = true;
            } else if (canScrollFromBottom(dstFocusView)) {
                scrollY = nextFocusViewLocation[1] + dstFocusView.getHeight() - focusViewLocation[1] - srcFocusView.getHeight();
                doScroll = true;
            }

            if (doScroll) {
                if (scrollY > 0 && mItemHeightMin > scrollY) {
                    scrollY = mItemHeightMin;
                }

                int fromTopOffset = getFromTopOffset(dstFocusView);
                if (fromTopOffset > 0) {
                    int maxScrollY = fromTopOffset - (mItemSpaceLarge + mItemSpace);
                    if (maxScrollY > 0 && scrollY > maxScrollY) {
                        scrollY = maxScrollY;
                    }
                } else {
                    scrollY = 0;
                }

                customSmoothScrollBy(0, scrollY);
                if (requestFocus) {
                    dstFocusView.requestFocus();
                }
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            boolean doScroll = false;
            int scrollY = 0;
            if (srcFocusView == dstFocusView) {
                scrollY = -(dstFocusView.getHeight() + mItemSpace);
                doScroll = true;
            } else if (canScrollFormTop(dstFocusView)) {
                scrollY = nextFocusViewLocation[1] - focusViewLocation[1];
                doScroll = true;
            }

            if (doScroll) {
                if (scrollY < 0 && mItemHeightMin > -scrollY) {
                    scrollY = -mItemHeightMin;
                }

                int fromBottomOffset = getFromBottomOffset(dstFocusView);
                if (fromBottomOffset < 0) {
                    int maxScrollY = -fromBottomOffset - (mItemSpaceLarge + mItemSpace);
                    if (maxScrollY > 0 && -scrollY > maxScrollY) {
                        scrollY = -maxScrollY;
                    }
                } else {
                    scrollY = 0;
                }

                customSmoothScrollBy(0, scrollY);
                if (requestFocus) {
                    dstFocusView.requestFocus();
                }

                return true;
            }
        }

        return false;
    }

    public boolean canScrollFromBottom(View nextFocused) {
        if (nextFocused == null) {
            return false;
        }

        int fromBottomOffset = getFromBottomOffset(nextFocused);
        if (fromBottomOffset > 0) {
            return true;
        }

        return false;
    }

    private int getFromBottomOffset(View nextFocused) {
        if (nextFocused == null) {
            return 0;
        }

        Rect global = new Rect();
        this.getGlobalVisibleRect(global);
        int[] location = new int[2];
        nextFocused.getLocationInWindow(location);
        int bottom = location[1] + nextFocused.getHeight() + mItemSpace;
        int offset = bottom - global.bottom;
        return offset;
    }

    public int getFromTopOffset(View nextFocused) {
        if (nextFocused == null) {
            return 0;
        }

        Rect global = new Rect();
        this.getGlobalVisibleRect(global);
        int[] location = new int[2];
        nextFocused.getLocationInWindow(location);
        int top = location[1] - mItemSpace;

        int offset = top - global.top;

        return offset;
    }

    public void customSmoothScrollBy(int x, int y) {
        super.smoothScrollBy(x, y);
    }

    public boolean canScrollFormTop(View nextFocused) {
        if (nextFocused == null) {
            return false;
        }

        int fromTopOffset = getFromTopOffset(nextFocused);
        if (fromTopOffset < 0) {
            return true;
        }

        return false;
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        mLayoutManager = (GridLayoutManager) layout;
    }
}
