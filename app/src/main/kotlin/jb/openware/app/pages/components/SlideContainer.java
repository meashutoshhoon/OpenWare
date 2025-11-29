package in.afi.codekosh.pages.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.transition.TransitionManager;

import com.google.android.material.transition.MaterialSharedAxis;

import jb.openware.app.pages.components.SlideView;

public class SlideContainer extends LinearLayout {
    private int currentSlideIndex = 0;

    public SlideContainer(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    public boolean isCurrentSlideView(SlideView slideView) {
        // Check if the given SlideView instance is the currently shown slide
        return getChildAt(currentSlideIndex) == slideView;
    }

    public int getCurrentSlideIndex() {
        return currentSlideIndex;
    }

    public void showSlide(int indexToShow) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (i == indexToShow) {
                // Show the desired slide with MaterialSharedAxis animation
                animateSlideWithSharedAxis(child, true);
                // Update the current slide index
                currentSlideIndex = i;
            } else {
                // Hide other slides with MaterialSharedAxis animation
                animateSlideWithSharedAxis(child, false);
            }
        }
    }

    public void showPreviousSlide() {
        int targetIndex = Math.max(0, currentSlideIndex - 1);

        if (currentSlideIndex != targetIndex) {
            View currentSlide = getChildAt(currentSlideIndex);
            View previousSlide = getChildAt(targetIndex);


            MaterialSharedAxis shared = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            TransitionManager.beginDelayedTransition((ViewGroup) getParent(), shared);
            currentSlide.setVisibility(View.GONE);

            MaterialSharedAxis sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            TransitionManager.beginDelayedTransition((ViewGroup) getParent(), sharedAxis);
            previousSlide.setVisibility(View.VISIBLE);


            currentSlideIndex = targetIndex;
        }
    }

    private void animateSlideWithSharedAxis(View view, boolean show) {
        MaterialSharedAxis sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.X, !show);
        TransitionManager.beginDelayedTransition((ViewGroup) getParent(), sharedAxis);

        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public void addSlideView(SlideView slideView) {
        // Add a SlideView to the SlideContainer's LinearLayout
        addView(slideView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }
}
