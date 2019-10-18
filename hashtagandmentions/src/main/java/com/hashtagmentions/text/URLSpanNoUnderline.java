package com.hashtagmentions.text;

import android.text.TextPaint;
import android.text.style.UnderlineSpan;

public class URLSpanNoUnderline extends UnderlineSpan {
    public URLSpanNoUnderline() {
    }
    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
