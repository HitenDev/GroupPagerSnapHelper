package me.hiten.grouppagersnaphelper;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;


public class GroupPagerSnapHelper extends SnapHelper {

    static final float MILLISECONDS_PER_INCH = 100f;

    private static final int MAX_SCROLL_ON_FLING_DURATION = 100; // m


    private int mGroupCapacity;

    private int mMainAxisSize;

    private int mCrossAxisSize;


    ScrollEventAdapter mScrollEventAdapter;

    public GroupPagerSnapHelper(int groupCapacity) {
        this(groupCapacity, 1);
    }

    public GroupPagerSnapHelper(int mainAxisSize, int crossAxisSize) {
        this.mGroupCapacity = mainAxisSize * crossAxisSize;
        this.mMainAxisSize = mainAxisSize;
        this.mCrossAxisSize = crossAxisSize;

        if (mGroupCapacity <= 0) {
            throw new IllegalArgumentException("Invalid groupCapacity value");
        }

        if (mMainAxisSize <= 0) {
            throw new IllegalArgumentException("Invalid groupCapacity value");
        }
        if (mCrossAxisSize <= 0) {
            throw new IllegalArgumentException("Invalid groupCapacity value");
        }

    }

    public void addOnPageChangeCallback(OnPageChangeCallback onPageChangeCallback) {
        getScrollEventAdapter();
        this.mScrollEventAdapter.addOnPageChangeCallback(onPageChangeCallback);
    }

    public void removeOnPageChangeCallback(OnPageChangeCallback onPageChangeCallback) {
        if (mScrollEventAdapter == null) {
            return;
        }
        this.mScrollEventAdapter.removeOnPageChangeCallback(onPageChangeCallback);
    }

    public ScrollEventAdapter getScrollEventAdapter() {
        if (mScrollEventAdapter == null) {
            mScrollEventAdapter = new ScrollEventAdapter();
        }
        return mScrollEventAdapter;
    }


    private void updateAndDispatchPageSelected(final RecyclerView.LayoutManager layoutManager) {
        if (mScrollEventAdapter != null) {
            View snapView = findSnapView(layoutManager);
            int position = -1;
            if (snapView != null) {
                position = balanceStartPosition(layoutManager, snapView) / mGroupCapacity;
            }
            if (position != -1) {
                mScrollEventAdapter.dispatchPageSelected(position);
            }

        }
    }

    private void updatePageScrolled(final RecyclerView.LayoutManager layoutManager, int dx, int dy) {
        if (mScrollEventAdapter != null) {
            OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
            if (orientationHelper != null) {
                View startView = findSnapView(layoutManager);
                int childPosition = layoutManager.getPosition(startView);
                int dOffset;
                if (orientationHelper == mVerticalHelper) {
                    dOffset = dy;
                } else {
                    dOffset = dx;
                }
                mScrollEventAdapter.updatePageScrolled(childPosition / mGroupCapacity, dOffset, orientationHelper.getDecoratedMeasurement(startView) * mMainAxisSize);
            }

        }
    }

    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;


    private RecyclerView mRecyclerView;


    private Scroller mGravityScroller;

