package com.keimoger.hackerembassy.util.fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentNavigator {
    private final FragmentManager mFragmentManager;
    private final int mContainerId;
    public FragmentFactory mFragmentFactory;

    public FragmentNavigator(FragmentManager fragmentManager, int containerId, FragmentFactory fragmentFactory) {
        this.mFragmentManager = fragmentManager;
        this.mContainerId = containerId;
        this.mFragmentFactory = fragmentFactory;
    }

    public void openFragment(String tag) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null)
            fragmentTransaction.hide(currentFragment);

        Fragment targetFragment = mFragmentManager.findFragmentByTag(tag);
        if (targetFragment == null) {
            fragmentTransaction.add(mContainerId, mFragmentFactory.getFragment(tag), tag);
        } else {
            fragmentTransaction.show(targetFragment);
        }

        fragmentTransaction.commitNow();
    }

    public Fragment getCurrentFragment() {
        for (Fragment fragment : mFragmentManager.getFragments()) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }
}
