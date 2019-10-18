package com.hashtagmentions.text.listener;

import android.text.Spanned;

public interface ParserConverter {

  Spanned convert(CharSequence source);
}
