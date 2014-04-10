package com.example.myapplication2.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.lang.reflect.Field;

/**
 * Created by mkluver on 3/21/14.
 */
public class CustomEditText extends EditText {
    private BitmapDrawable drawable;
    private NinePatchDrawable npd;
    private Bitmap bmp;

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomEditText(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
       // drawable = getResources().getDrawable(R.drawable.redeem_code_edit_text);
       // drawable.setBounds(0,0,50,50);

        bmp = BitmapFactory.decodeResource(getResources(),R.drawable.redeem_code_edit_text);

     //   npd = (NinePatchDrawable) getResources().getDrawable(R.drawable.redeem_code_edit_text);

// Set its bound where you need
       // Rect npdBounds = new Rect(0,0,30,30);
       // npd.setBounds(npdBounds);


/*
        addTextChangedListener(new TextWatcher() {
            boolean isDelete = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("MARC","sdfdsdss");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("MARC","sdfdsdss");
                if (before > count){
                    isDelete = true;
                } else {
                    isDelete = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isDelete) {
                    if ((s.length() & 1) == 1){
                        s.delete(s.length()-1,s.length());
                    }
                } else {
                    if ((s.length() & 1) == 1) {
                        s.append(" ");
                    }
                }
                setSpan(s);
            }
        });*/
    }

    private void __setSpan(Editable s){

        String str = s.toString();
        int i = 0;



        for (char c : str.toCharArray()) {

            if ((i & 1) == 1) {
                Drawable d = new ShapeDrawable();
                d.setBounds(new Rect(0,0,100,0));
                d.setColorFilter(Color.BLUE, PorterDuff.Mode.CLEAR);
                ImageSpan is = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                s.setSpan(is, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            i++;
        }


    }


    private void setSpan(Editable s) {

    }

    private void ____setSpan(Editable s){

        String str = s.toString();
        int i = 0;



        for (char c : str.toCharArray()) {

            if ((i & 1) == 1) {
                Drawable d = new ShapeDrawable();
                d.setBounds(new Rect(0,0,100,0));
                d.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                ImageSpan is = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);



                s.setSpan(is, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            i++;
        }


    }


    private void _setSpan(Editable s){
        String str = s.toString();
        int i = 0;
        for (char c : str.toCharArray()) {

            if ((i & 1) == 1) {
                s.setSpan(new MetricAffectingSpan() {
                    @Override
                    public void updateMeasureState(TextPaint paint) {
                        paint.setTextScaleX(20);
                        Log.i("SDFSDAF", "asdfsd");
                    }

                    @Override
                    public void updateDrawState(TextPaint paint) {
                        paint.setTextScaleX(20);

                        Log.i("SDFSDAF", "asdfsd");
                    }
                }, i, i + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            i++;
        }
    }

  //  @Override
    public Editable getTextValue(){
        Editable e = super.getText();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int i = 0; i < e.length(); i++){
            if ((i & 1) == 0){
                spannableStringBuilder.append(e.toString().charAt(i));
            }
        }
        return spannableStringBuilder;
    }

    public void setTextValue(String text){
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int i = 0; i < text.length(); i++){
            if ((i & 1) == 0){
                spannableStringBuilder.append(text.charAt(i));
                spannableStringBuilder.append(" ");
            }
        }
        setSpan(spannableStringBuilder);
    }

    long mShowCursor = 0;
    @Override
    public void onDraw(Canvas canvas){

        Editable originalEditable = null;

        try {
            Field f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mText");
            f.setAccessible(true);
            Editable mText = (Editable)f.get(this);

            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mLayout");
            f.setAccessible(true);
            Layout mLayout = (Layout)f.get(this);

            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mTextPaint");
            f.setAccessible(true);
            TextPaint mTextPaint = (TextPaint)f.get(this);

            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mCurTextColor");
            f.setAccessible(true);
            int mCurTextColor  = f.getInt(this);

            //private final Paint             mHighlightPaint;
            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mHighlightPaint");
            f.setAccessible(true);
            Paint mHighlightPaint  = (Paint)f.get(this);

            //private Path                    mHighlightPath;
            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mHighlightPath");
            f.setAccessible(true);
            Path mHighlightPath  = (Path)f.get(this);


            //private boolean                 mHighlightPathBogus = true;
            f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mHighlightPathBogus");
            f.setAccessible(true);
            boolean mHighlightPathBogus  = f.getBoolean(this);



            //  private long                    mShowCursor;
         //   f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowCursor");
         //   f.setAccessible(true);
           // long mShowCursor  = f.getLong(this);

            mTextPaint.drawableState = getDrawableState();
            int color = mCurTextColor;
            final int        BLINK = 500;


            canvas.save();
            final int compoundPaddingLeft = getCompoundPaddingLeft();
            final int compoundPaddingTop = getCompoundPaddingTop();
            final int compoundPaddingRight = getCompoundPaddingRight();
            final int compoundPaddingBottom = getCompoundPaddingBottom();
            int extendedPaddingTop = getExtendedPaddingTop();
            int extendedPaddingBottom = getExtendedPaddingBottom();

            // translate in by our padding
            canvas.translate(compoundPaddingLeft, extendedPaddingTop );

            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            int spacing = 100;

            int index = 0;
            for (char c : mText.toString().toCharArray()) {

                if (   index >= selStart
                    && index <  selEnd)
                {
                    //TODO: we need to set the selection & cursor state on a per character basis since there is space between them
                }



                canvas.drawBitmap(bmp,index*spacing,0,null);
                canvas.drawText("" + c, index * spacing, 40, mTextPaint);
                index++;
            }
            canvas.restore();


            return;

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }



        super.onDraw(canvas);

    }
}
