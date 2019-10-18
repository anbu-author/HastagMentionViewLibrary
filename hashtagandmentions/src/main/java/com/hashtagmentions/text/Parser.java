package com.hashtagmentions.text;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.hashtagmentions.text.listener.OnMentionClicked;
import com.hashtagmentions.text.listener.ParserConverter;

public class Parser implements ParserConverter {

  Context context;
  private OnMentionClicked mOnMentionClicked;

  public Parser(Context context) {
    this.context=context;
  }
  public Parser(Context context, OnMentionClicked mOnMentionClicked) {
    this.context=context;
    this.mOnMentionClicked = mOnMentionClicked;
  }

  @Override public Spanned convert(CharSequence source) {
    if (TextUtils.isEmpty(source)) return new SpannableString("");
    String sourceString = source.toString();
    sourceString = LinkUtil.replaceUrl(sourceString);
    return Html.fromHtml(sourceString, null, new HtmlTagHandler(context, mOnMentionClicked));
  }

  public void setOnItemClicked(OnMentionClicked onMentionClicked) {
      mOnMentionClicked = onMentionClicked;
  }
}
