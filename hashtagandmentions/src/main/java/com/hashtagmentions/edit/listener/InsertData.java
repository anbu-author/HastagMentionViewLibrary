package com.hashtagmentions.edit.listener;


import com.hashtagmentions.model.FormatRange;


public interface InsertData {

  CharSequence charSequence();

  FormatRange.FormatData formatData();

  int color();
}
