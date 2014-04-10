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

import org.jetbrains.annotations.NotNull;

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
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mFlingDistance;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;
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

    @Override
    public void onMeasure(int w,int h){
        super.onMeasure(w,h);

    }

    private void init(Context context){

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        carousel = new Carousel(getContext());

        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,carouselHeight);
        carousel.setLayoutParams(lp);
        addView(carousel,0);
        carousel.setImageResources(new int[]{R.drawable.img1,R.drawable.img2,R.drawable.img3,R.drawable.img4,R.drawable.img5});

    }


    @Override
    public boolean onTouchEvent(@NotNull MotionEvent ev) {

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
                mLastMotionX = mInitialMotionX = ev.getX();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);
                    //if (DEBUG) Log.v(TAG, "Moved x to " + x + "," + y + " diff=" + xDiff + "," + yDiff);
                    if (xDiff > yDiff) {
                        //if (DEBUG) Log.v(TAG, "Starting drag!");
                        mIsBeingDragged = true;
                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX  : mInitialMotionX ;
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

                    final int scrollX = carousel.getScrollX();

                    float curPageFlt = (float)scrollX/(float)itemWidth;
                    final int currentPage = (int)curPageFlt;
                    final float pageOffset = (curPageFlt-currentPage);

                    final int activePointerIndex =  MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
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
                mLastMotionX = x;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                mLastMotionX = MotionEventCompat.getX(ev,
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



        if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {

            if (pageOffset<0) {
                targetPage = velocity > 0 ? currentPage-1 : currentPage;
            }else {
                targetPage = velocity > 0 ? currentPage : currentPage + 1;
            }


        } else {

            float truncator = currentPage >= mCurItem ? 0.4f : 0.6f;

            if (pageOffset<0) truncator=-truncator;

            targetPage = (int) (currentPage + pageOffset + truncator);

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

        final float deltaX = mLastMotionX - x;
        mLastMotionX = x;

        float oldScrollX = carousel.getScrollX();
        float scrollX = oldScrollX + deltaX;
        final int width = getWidth();//getClientWidth();

        boolean leftAbsolute = true;
        boolean rightAbsolute = true;

        mLastMotionX += scrollX - (int) scrollX;
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
        public Carousel(Context context) {
            super(context);

            scroller = new Scroller(getContext());

            left = new ImageView(getContext());
            middle = new ImageView(getContext());
            right = new ImageView(getContext());

            leftOffset   = -itemWidth;
            middleOffset = 0;
            rightOffset  = itemWidth;
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

        }



        @Override
        protected void dispatchDraw(Canvas canvas) {


            boolean more = false;
            final long drawingTime = getDrawingTime();

            int width = itemWidth;

            int x = scroller.getCurrX();
            int currentItem = x/width;


            int itemDiff = oldItem-currentItem;

            while (itemDiff < 0) {

                Log.i("DRAG", "Moving Left :: Dispatch Draw currentItem=" + currentItem);

                //This means we have moved one frame to the left (showing a new frame to the right)
                windowStart += width;
                windowEnd += width;



                ImageView tmp = left;
                left = middle;
                middle = right;
                right = tmp;

                itemDiff++;


            }
            //if (oldItem > currentItem){
            while (itemDiff > 0) {

                Log.i("DRAG","Moving Right ::Dispatch Draw currentItem="+currentItem);

                //We moved 1 frame to the right
                windowStart -= width;
                windowEnd -= width;



                ImageView tmp = right;
                right = middle;
                middle = left;
                left = tmp;

                itemDiff--;
            }

            oldItem = currentItem;


            int count = imageResources.size();

            int imageItem = currentItem % count;
            if (imageItem<0)imageItem+=count;

            Log.i("CAR","imageItem="+imageItem+"    currentItem="+currentItem+"    count="+count);

            int l = windowStart-width;
            int t = 0;
            int r = windowStart;
            int b = 400;
            Log.i("DRAG","WindowStart ="+l);
            left.setImageResource(imageResources.get(imageItem));
            left.layout(l,t,r,b);


            drawChild(canvas, left, drawingTime);
            l+=width;
            r+=width;
            imageItem = (currentItem+1) % count;
            if (imageItem<0)imageItem+=count;

            middle.setImageResource(imageResources.get(imageItem));
            middle.layout(l,t,r,b);
            drawChild(canvas, middle, drawingTime);
            l+=width;
            r+=width;
            right.layout(l, t, r, b);
            imageItem = (currentItem+2) % count;
            if (imageItem<0)imageItem+=count;

            right.setImageResource(imageResources.get(imageItem));
            drawChild(canvas,right,drawingTime);


        }




        ImageView left;
        ImageView middle;
        ImageView right;

        int leftOffset = 0;
        int middleOffset = 0;
        int rightOffset = 0;

        int windowStart = 0;
        int windowEnd = itemWidth *3;



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
