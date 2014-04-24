package com.example.myapplication2.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;


/**
 *
 * <p>To use a ImageCarouselView you must call its setCarouselAdapter function.
 * this adapter tells the carousel what views to show. The carousel itself is
 * pragmatically inserted as the first view in the ImageCarouselView. Any subviews
 * added to this layout in the XML or pragmatically will not scroll but will be
 * touch sensitive to for the carousel.
 * </p>
 *
 * @attr ref R.styleable#ImageCarouselView_carouselHeight
 * @attr ref R..styleable#ImageCarouselView_animationRate
 */
public class ImageCarouselView extends RelativeLayout implements Runnable {

    private int carouselHeight; //Set by the XML Attribute carouselHeight

    //UI Configuration
    private static final int MIN_FLING_VELOCITY = 400; // dips
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    public int itemWidth;     //Width of a carousel item. Is set to the width of this view.

    private int mCurItem=0;   // Index of currently displayed page

    /**
     * Useful for the on touch event and for estimating velocity for fake physics
     */
    private VelocityTracker velocityTracker;
    private int minimumVelocity;
    private int maximumVelocity;
    private int flingDistance;
    private float lastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;


    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    private boolean isBeingDragged=false; //True while the view is being dragged

    //ViewGroup which is inserted at as the container for the side scrolling images
    private Carousel carousel;

    //For handling auto flip through the frames
    private int animationRate;


    /**
     * @return True if the view pager is currently being dragged by the user
     */
    public boolean isDragging() {
        return isBeingDragged;
    }

    /**
     * Causes the pager to switch to the next page to the right (left scroll)
     */
    public void nextPage() {
        float curPageFlt = (float)carousel.getScrollX()/(float)itemWidth;
        int currentPage = (int)curPageFlt;
        currentPage++;
        carousel.doScroll(itemWidth*currentPage);
    }

