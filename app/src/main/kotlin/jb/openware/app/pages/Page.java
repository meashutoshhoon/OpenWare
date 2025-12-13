package in.afi.codekosh.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.transition.MaterialSharedAxis;

import in.afi.codekosh.R;

public abstract class Page extends Fragment {

    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


    protected void addBaseFragment(Page page) {
        addPage(page, false);
    }

    protected void replacePage(Page page) {
        replacePage(page, true);
    }

    protected void replacePage(Page page, boolean useSharedAnimation) {
        FragmentManager fragmentManager = requireFragmentActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (useSharedAnimation) {
            setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
            page.setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
            page.setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        } else {
            transaction.setCustomAnimations(R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            );
        }

        transaction.replace(getContainerId(), page);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view, savedInstanceState);
        logic(view, savedInstanceState);
    }

    protected void replaceFragmentWithOutAnimation(Page page) {
        FragmentManager fragmentManager = requireFragmentActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(getContainerId(), page);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @NonNull
    protected Drawable getRoundRectSelectorDrawable() {
        Drawable maskDrawable = createRoundRectDrawable(dp(8));
        ColorStateList colorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{(Color.parseColor("Grey") & 0x00ffffff) | 0x19000000});
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    @NonNull
    private Drawable createRoundRectDrawable(int rad) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(-1);
        return defaultDrawable;
    }

    protected void deleteStackFragment() {
        FragmentManager fragmentManager = requireFragmentActivity().getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();

        // Remove all fragments above the home fragment
        for (int i = 0; i < backStackEntryCount; i++) {
            fragmentManager.popBackStack();
        }
    }

    protected void removeFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.popBackStack();
    }

    protected void addPage(Page page) {
        addPage(page, true);
    }

    protected void addPage(Page page, boolean addToBackStack) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(getContainerId(), page);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    protected View inflateLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Call the createView method instead of inflating the layout
        return createView(inflater, container, null);
    }

    protected int getContainerId() {
        // Replace with the ID of the container where you want to display the fragments
        return getId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Retain this fragment across configuration changes
        setRetainInstance(true);
    }

    protected FragmentActivity requireFragmentActivity() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Fragment is not attached to an activity.");
        }
        return activity;
    }

    protected void replaceTopFragment(Page page, boolean animation) {
        FragmentManager fragmentManager = requireFragmentActivity().getSupportFragmentManager();

        // Check if there are any fragments in the back stack
        if (fragmentManager.getBackStackEntryCount() > 0) {
            // Remove the topmost fragment from the back stack
            fragmentManager.popBackStackImmediate();

            // Replace it with the new fragment
            replacePage(page, animation);
        } else {
            // No fragments in the back stack, replace the current fragment with the new fragment
            replacePage(page, animation);
        }
    }

    protected void removeAllFragmentsAndChangeBaseFragment(Page newBaseFragment, boolean useSharedAnimation) {
        FragmentManager fragmentManager = requireFragmentActivity().getSupportFragmentManager();

        // Clear the back stack
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Replace the base fragment with the new base fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (useSharedAnimation) {
            // Set exit transition for Fragment A
            setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
            newBaseFragment.setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
            newBaseFragment.setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        } else {
            transaction.setCustomAnimations(R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            );
        }
        transaction.replace(getContainerId(), newBaseFragment);
        transaction.commit();
    }


    protected void goBack() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
    }


    protected void openActivity(Activity activityToOpen) {
        openActivity(activityToOpen, false);
    }

    protected void openActivity(Activity activityToOpen, boolean t) {
        // Create an Intent to open the desired activity
        Intent intent = new Intent(requireFragmentActivity(), activityToOpen.getClass());
        if (t) {
            startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireFragmentActivity().finish();
        }

    }


    protected void restartFragment() {
        if (getActivity() != null) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.detach(this);
            fragmentTransaction.attach(this);
            fragmentTransaction.commit();
        }
    }


    protected void repeatTask(int repetitions, Runnable task) {
        for (int i = 0; i < repetitions; i++) {
            task.run();
        }
    }


    public abstract void getThemeDescriptions(ThemeBuilder themeBuilder);

    protected Context getApplicationContext() {
        return ApplicationLoader.applicationContext;
    }


    @Override
    public void onResume() {
        super.onResume();

        ThemeBuilder themeBuilder = createThemeBuilder();
        getThemeDescriptions(themeBuilder);
    }

    private ThemeBuilder createThemeBuilder() {
        return new ThemeBuilder(requireContext()) {
            @Override
            public void setValues(ThemeBuilder themeBuilder) {
                // Leave this method implementation empty in the base fragment
            }
        };
    }

    public int dp(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    protected abstract void initialize(View view, Bundle saved);

    protected abstract void logic(View view, Bundle saved);


}