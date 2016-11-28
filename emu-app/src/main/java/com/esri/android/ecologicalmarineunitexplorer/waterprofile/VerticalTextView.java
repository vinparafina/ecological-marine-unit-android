package com.esri.android.ecologicalmarineunitexplorer.waterprofile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */

// This class was taken from http://stackoverflow.com/questions/2888780/is-it-possible-to-write-vertically-in-a-textview-in-android

public class VerticalTextView extends TextView {

  private int _width, _height;
  private final Rect _bounds = new Rect();

  public VerticalTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public VerticalTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VerticalTextView(Context context) {
    super(context);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    // vise versa
    _height = getMeasuredWidth();
    _width = getMeasuredHeight();
    setMeasuredDimension(_width, _height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.save();

    canvas.translate(_width, _height);
    canvas.rotate(-90);

    TextPaint paint = getPaint();
    paint.setColor(getTextColors().getDefaultColor());

    String text = text();

    paint.getTextBounds(text, 0, text.length(), _bounds);
    canvas.drawText(text, getCompoundPaddingLeft(), (_bounds.height() - _width) / 2, paint);

    canvas.restore();
  }

  private String text() {
    return super.getText().toString();
  }
}