package com.hisilicon.android.music.util;

import java.util.List;
import java.lang.IndexOutOfBoundsException;

import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;

/**
 * @author
 */
public class LyricView extends TextView {
    private static final String TAG = "HiMusic_LyricView";
    private TextPaint NotCurrentPaint; // Not Cuurent Lyric Paint
    private TextPaint CurrentPaint; // Cuurent Lyric Paint

    //    private int notCurrentPaintColor = Color.WHITE;// Not Cuurent Lyric color of
    // the Paint
    private int notCurrentPaintColor = color.white;
    //    private int CurrentPaintColor = Color.RED; // Cuurent Lyric color of the
    // Paint
    private int CurrentPaintColor = color.white;

    private Typeface Texttypeface = Typeface.SERIF;
    private Typeface CurrentTexttypeface = Typeface.DEFAULT_BOLD;
    //    private Typeface CurrentTexttypeface = Typeface.SERIF;
    private float width;
    private static Lyric mLyric;
    private int brackgroundcolor = 0xf000000; // Brackground color
    private float lrcTextSize = 22; // Lyric Text Size
    //jly
    //    private float CurrentTextSize = 24;
    private float CurrentTextSize = 40;
    public float mTouchHistoryY;

    private int height;
    private long currentDunringTime; // The duration of the current line of
    // lyrics, use the time to sleep
    private int TextHeight = 20; // Each row spacing
    private boolean lrcInitDone = false;// Is initialised.
    public int index = 0;
    private int lastIndex = 0;
    private List<Sentence> Sentencelist; // Lyric List

    private long currentTime;

    private long sentenctTime;

    private float lrcSpacingMult = 1.0f;

    public float getLrcSpacingMult() {
        return lrcSpacingMult;
    }

    public void setLrcSpacingMult(float lrcSpacingMult) {
        this.lrcSpacingMult = lrcSpacingMult;
    }

    public TextPaint getNotCurrentPaint() {
        return NotCurrentPaint;
    }

    public void setNotCurrentPaint(TextPaint notCurrentPaint) {
        NotCurrentPaint = notCurrentPaint;
    }

    public boolean isLrcInitDone() {
        return lrcInitDone;
    }

    public Typeface getCurrentTexttypeface() {
        return CurrentTexttypeface;
    }

    public void setCurrentTexttypeface(Typeface currrentTexttypeface) {
        CurrentTexttypeface = currrentTexttypeface;
    }

    public void setLrcInitDone(boolean lrcInitDone) {
        this.lrcInitDone = lrcInitDone;
    }

    public float getLrcTextSize() {
        return lrcTextSize;
    }

    public void setLrcTextSize(float lrcTextSize) {
        this.lrcTextSize = lrcTextSize;
    }

    public float getCurrentTextSize() {
        return CurrentTextSize;
    }

    public void setCurrentTextSize(float currentTextSize) {
        CurrentTextSize = currentTextSize;
    }

    public static Lyric getmLyric() {
        return mLyric;
    }

    public void setmLyric(Lyric mLyric) {
        LyricView.mLyric = mLyric;
    }

    public TextPaint getCurrentPaint() {
        return CurrentPaint;
    }

    public void setCurrentPaint(TextPaint currentPaint) {
        CurrentPaint = currentPaint;
    }

    public List<Sentence> getSentencelist() {
        return Sentencelist;
    }

    public void setSentencelist(List<Sentence> sentencelist) {
        Sentencelist = sentencelist;
    }

    public int getNotCurrentPaintColor() {
        return notCurrentPaintColor;
    }

    public void setNotCurrentPaintColor(int notCurrentPaintColor) {
        this.notCurrentPaintColor = notCurrentPaintColor;
    }

    public int getCurrentPaintColor() {
        return CurrentPaintColor;
    }

    public void setCurrentPaintColor(int currrentPaintColor) {
        CurrentPaintColor = currrentPaintColor;
    }

    public Typeface getTexttypeface() {
        return Texttypeface;
    }

    public void setTexttypeface(Typeface texttypeface) {
        Texttypeface = texttypeface;
    }

    public int getBrackgroundcolor() {
        return brackgroundcolor;
    }

    public void setBrackgroundcolor(int brackgroundcolor) {
        this.brackgroundcolor = brackgroundcolor;
    }

    public int getTextHeight() {
        return TextHeight;
    }