    @Override
    protected LinearSmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return null;
        }
        return new LinearSmoothScroller(mRecyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx));
            }
        };
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX,
                                      int velocityY) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return RecyclerView.NO_POSITION;
        }

        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }
        View centerView = null;
        if (layoutManager.canScrollVertically()) {
            centerView = findCenterView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            centerView = findCenterView(layoutManager, getHorizontalHelper(layoutManager));
        }
        if (centerView == null) {
            return RecyclerView.NO_POSITION;
        }

        final int currentPosition = layoutManager.getPosition(centerView);
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        final boolean forwardDirection;
        if (layoutManager.canScrollHorizontally()) {
            forwardDirection = velocityX > 0;
        } else {
            forwardDirection = velocityY > 0;
        }
        boolean reverseLayout = false;
        RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
                (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
        PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
        if (vectorForEnd != null) {
            reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0;
        }
        int targetPosition = currentPosition;

        boolean toEnd = forwardDirection && !reverseLayout;
        if (toEnd) {
            targetPosition += (mGroupCapacity / 2 + 1);
        } else {
            targetPosition -= (mGroupCapacity / 2 + 1);
        }
        return getCurrentPagerStartPosition(layoutManager, targetPosition);
    }


    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                boolean mScrolled = false;

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && mScrolled) {
                        mScrolled = false;
                        snapToTargetExistingView();
                        updateAndDispatchPageSelected(mRecyclerView.getLayoutManager());
                        updatePageScrolled(mRecyclerView.getLayoutManager(),0,0);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dx != 0 || dy != 0) {
                        mScrolled = true;
                    } else {
                        updateAndDispatchPageSelected(mRecyclerView.getLayoutManager());
                    }
                    updatePageScrolled(mRecyclerView.getLayoutManager(), dx, dy);
                }
            };

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            setupCallbacks();
            mGravityScroller = new Scroller(mRecyclerView.getContext(),
                    new DecelerateInterpolator());
            snapToTargetExistingView();
        }
    }

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }
        int minFlingVelocity = mRecyclerView.getMinFlingVelocity();
        return (Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX, velocityY);
    }

    private boolean snapFromFling(@NonNull RecyclerView.LayoutManager layoutManager, int velocityX,
                                  int velocityY) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return false;
        }

        RecyclerView.SmoothScroller smoothScroller = createScroller(layoutManager);
        if (smoothScroller == null) {
            return false;
        }

        int targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        smoothScroller.setTargetPosition(targetPosition);
        layoutManager.startSmoothScroll(smoothScroller);
        return true;
    }

    private void snapToTargetExistingView() {
        if (mRecyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View snapView = findSnapView(layoutManager);
        if (snapView == null) {
            return;
        }
        int targetPosition;
        if (layoutManager.getPosition(snapView) % mGroupCapacity != 0) {
            targetPosition = balanceStartPosition(layoutManager, snapView);
            View targetView = layoutManager.findViewByPosition(targetPosition);
            if (targetView != null) {
                snapView = targetView;
            }
        } else {
            targetPosition = layoutManager.getPosition(snapView);
        }

        if (layoutManager.getPosition(snapView) % mGroupCapacity == 0) {
            int[] snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView);
            if (snapDistance[0] != 0 || snapDistance[1] != 0) {
                mRecyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
            }
        } else {
            if (targetPosition == RecyclerView.NO_POSITION) {
                return;
            }
            RecyclerView.SmoothScroller smoothScroller = createScroller(layoutManager);
            if (smoothScroller == null) {
                return;
            }
            smoothScroller.setTargetPosition(targetPosition);
            layoutManager.startSmoothScroll(smoothScroller);
        }
    }

    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        int[] outDist = new int[2];
        mGravityScroller.fling(0, 0, velocityX, velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        outDist[0] = mGravityScroller.getFinalX();
        outDist[1] = mGravityScroller.getFinalY();
        return outDist;
    }


    private void setupCallbacks() throws IllegalStateException {
        if (mRecyclerView.getOnFlingListener() != null) {
            throw new IllegalStateException("An instance of OnFlingListener already set.");
        }
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(this);
    }

    /**
     * Called when the instance of a {@link RecyclerView} is detached.
     */
    private void destroyCallbacks() {
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(null);
    }


    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null || mVerticalHelper.getLayoutManager() != layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null || mHorizontalHelper.getLayoutManager() != layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }


    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findVisibleStartView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findVisibleStartView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private OrientationHelper getOrientationHelper(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return getVerticalHelper(layoutManager);
        } else if (layoutManager.canScrollHorizontally()) {
            return getHorizontalHelper(layoutManager);
        }
        return null;
    }

    private View findCenterView(RecyclerView.LayoutManager layoutManager,
                                OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            /** if child center is closer than previous closest, set it as closest  **/
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }


    @NonNull
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, layoutManager,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, layoutManager,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }


    private int distanceToStart(View targetView, RecyclerView.LayoutManager lm,
                                @NonNull OrientationHelper helper) {
        int distance;
        distance = helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
        return distance;
    }


    private int balanceStartPosition(RecyclerView.LayoutManager layoutManager, View view) {
        int position = layoutManager.getPosition(view);
        int itemCount = layoutManager.getItemCount();
        if (position >= itemCount) {
            return itemCount - 1;
        }
        if (position < 0) {
            return 0;
        }
        int absIndex = position / mCrossAxisSize % mMainAxisSize;
        int midIndex = mMainAxisSize % 2 == 0 ? mMainAxisSize / 2 - 1 : mMainAxisSize / 2;
        if (mMainAxisSize % 2 == 0 || (mMainAxisSize % 2 != 0 && absIndex != midIndex)) {
            if (absIndex > midIndex) {
                return (position / mGroupCapacity + 1) * mGroupCapacity;
            } else {
                return (position / mGroupCapacity) * mGroupCapacity;
            }
        } else {
            OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
            if (orientationHelper != null) {
                int childCenter = orientationHelper.getDecoratedStart(view) + orientationHelper.getDecoratedMeasurement(view) / 2;
                int startAfterPadding = orientationHelper.getStartAfterPadding();
                if (childCenter < startAfterPadding) {
                    return (position / mGroupCapacity + 1) * mGroupCapacity;
                } else {
                    return (position / mGroupCapacity) * mGroupCapacity;
                }
            }
        }
        return (position / mGroupCapacity) * mGroupCapacity;
    }

    private int getCurrentPagerStartPosition(RecyclerView.LayoutManager layoutManager, int targetPosition) {
        if (targetPosition >= layoutManager.getItemCount() - 1) {
            return (layoutManager.getItemCount() - 1) / mMainAxisSize * mMainAxisSize;
        }
        if (targetPosition < 0) {
            return 0;
        }
        return (targetPosition / mGroupCapacity) * mGroupCapacity;
    }


    private View findVisibleStartView(RecyclerView.LayoutManager layoutManager,
                                      OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        int startest = Integer.MAX_VALUE;
        int startLimit = helper.getStartAfterPadding();
        int endLimit = helper.getEndAfterPadding();
        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childStart = helper.getDecoratedStart(child);
            int childEnd = helper.getDecoratedEnd(child);
            if (childEnd <= startLimit) {
                continue;
            }
            if (childStart >= endLimit) {
                continue;
            }
            if (childStart < startest) {
                startest = childStart;
                closestChild = child;
            }
        }
        return closestChild;
    }


}
