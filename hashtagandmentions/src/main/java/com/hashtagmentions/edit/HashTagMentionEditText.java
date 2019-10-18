package com.hashtagmentions.edit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

import com.hashtagmentions.edit.listener.InsertData;
import com.hashtagmentions.edit.listener.MentionInputConnection;
import com.hashtagmentions.edit.listener.MentionTextWatcher;
import com.hashtagmentions.edit.util.FormatRangeManager;
import com.hashtagmentions.edit.util.RangeManager;
import com.hashtagmentions.model.FormatRange;
import com.hashtagmentions.model.Range;
import com.hashtagmentions.text.ClickableForegroundColorSpan;
import com.hashtagmentions.text.CustomTypefaceSpan;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class HashTagMentionEditText extends AppCompatMultiAutoCompleteTextView {
    private Runnable mAction;
    char[] additionalSymbols = new char[]{'_'};
    private boolean mIsSelected;
    private HashTagMentionEditText mTextView;
    private ArrayList<Character> mAdditionalHashTagChars;

    public HashTagMentionEditText(Context context) {
        super(context);
        init();
    }

    public HashTagMentionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public HashTagMentionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MentionInputConnection(super.onCreateInputConnection(outAttrs), true, this);
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        super.setText(text, type);
        //hack, put the cursor at the end of text after calling setText() method
        if (mAction == null) {
            mAction = new Runnable() {
                @Override
                public void run() {

                    setSelection(getText().length());
                }
            };
        }
        post(mAction);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //avoid infinite recursion after calling setSelection()
        if (null != mRangeManager && !mRangeManager.isEqual(selStart, selEnd)) {
            //if user cancel a selection of mention string, reset the state of 'mIsSelected'
            Range closestRange = mRangeManager.getRangeOfClosestMentionString(selStart, selEnd);
            if (closestRange != null && closestRange.getTo() == selEnd) {
                mIsSelected = false;
            }

            Range nearbyRange = mRangeManager.getRangeOfNearbyMentionString(selStart, selEnd);
            //if there is no mention string nearby the cursor, just skip
            if (null != nearbyRange) {
                //forbid cursor located in the mention string.
                if (selStart == selEnd) {
                    setSelection(nearbyRange.getAnchorPosition(selStart));
                } else {
                    if (selEnd < nearbyRange.getTo()) {
                        setSelection(selStart, nearbyRange.getTo());
                    }
                    if (selStart > nearbyRange.getFrom()) {
                        setSelection(nearbyRange.getFrom(), selEnd);
                    }
                }
            }
        }
    }


    public void insert(InsertData insertData) {
        if (null != insertData) {
            CharSequence charSequence = insertData.charSequence();
            Editable editable = getText();
            int start = getSelectionStart();
            int end = start + charSequence.length();
            editable.insert(start, charSequence);
            FormatRange.FormatData format = insertData.formatData();
            FormatRange range = new FormatRange(start, end);
            range.setConvert(format);
            range.setRangeCharSequence(charSequence);
            mRangeManager.add(range);

            int color = insertData.color();
            editable.setSpan(new ForegroundColorSpan(color), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void insert(CharSequence charSequence) {
        insert(new Default(charSequence));
    }

    class Default implements InsertData {

        private final CharSequence charSequence;

        public Default(CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        @Override
        public CharSequence charSequence() {
            return charSequence;
        }

        @Override
        public FormatRange.FormatData formatData() {
            return new DEFAULT();
        }

        @Override
        public int color() {
            return Color.RED;
        }

        class DEFAULT implements FormatRange.FormatData {
            @Override
            public CharSequence formatCharSequence() {
                return charSequence;
            }
        }
    }

    public CharSequence getFormatCharSequence() {
        String text = getText().toString();
        return mRangeManager.getFormatCharSequence(text);
    }

    public void clear() {
        mRangeManager.clear();
        setText("");
    }

    protected FormatRangeManager mRangeManager;

    private void init() {
        mRangeManager = new FormatRangeManager();
        //disable suggestion
        addTextChangedListener(new MentionTextWatcher(this));
        mAdditionalHashTagChars = new ArrayList<>();
        if (additionalSymbols != null) {
            for (char additionalChar : additionalSymbols) {
                mAdditionalHashTagChars.add(additionalChar);
            }
        }

        handle(this);
    }


    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            if (text.length() > 0) {
                eraseAndColorizeAllText(text);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final TextWatcher setDropTownWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            Layout layout = mTextView.getLayout();
            int pos = mTextView.getSelectionStart();
            int sLine = 0;
            int line = layout.getLineForOffset(pos);
            if (line > 9) {
                sLine = 9;
            } else {
                sLine = line;

            }
//      Utils.loge("Line: ",""+line);
            int baseline = layout.getLineBaseline(sLine);
            int bottom = mTextView.getHeight() - 80;
            int offset = 64;
            mTextView.setDropDownHorizontalOffset(-1 * offset);
            mTextView.setDropDownWidth(mTextView.getWidth() + offset * 2);
            mTextView.setDropDownVerticalOffset(baseline - bottom);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    public void handle(HashTagMentionEditText textView) {
        if (mTextView == null) {
            mTextView = textView;

            mTextView.addTextChangedListener(mTextWatcher);
            // in order to use spannable we have to set buffer type
//            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "arial_regular.ttf");
//            mTextView.setTypeface(font);
            mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);
            setColorsToAllHashTags(mTextView.getText());

//            mTextView.addTextChangedListener(setDropTownWatcher);

        } else {
            throw new RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView");
        }

    }

    private void eraseAndColorizeAllText(CharSequence text) {

        Spannable spannable = ((Spannable) mTextView.getText());

        CharacterStyle[] spans = spannable.getSpans(0, text.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            spannable.removeSpan(span);
        }

        setColorsToAllHashTags(text);
    }

    private void setColorsToAllHashTags(CharSequence text) {

        int startIndexOfNextHashSign;

        int index = 0;
        while (index < text.length() - 1) {
            char sign = text.charAt(index);
            int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
//      Utils.loge("Sign", "Char: " + text.charAt(index)+" Position: "+index);

            if (sign == '#') {

                if (text.charAt(index + 1) != '#' && text.charAt(index + 1) != ' ' && String.valueOf(text.charAt(index + 1)).matches("([A-Za-z_])")) {

                    Log.e("Sign", "setColorsToAllHashTags: " + text.charAt(index) + " Position: " + (index));

                    startIndexOfNextHashSign = index;

                    nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);

                    setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
                }

            }

//            else if (sign == '@') {
//
//                if (text.charAt(index + 1) != '@' && text.charAt(index + 1) != ' ') {
//
//                    Log.e("Sign", "setColorsToAllMentions: " + text.charAt(index) + " Position: " + (index));
//
//                    startIndexOfNextHashSign = index;
//
//                    nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);
//
//                    setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
//                }
//            }

            index = nextNotLetterDigitCharIndex;
        }
    }

    private int findNextValidHashTagChar(CharSequence text, int start) {

        int nonLetterDigitCharIndex = -1; // skip first sign '#"
        for (int index = start + 1; index < text.length(); index++) {

            char sign = text.charAt(index);

            boolean isValidSign = Character.isLetterOrDigit(sign) || mAdditionalHashTagChars.contains(sign);
            if (!isValidSign) {
                nonLetterDigitCharIndex = index;
                break;
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length();
        }

        return nonLetterDigitCharIndex;
    }

    private void setColorForHashTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

//        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "arial_bold.ttf");


        span = new ForegroundColorSpan(Color.BLUE);

//        s.setSpan(new CustomTypefaceSpan(font), startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public List<String> getAllHashTags(boolean withHashes) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            char letter = text.charAt(spannable.getSpanStart(span));
            if (letter == '#')
                hashTags.add(
                        text.substring(!withHashes ? spannable.getSpanStart(span) + 1/*skip "#" sign*/
                                        : spannable.getSpanStart(span),
                                spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(hashTags);
    }

    public List<String> getAllMentions(boolean withHashes) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            char letter = text.charAt(spannable.getSpanStart(span));
            if (letter == '@')
                hashTags.add(
                        text.substring(!withHashes ? spannable.getSpanStart(span) + 1/*skip "@" sign*/
                                        : spannable.getSpanStart(span),
                                spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(hashTags);
    }

    public List<String> getAllHashTags() {
        return getAllHashTags(false);
    }

    public List<String> getAllMentions() {
        return getAllMentions(false);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RangeManager getRangeManager() {
        return mRangeManager;
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }
}
