/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sobinary.clockplus.app;

import android.preference.DialogPreference;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerDialog extends DialogPreference 
{
	private Context cont;
	private ColorPickerView colorPickerView;

	public ColorPickerDialog(Context cont, AttributeSet attrs) 
	{
		super(cont, attrs);
		this.cont = cont;
		setPersistent(false);
	}

	@Override
	public View onCreateDialogView()
	{
		return new ColorPickerView(cont, 0xffffffff);
	}

	@Override
	protected void onBindDialogView(View view) 
	{
		super.onBindDialogView(view);
		colorPickerView = (ColorPickerView)view;
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		colorPickerView.setColor(prefs.getInt("clcol", 0xffffffff));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) 
	{
		super.onDialogClosed(positiveResult);
		if(positiveResult){
			SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
			prefs.edit().putInt("clcol", colorPickerView.getColor()).commit();
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	private static class ColorPickerView extends View 
	{
		private static final int CENTER_X = 300;
		private static final int CENTER_Y = 300;
		private static final int CENTER_RADIUS = 32;
		
		private Paint mPaint;
		private Paint mCenterPaint;
		private final int[] mColors;
		private RectF rect;

		private boolean mTrackingCenter;
		private boolean mHighlightCenter;


		public ColorPickerView(Context c, int color) 
		{
			super(c);
			rect = new RectF();
			mColors = new int[] {
					0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
					0xFFFFFF00, 0xFFFFFFFF, 0xFF000000
			};
			Shader s = new SweepGradient(0, 0, mColors, null);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(s);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(50);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(8);
		}

		public int getColor()
		{
			return mCenterPaint.getColor();
		}

		public void setColor(int color)
		{
			mCenterPaint.setColor(color);
		}

		@Override
		protected void onDraw(Canvas canvas) 
		{
			float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;

			canvas.translate(CENTER_X, CENTER_X);

			rect.set(-r, -r, r, r);
			canvas.drawOval(rect, mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

			if (mTrackingCenter) {
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);

				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else {
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0,
						CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
		{
			setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
		}

		private int floatToByte(float x) {
			int n = java.lang.Math.round(x);
			return n;
		}
		private int pinToByte(int n) {
			if (n < 0) {
				n = 0;
			} else if (n > 255) {
				n = 255;
			}
			return n;
		}

		private int ave(int s, int d, float p) {
			return s + java.lang.Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) 
		{
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int)p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i+1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}


		@SuppressWarnings("unused")
		public int rotateColor(int color, float rad) {
			float deg = rad * 180 / 3.1415927f;
			int r = Color.red(color);
			int g = Color.green(color);
			int b = Color.blue(color);

			ColorMatrix cm = new ColorMatrix();
			ColorMatrix tmp = new ColorMatrix();

			cm.setRGB2YUV();
			tmp.setRotate(0, deg);
			cm.postConcat(tmp);
			tmp.setYUV2RGB();
			cm.postConcat(tmp);

			final float[] a = cm.getArray();

			int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
			int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
			int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

			return Color.argb(Color.alpha(color), pinToByte(ir),
					pinToByte(ig), pinToByte(ib));
		}

		private static final float PI = 3.1415926f;

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - CENTER_X;
			float y = event.getY() - CENTER_Y;
			boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTrackingCenter = inCenter;
				if (inCenter) {
					mHighlightCenter = true;
					invalidate();
					break;
				}
			case MotionEvent.ACTION_MOVE:
				if (mTrackingCenter) {
					if (mHighlightCenter != inCenter) {
						mHighlightCenter = inCenter;
						invalidate();
					}
				} else {
					float angle = (float)java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
							float unit = angle/(2*PI);
					if (unit < 0) {
						unit += 1;
					}
					mCenterPaint.setColor(interpColor(mColors, unit));
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mTrackingCenter) {
					if (inCenter) {
					}
					mTrackingCenter = false;    // so we draw w/o halo
					invalidate();
				}
				break;
			}
			return true;
		}
	}

}