    /**
     * Optional sets a page change listener. Useful to update content after each page change
     * @param pageChangeListener
     */
    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }
    public interface OnPageChangeListener{
        public void onNewPage(int newPage);
    }
    private OnPageChangeListener pageChangeListener;

    /**
     * Required sets an adapter to create the views and set their value.
     * @param carouselAdapter
     */
    public void setCarouselAdapter(CarouselAdapter carouselAdapter) {
        this.carouselAdapter = carouselAdapter;
        carousel.init();
        if (animationRate>0) postDelayed(this,animationRate);
    }
    public interface CarouselAdapter {
        View generateView();
        void setViewContent(View view, int page);
        int getCount();
    }
    private CarouselAdapter carouselAdapter;

    //Runnable for the handler
    @Override
    public void run() {
        if (isDragging()==false){
            nextPage();
            if (animationRate>0) postDelayed(this,animationRate);
        }
    }

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

    //Items in the scroller will always be fill width, we need to know what that is and onLayout is a good time to find out
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        itemWidth = getWidth();
    }

    //In the xml we specify the carousel height
    private void parseAttributes(AttributeSet attrs) {

        try {
            int[] attrsArray = new int[]{R.attr.carouselHeight,R.attr.animationRate};
            TypedArray a = getContext().obtainStyledAttributes(attrs, attrsArray);
            try {
                carouselHeight = a.getDimensionPixelSize(0, -1);
                if (carouselHeight == -1) {
                    Log.e("ImageCarouselView", "You need to specify a carouselHeight defaulting to 300");
                    carouselHeight = 300;
                }
                animationRate = a.getInt(1,0);
            } finally {
                a.recycle();
            }
        } catch (NullPointerException e){
            Log.e("ImageCarouselView", "Error decoding carouselHeight defaulting to 300");
            carouselHeight = 300;
            e.printStackTrace();
        }
    }

    boolean carouselExists = false;

    //Set up the view, we cant do it in the constructor, we didn't have enough info yet.
    private void init(Context context){

        //Set up fake physics parameters
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;
        minimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        flingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        if (carouselExists==false){
            //Add the container for our images
            carousel = new Carousel(getContext());
            carousel.setId(android.R.id.custom);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,carouselHeight);
            carousel.setLayoutParams(lp);
            addView(carousel, 0);
            carouselExists=true;

        }

    }

    //Override this so the parent scroll view does not cancel a page flip
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean retVal = super.dispatchTouchEvent(ev);
        getParent().requestDisallowInterceptTouchEvent(isBeingDragged);
        return retVal;
    }


    /**
     * My logic here is mainly lifted from the support lib's view pager
     * @see android.support.v4.view.ViewPager#onTouchEvent(MotionEvent ev)
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false;
        }

        //The velocity tracker helps us determine if its a fling or not
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        final int action = ev.getAction();
        boolean needsInvalidate = false;

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                carousel.scroller.abortAnimation();

                // Remember where the motion event started
                lastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                removeCallbacks(this); //Cancel the animation timer
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!isBeingDragged) {
                    if (mActivePointerId==INVALID_POINTER){
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    }
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - lastMotionX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);

                    if (xDiff > yDiff) {
                        isBeingDragged = true;
                        removeCallbacks(this); //Cancel the animation timer
                        lastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX  : mInitialMotionX ;
                        mLastMotionY = y;
                    }
                }
                // Not else! Note that isBeingDragged can be set above.
                if (isBeingDragged) {
                    // Scroll to follow the motion event
                    int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                    float x;
                    try {
                        x = MotionEventCompat.getX(ev, activePointerIndex);
                    } catch (IllegalArgumentException e){
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                        activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                        x = MotionEventCompat.getX(ev, activePointerIndex);
                    }
                    performDrag(x);
                    needsInvalidate=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isBeingDragged) {
                    final VelocityTracker velocityTracker = this.velocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                    int nextPage = determineTargetPage(ev);
                    carousel.doScroll((int) (itemWidth * nextPage));
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else {
                    carouselOnClickListener(ev.getY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isBeingDragged) {
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

    private OnClickListener onClickListener;
    private void carouselOnClickListener(float y) {
        if (y<carouselHeight && onClickListener!=null){
            onClickListener.onClick(carousel);
        }
    }
    public void setOnCarouselClickedListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }


    /**
     * Determines target page derived from ViewPager but handles going in negative direction
     * @see  android.support.v4.view.ViewPager#determineTargetPage(int,float,int,int)
     * @param ev MotionEvent from the onTouch handler
     * @return the target page accounting for velocity and scroll position
     */
    private int determineTargetPage(MotionEvent ev) {
        int targetPage;

        int velocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
        float curPageFlt = (float)carousel.getScrollX()/(float)itemWidth;
        final int currentPage = (int)curPageFlt;
        final float pageOffset = (curPageFlt-currentPage);
        final float x = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
        final int deltaX = (int) (x - mInitialMotionX);

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
        isBeingDragged = false;
        if (animationRate>0) postDelayed(this,animationRate);
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void performDrag(float x) {

        final float deltaX = lastMotionX - x;
        lastMotionX = x;

        float oldScrollX = carousel.getScrollX();
        float scrollX = oldScrollX + deltaX;

        lastMotionX += scrollX - (int) scrollX;
        carousel.scrollTo((int) scrollX, getScrollY());

    }

    /**
     * Created by mkluver on 4/8/14.
     */
    public class Carousel extends FrameLayout {
        public Scroller scroller;


        private int oldItem=0;

        //These views get cycled through each other
        private View left;
        private View middle;
        private View right;

        //Initialize to values that are not real so we don't start on the current page
        private int currentLeftPage=-100;
        private int currentMiddlePage=-100;
        private int currentRightPage=-100;

        //We have 3 views that display in a window windowStart and windowEnd describe where the 3 images are
        int windowStart;
        int windowEnd;

        public Carousel(Context context) {
            super(context);
            scroller = new Scroller(getContext());
            windowStart = 0;
            windowEnd = itemWidth *3;
        }

        public void init(){
            left = carouselAdapter.generateView();
            left.setId(android.R.id.button1);
            middle = carouselAdapter.generateView();
            middle.setId(android.R.id.button2);
            right = carouselAdapter.generateView();
            right.setId(android.R.id.button3);
            addView(left);
            addView(middle);
            addView(right);
            windowStart = 0;
            windowEnd = itemWidth *3;
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (carouselAdapter ==null || carouselAdapter.getCount()==0){
                return;
            }

            final long drawingTime = getDrawingTime();

            //Each time we need to draw our children we recalculate which child goes where
            //and what gets displayed in it
            int width = itemWidth;

            //Determine the current visible page based on the scroll position
            int x;
            if (isInEditMode()){
                x = 0;
            } else {
                x = scroller.getCurrX();
            }
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
                View tmp = left;
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
                View tmp = right;
                right = middle;
                middle = left;
                left = tmp;

                itemDiff--;
                pageChanged=true;


            }

            //Save the page location for next time
            oldItem = currentItem;


            //Determine what image resource goes in the left view
            int count;
            if (isInEditMode()){
                count = 1;
            } else {
                count = carouselAdapter.getCount();
            }
            int imageItem = (currentItem-1) % count;

            //I don't see why I need this the currentItem % count should not be negative
            if (imageItem<0)imageItem+=count;

            //Use our window position to determine
            int l = windowStart-width;
            int t = 0;
            int r = windowStart;
            int b = carouselHeight;

            //Check if we need to update the left image
            if (currentLeftPage != imageItem)
            {
                currentLeftPage = imageItem;
                carouselAdapter.setViewContent(left, currentLeftPage);
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
            if (currentMiddlePage != imageItem)
            {
                currentMiddlePage = imageItem;
                carouselAdapter.setViewContent(middle,currentMiddlePage);

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
            if (currentRightPage != imageItem)
            {
                currentRightPage = imageItem;
                carouselAdapter.setViewContent(right,currentRightPage);
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
            if (isInEditMode()) return;
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
