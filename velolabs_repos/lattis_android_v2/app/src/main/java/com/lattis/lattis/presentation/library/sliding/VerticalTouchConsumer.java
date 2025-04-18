package com.lattis.lattis.presentation.library.sliding;

import android.view.MotionEvent;
import android.view.View;


/**
 * @author pa.gulko zTrap (05.07.2017)
 */
class VerticalTouchConsumer extends TouchConsumer {
    private boolean mGoingUp = false;
    private boolean mGoingDown = false;

    VerticalTouchConsumer(SlideUpBuilder builder, LoggerNotifier notifier, AnimationProcessor animationProcessor) {
        super(builder, notifier, animationProcessor);
    }

    boolean consumeBottomToTop(View touchedView, MotionEvent event){
        float touchedArea = event.getY();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
//                mCanSlide = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;

                if(difference<0 && moveTo > mBuilder.mAlsoScrollView.getY()){
                    moveTo = mBuilder.mAlsoScrollView.getY()-100;
                    mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                }else if(difference>0 && moveTo < mBuilder.mAlsoScrollView.getY()+mBuilder.mAlsoScrollView.getHeight() && moveTo > mBuilder.mAlsoScrollView.getY()-100){
                    moveTo = mBuilder.mAlsoScrollView.getY()-100;
                    mBuilder.mSliderView.setVisibility(View.GONE);

                    mPrevPositionY = event.getRawY();
                    mPrevPositionX = event.getRawX();
                    return true;
                }

                float percents = moveTo * 100 / mBuilder.mSliderView.getHeight();
                calculateDirection(event);

                if (moveTo > 0 && mCanSlide){
                    mNotifier.notifyPercentChanged(percents);
                    mBuilder.mSliderView.setTranslationY(moveTo);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mBuilder.mSliderView.getVisibility()==View.VISIBLE) {
                    float slideAnimationFrom = mBuilder.mSliderView.getTranslationY();
                    if (slideAnimationFrom == mViewStartPositionY) {
                        return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                    }
                    boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() > mBuilder.mSliderView.getHeight() / 5;

                    if (scrollableAreaConsumed && mGoingDown) {
                        mAnimationProcessor.setValuesAndStart(slideAnimationFrom, mBuilder.mAlsoScrollView.getY()-50);
                    } else {
                        mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0);
                    }
                }
                mCanSlide = true;
                mGoingUp = false;
                mGoingDown = false;

                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }

    boolean consumeTopToBottom(View touchedView, MotionEvent event){
        float touchedArea = event.getY();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float percents = moveTo * 100 / -mBuilder.mSliderView.getHeight();
                calculateDirection(event);

                if (moveTo < 0 && mCanSlide){
                    mNotifier.notifyPercentChanged(percents);
                    mBuilder.mSliderView.setTranslationY(moveTo);
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = -mBuilder.mSliderView.getTranslationY();
                if (slideAnimationFrom == mViewStartPositionY){
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                }
                boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() < -mBuilder.mSliderView.getHeight() / 5;
            
                if (scrollableAreaConsumed && mGoingUp){
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, mBuilder.mSliderView.getHeight() + mBuilder.mSliderView.getTop());
                }else {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0);
                }
                mCanSlide = true;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }

    private void calculateDirection(MotionEvent event) {
        mGoingUp = mPrevPositionY - event.getRawY() > 0;
        mGoingDown = mPrevPositionY - event.getRawY() < 0;
    }
}
