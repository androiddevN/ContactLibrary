package com.trivedi.contact.UI.utils;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class InitiateSearch {

    public static void handleToolBar(final Context context, final CardView search, final View view, final EditText editText) {

        final Animation fade_in = AnimationUtils.loadAnimation(context.getApplicationContext(), android.R.anim.fade_in);
        final Animation fade_out = AnimationUtils.loadAnimation(context.getApplicationContext(), android.R.anim.fade_out);

        if (search.getVisibility() == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Animator animatorHide = ViewAnimationUtils.createCircularReveal(search,
                        search.getWidth() - (int) convertDpToPixel(30, context),
                        (int) convertDpToPixel(23, context),
                        (float) Math.hypot(search.getWidth(), search.getHeight()),
                        0);
                animatorHide.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        search.setVisibility(View.INVISIBLE);
                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorHide.setDuration(400);
                animatorHide.start();
            } else {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.startAnimation(fade_out);
                view.setVisibility(View.VISIBLE);
                search.setVisibility(View.INVISIBLE);
            }
            editText.setText("");
            search.setEnabled(false);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Animator animator = ViewAnimationUtils.createCircularReveal(search,
                        search.getWidth() - (int) convertDpToPixel(56, context),
                        (int) convertDpToPixel(23, context),
                        0,
                        (float) Math.hypot(search.getWidth(), search.getHeight()));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        view.startAnimation(fade_in);
                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                search.setVisibility(View.VISIBLE);
                if (search.getVisibility() == View.VISIBLE) {
                    animator.setDuration(400);
                    animator.start();
                    search.setEnabled(true);
                }
                fade_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                search.setVisibility(View.VISIBLE);
                search.setEnabled(true);
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }
    }


    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }
}