    public void setTextHeight(int textHeight) {
        TextHeight = textHeight;
    }

    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public LyricView(Context context, AttributeSet attr, int i) {
        super(context, attr, i);
        init();
    }

    private void init() {
        setFocusable(true);
        // Non highlighted
        NotCurrentPaint = new TextPaint();
        NotCurrentPaint.setAntiAlias(true);

        // highlighted Current Lyric
        CurrentPaint = new TextPaint();
        CurrentPaint.setAntiAlias(true);
    }

    @SuppressLint("ResourceAsColor")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(brackgroundcolor);
        NotCurrentPaint.setColor(notCurrentPaintColor);
        CurrentPaint.setColor(CurrentPaintColor);
        NotCurrentPaint.setTextSize(lrcTextSize);
        NotCurrentPaint.setTypeface(Texttypeface);
        CurrentPaint.setTextSize(CurrentTextSize);
        CurrentPaint.setTypeface(CurrentTexttypeface);

        if (index == -1)
        { return; }

        float plus = currentDunringTime == 0 ? 30
                     : 30
                     + (((float) currentTime - (float) sentenctTime) / (float) currentDunringTime)
                     * (float) 30;
        // Scroll up to this is based on the length of time the lyrics to
        // rolling, the overall shift
        canvas.translate(0, -plus);

        // First draw the current line, then draw the front and the back of his,
        // so as to maintain the current line in the middle of the position
        try {
            if (Sentencelist == null)
            { return; }

            if (Sentencelist.get(index) == null)
            { return; }

            StaticLayout current_lyc_layout = new StaticLayout(Sentencelist.get(index).getContent(),
                    CurrentPaint,(int)width,Alignment.ALIGN_CENTER,lrcSpacingMult,0.0f,true);
            canvas.translate(0,height / 2);
            current_lyc_layout.draw(canvas);
            int current_lyc_height = current_lyc_layout.getLineTop(current_lyc_layout.getLineCount()) - current_lyc_layout.getLineTop(0);

            int noCurrent_lyc_descent = 0;
            int upper_lyc_size = height / 2;
            int i = index - 1;
            while (upper_lyc_size>=0 && i>=0) {
                StaticLayout upper_layout = new StaticLayout(Sentencelist.get(i).getContent(),
                        NotCurrentPaint,(int)width,Alignment.ALIGN_CENTER,lrcSpacingMult,0.0f,true);
                int lyc_height = upper_layout.getLineTop(upper_layout.getLineCount())
                        - upper_layout.getLineTop(0);
                lyc_height += TextHeight;
                if (upper_lyc_size - lyc_height < 0)
                    break;
                canvas.translate(0,-lyc_height);
                upper_layout.draw(canvas);
                upper_lyc_size = upper_lyc_size - lyc_height;
                i--;
            }

            canvas.translate(0,height / 2 - upper_lyc_size + current_lyc_height + TextHeight);
            int lower_lyc_size = height / 2 - current_lyc_height;
            i = index + 1;
            while (lower_lyc_size>=0 && i<Sentencelist.size()) {
                StaticLayout lower_layout = new StaticLayout(Sentencelist.get(i).getContent(),
                        NotCurrentPaint,(int)width,Alignment.ALIGN_CENTER,lrcSpacingMult,0.0f,true);
                int lyc_height = lower_layout.getLineTop(lower_layout.getLineCount())
                        - lower_layout.getLineTop(0);
                lyc_height += TextHeight;
                if (lower_lyc_size - lyc_height < 0)
                    break;
                lower_layout.draw(canvas);
                canvas.translate(0,lyc_height);
                lower_lyc_size = lower_lyc_size - lyc_height;
                i++;
            }

        } catch (IndexOutOfBoundsException e) {
            Log.e( TAG, "Sentencelist.get", e);
        }
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        width = w; // remember the center of the screen
        height = h;
        // middleY = h * 0.5f;
    }

    //
    /**
     * @param time
     *            The time axis current lyrics
     * @return null
     */
    public void updateIndex(long time) {
        this.currentTime = time;

        if (null == mLyric)
        { return; }

        index = mLyric.getNowSentenceIndex(time);

        if (index != -1) {
            if (Sentencelist == null)
            { return; }

            Sentence sen = Sentencelist.get(index);
            sentenctTime = sen.getFromTime();
            currentDunringTime = sen.getDuring();
        }
    }

}
