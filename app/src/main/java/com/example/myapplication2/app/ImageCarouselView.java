package com.example.myapplication2.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Used to create an image carousel with an area that does not scroll that also has touch handlers
 * to handle scrolling
 */
public class ImageCarouselView extends LinearLayout {

    //UI Configuration
    private static final int MIN_FLING_VELOCITY = 400; // dips
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips


    public int itemWidth;     //Width of a carousel item. Is set to the width of this view.

    private int mCurItem=0;   // Index of currently displayed page

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    private int minimumVelocity;
    private int mMaximumVelocity;
    private int flingDistance;

    /**
     * Position of the last motion event.
     */
    private float lastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;


    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    private boolean mIsBeingDragged; //True while the view is being dragged

    private int carouselHeight;
    private Carousel carousel;

    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }
    interface OnPageChangeListener{
        public void onNewPage(int newPage);
    }
    private OnPageChangeListener pageChangeListener;


    @SuppressLint("NewApi")
    public ImageCarouselView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(attrs);
        init(context);
    }
    public ImageCarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
        init(context);
    }
    public ImageCarouselView(Context context, int carouselHeight) {
        super(context);
        this.carouselHeight = carouselHeight;
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        itemWidth = getWidth();
    }

    private void parseAttributes(AttributeSet attrs) {

        try {
            int[] attrsArray = new int[]{R.attr.carouselHeight};
            TypedArray a = getContext().obtainStyledAttributes(attrs, attrsArray);
            try {
                carouselHeight = a.getDimensionPixelSize(0, -1);
                if (carouselHeight == -1) {
                    Log.e("ImageCarouselView", "You need to specify a carouselHeight defaulting to 300");
                    carouselHeight = 300;
                }
            } finally {
                a.recycle();
            }
        } catch (NullPointerException e){
            Log.e("ImageCarouselView", "Error decoding carouselHeight defaulting to 300");
            carouselHeight = 300;
            e.printStackTrace();
        }
    }

    private void init(Context context){

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;
        minimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        flingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        carousel = new Carousel(getContext());

        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,carouselHeight);
        carousel.setLayoutParams(lp);
        addView(carousel,0);
        carousel.setImageResources(new int[]{R.drawable.img1,R.drawable.img2,R.drawable.img3,R.drawable.img4,R.drawable.img5});

    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false;
        }

        //The velocity tracker helps us determine if its a fling or not
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        boolean needsInvalidate = false;

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                carousel.scroller.abortAnimation();

                // Remember where the motion event started
                lastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - lastMotionX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);

                    if (xDiff > yDiff) {
                        mIsBeingDragged = true;
                        lastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX  : mInitialMotionX ;
                        mLastMotionY = y;
                    }
                }
                // Not else! Note that mIsBeingDragged can be set above.
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    performDrag(x);
                    needsInvalidate=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
                    float curPageFlt = (float)carousel.getScrollX()/(float)itemWidth;
                    final int currentPage = (int)curPageFlt;
                    final float pageOffset = (curPageFlt-currentPage);
                    final float x = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity, totalDelta);
                    carousel.doScroll((int) (itemWidth * nextPage));
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    carousel.doScroll((int) (itemWidth * mCurItem));
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, index);
                lastMotionX = x;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                lastMotionX = MotionEventCompat.getX(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(carousel);
        }
        return true;
    }

    private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaX) {
        int targetPage;

        //First check if there was enough velocity and distance that we need to fake some physics
        //and finish the scroll
        if (Math.abs(deltaX) > flingDistance && Math.abs(velocity) > minimumVelocity) {

            //If the pageOffset<0 that means that we are scrolling in to the negative direction.
            //that is to the left of the initial position.
            if (pageOffset<0) {
                targetPage = velocity > 0 ? currentPage-1 : currentPage;
            }else {
                targetPage = velocity > 0 ? currentPage : currentPage + 1;
            }

        } else {

            //60/40 ratio for determining if we went to the next page
            float truncation = currentPage >= mCurItem ? 0.4f : 0.6f;

            //If we are scrolling in to negative directions the truncation is backward
            if (pageOffset<0) truncation=-truncation;

            //These will get added up to a fraction that is truncated (i.e 2.8 = 2)
            targetPage = (int) (currentPage + pageOffset + truncation);

        }

        return targetPage;
    }

    private void endDrag() {
        mIsBeingDragged = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean performDrag(float x) {
        boolean needsInvalidate = false;

        final float deltaX = lastMotionX - x;
        lastMotionX = x;

        float oldScrollX = carousel.getScrollX();
        float scrollX = oldScrollX + deltaX;

        lastMotionX += scrollX - (int) scrollX;
        carousel.scrollTo((int) scrollX, getScrollY());

        return true;
    }


    /**
     * Created by mkluver on 4/8/14.
     */
    public class Carousel extends ViewGroup {
        public Scroller scroller;
        private ArrayList<Integer> imageResources;

        private int oldItem=0;

        //These views get cycled through each other
        private ImageView left;
        private ImageView middle;
        private ImageView right;

        private int currentLeftResource;
        private int currentMiddleResource;
        private int currentRightResource;


        //We have 3 views that display in a window windowStart and windowEnd describe where the 3 images are
        int windowStart;
        int windowEnd;


        public Carousel(Context context) {
            super(context);

            scroller = new Scroller(getContext());

            left = new ImageView(getContext());
            middle = new ImageView(getContext());
            right = new ImageView(getContext());

            left.setScaleType(ImageView.ScaleType.FIT_XY);
            middle.setScaleType(ImageView.ScaleType.FIT_XY);
            right.setScaleType(ImageView.ScaleType.FIT_XY);

            windowStart = 0;
            windowEnd = itemWidth *3;
        }


        public void setImageResources(ArrayList<Integer> imageResources){
            this.imageResources = imageResources;
        }

        public void setImageResources(int[] imageResources){
            this.imageResources = new ArrayList<Integer>(imageResources.length);
            for (int imageResource : imageResources){
                this.imageResources.add(imageResource);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            //this one is abstract in ViewGroup but in this case we don't need it because
            //there are always the same 3 views
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            final long drawingTime = getDrawingTime();

            //Each time we need to draw our children we recalculate which child goes where
            //and what gets displayed in it
            int width = itemWidth;

            //Determine the current visible page based on the scroll position
            int x = scroller.getCurrX();
            int currentItem = x/width;

            //Determine how far ahead or behind the current page is. In most cases it will be -1 or 1
            //but it can be a bigger number if the user somehow scrolled a long way
            int itemDiff = oldItem-currentItem;
            boolean pageChanged = false;
            //This means we have moved one frame to the left (showing a new frame to the right)
            while (itemDiff < 0) {

                //Move our visible window one page over
                windowStart += width;
                windowEnd += width;

                //Rotate the views such that the left view goes on the right side
                ImageView tmp = left;
                left = middle;
                middle = right;
                right = tmp;

                itemDiff++;
                pageChanged=true;
            }

            //This means we have moved one frame to the right (showing a new frame to the left)
            while (itemDiff > 0) {

                //Move our visible window one page over
                windowStart -= width;
                windowEnd -= width;

                //Rotate the views such that the right view goes on the left side
                ImageView tmp = right;
                right = middle;
                middle = left;
                left = tmp;

                itemDiff--;
                pageChanged=true;
            }

            //Save the page location for next time
            oldItem = currentItem;


            //Determine what image resource goes in the left view
            int count = imageResources.size();
            int imageItem = (currentItem-1) % count;

            //I don't see why I need this the currentItem % count should not be negative
            if (imageItem<0)imageItem+=count;

            //Use our window position to determine
            int l = windowStart-width;
            int t = 0;
            int r = windowStart;
            int b = carouselHeight;

            //Check if we need to update the left image
            int resource = imageResources.get(imageItem);
            if (currentLeftResource != resource)
            {
                currentLeftResource = resource;
                left.setImageResource(currentLeftResource);
            }
            //Draw the view off screen to the left
            left.layout(l,t,r,b);
            drawChild(canvas, left, drawingTime);

            //Move to the middle part
            l+=width;
            r+=width;

            //Determine what image resource goes in the center view
            imageItem = (currentItem) % count;
            if (imageItem<0)imageItem+=count;

            //Check if we need to update the center image
            resource = imageResources.get(imageItem);
            if (currentMiddleResource != resource)
            {
                currentMiddleResource = resource;
                middle.setImageResource(currentMiddleResource);

                //If we update the middle view then we should let the listener know
                if (pageChanged && pageChangeListener!=null){
                    pageChangeListener.onNewPage(imageItem);
                }
            }
            //Draw the view centered
            middle.layout(l,t,r,b);
            drawChild(canvas, middle, drawingTime);

            //Move to the right part
            l+=width;
            r+=width;

            //Determine what image resource goes in the right view
            imageItem = (currentItem+1) % count;
            if (imageItem<0)imageItem+=count;

            //Check if we need to update the right image
            resource = imageResources.get(imageItem);
            if (currentRightResource != resource)
            {
                currentRightResource = resource;
                right.setImageResource(currentRightResource);
            }

            //Draw the view off to the right
            right.layout(l, t, r, b);
            drawChild(canvas,right,drawingTime);
        }


        public void doScroll(int x){
            scroller.forceFinished(true);
            scroller.abortAnimation();
            scroller.startScroll(getScrollX(), 0, x - getScrollX(), 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }


        @Override
        public void computeScroll() {
            if (!scroller.isFinished() && scroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = scroller.getCurrX();
                int y = scroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                }

                // Keep on drawing until the animation has finished.
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }
    }
}
