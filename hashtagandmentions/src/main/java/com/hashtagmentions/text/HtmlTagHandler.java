package com.hashtagmentions.text;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;

import com.hashtagmentions.text.listener.OnMentionClicked;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.util.Map;


public class HtmlTagHandler implements Html.TagHandler {
    private static final String TAG = "tag";
    private static final String USER = "user";
    private static final String ID = "id";
    public static final String NAME = "name";
    private Context context;
    private OnMentionClicked onMentionClicked;

    public HtmlTagHandler(Context context, OnMentionClicked mOnMentionClicked) {
        this.context = context;
        this.onMentionClicked = mOnMentionClicked;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.toLowerCase().equals(TAG) || tag.toLowerCase().equals(USER)) {
//            Log.e(TAG, "opening : " + opening);
            if (opening) {
                Map<String, String> map = HtmlParserUtil.parseStart(tag, output, xmlReader);
                String id = map.get(ID);
                String name = map.get(NAME);
//                Log.e(TAG, "handleTag: " + id + " name : " + name + " outputLength: " + output.length());
                output.setSpan(new TagBean(name, id), output.length(), output.length(), 0);
                URLSpanNoUnderline span = new URLSpanNoUnderline();
                output.setSpan(span,output.length(),output.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/robo_med.ttf");
//                output.setSpan(new CustomTypefaceSpan(font), output.length(), output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                output.setSpan(new RelativeSizeSpan(1.2f), output.length(), output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                endTag(tag, output, xmlReader);
            }
        }
    }

    private Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private void endTag(String tag, Editable text, XMLReader xmlReader) {
        //The myfont tag can't be naked, that is, it must have a html tag, or other content in front, otherwise the font size will not work.
        //That is, getlast becomes taken from the back, and the final content ranges from 0 to the full length of the text.
        int len = text.length();
        Object obj = getLast(text, TagBean.class);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
//        Log.e("AAA", "where:" + where + ",len:" + len);
        if (where != len) {
            final TagBean t = (TagBean) obj;

//            Log.e(TAG, "endTag: " + t);

            if (null != t) {
                text.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Log.e(TAG, "onClick: " + t);
                        try {
                            JSONObject jsonObject=new JSONObject(t.toString());

                            onMentionClicked.onClick(jsonObject);
//                            widget.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, where, len, 0);
                URLSpanNoUnderline span = new URLSpanNoUnderline();
                text.setSpan(span, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/robo_med.ttf");
//                text.setSpan(new CustomTypefaceSpan(font), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                text.setSpan(new RelativeSizeSpan(1.2f), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }
    }
}