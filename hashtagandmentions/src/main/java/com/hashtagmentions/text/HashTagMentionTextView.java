package com.hashtagmentions.text;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

import com.hashtagmentions.text.listener.ParserConverter;


public class HashTagMentionTextView extends AppCompatTextView implements ClickableForegroundColorSpan.OnHashTagClickListener {

  private static final String TAG = HashTagMentionTextView.class.getSimpleName();
  private CharSequence mOriginalText;

  char[] additionalSymbols = new char[]{'_'};

  public HashTagMentionTextView(Context context) {
    this(context, null);
  }

  public HashTagMentionTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HashTagMentionTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }


  @Override public void setText(CharSequence text, BufferType type) {
    mOriginalText = text;

    if (!TextUtils.isEmpty(text) && null != mParserConverter) {
      text = mParserConverter.convert(text);
    }
    text = wrapper(text);
    Log.e("HASHtag", "setText: "+text );
    setColorsToAllHashTags(text);

    super.setText(text, type);
  }

  public CharSequence wrapper(CharSequence text) {
    return text;
  }

  private ParserConverter mParserConverter;

  public void setParserConverter(ParserConverter parserConverter) {
    mParserConverter = parserConverter;
  }

  public CharSequence getOriginalText() {
    return mOriginalText;
  }

  private void setColorsToAllHashTags(CharSequence text) {

    int startIndexOfNextHashSign;

    int index = 0;
    while (index < text.length() - 1) {
      char sign = text.charAt(index);
      int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1

      if (sign == '#') {

        if (text.charAt(index + 1) != '#' && text.charAt(index + 1) != ' '&& String.valueOf(text.charAt(index + 1)).matches("([A-Za-z_])")) {

//          Log.e("Sign", "setColorsToAllHashTags: " + text.charAt(index) + " Position: " + (index));

          startIndexOfNextHashSign = index;

          nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);
          Log.e("Mentiontext", "startIndexOfNextHashSign: "+startIndexOfNextHashSign+" nextNotLetterDigitCharIndex"+nextNotLetterDigitCharIndex );
          setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex,text);
        }

      }


      index = nextNotLetterDigitCharIndex;
    }
  }

  private int findNextValidHashTagChar(CharSequence text, int start) {

    int nonLetterDigitCharIndex = -1; // skip first sign '#"
    for (int index = start + 1; index < text.length(); index++) {

      char sign = text.charAt(index);

      boolean isValidSign = Character.isLetterOrDigit(sign) ;
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

  private void setColorForHashTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex, CharSequence text) {
    Spannable s = (Spannable) text ;

    CharacterStyle span;

//    if (mOnHashTagClickListener != null) {
      span = new ClickableForegroundColorSpan(Color.BLUE, this);
//    } else {
      // no need for clickable span because it is messing with selection when click
//      span = new ForegroundColorSpan(Color.BLUE);
//    }

    Log.e(TAG, "setColorForHashTagToTheEnd: "+s.length() );
    if(s!=null){
      s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

  }

  @Override
  public void onHashTagClicked(String hashTag) {
    Toast.makeText(getContext(), hashTag, Toast.LENGTH_SHORT).show();
//    mOnHashTagClickListener.onHashTagClicked(hashTag);
  }

  public interface OnHashTagClickListener {
    void onHashTagClicked(String hashTag);
  }





}
