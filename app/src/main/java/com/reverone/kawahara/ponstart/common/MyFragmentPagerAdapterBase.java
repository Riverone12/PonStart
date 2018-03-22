package com.reverone.kawahara.ponstart.common;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 *
 * Created by kawahara on 2017/06/01.
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 */

public abstract class MyFragmentPagerAdapterBase<T> extends MyPagerAdapterBase<T> {

    private FragmentManager fragmentManager;
    private FragmentTransaction currentTransaction = null;
    private Fragment currentPrimaryItem = null;

    @SuppressWarnings("unused")
    public MyFragmentPagerAdapterBase(FragmentManager fm) {
        super();
        fragmentManager = fm;
    }

    /*
    public MyFragmentPagerAdapterBase(FragmentManager fm, T... items) {
        super(items);
        fragmentManager = fm;
    }
    */

    @SuppressWarnings("unused")
    public MyFragmentPagerAdapterBase(FragmentManager fm, List<T> items) {
        super(items);
        fragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position and item.
     * @param item item of this page.
     * @param position position of this page.
     * @return fragment that represent this page.
     */
    public abstract Fragment getFragment(T item, int position);

    @SuppressWarnings("unused")
    public Fragment getAttachedFragment(int position) {
        final IdentifiedItem<T> item = getItemWithId(position);
        final String name = makeFragmentName(item.id);
        return fragmentManager.findFragmentByTag(name);
    }

    @SuppressLint("CommitTransaction")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction();
        }

        final IdentifiedItem<T> item = getItemWithId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(item.id);
        Fragment fragment = fragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            currentTransaction.attach(fragment);
        } else {
            fragment = getFragment(item.item, position);
            currentTransaction.add(container.getId(), fragment,
                    makeFragmentName(item.id));
        }
        if (fragment != currentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return super.instantiateItem(container, position);
    }


    @SuppressLint("CommitTransaction")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction();
        }
        String name = makeFragmentName(((IdentifiedItem) object).id);
        Fragment f = fragmentManager.findFragmentByTag(name);
        if (f != null) {
            currentTransaction.detach(f);
        }
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void remove(int position) throws IndexOutOfBoundsException {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction();
        }
        String name = makeFragmentName(getItemId(position));
        Fragment f = fragmentManager.findFragmentByTag(name);
        if (f != null) {
            currentTransaction.remove(f);
        }
        super.remove(position);
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void clear() {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction();
        }
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    currentTransaction.remove(fragment);
                }
            }
        }
        super.clear();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = fragmentManager.findFragmentByTag(makeFragmentName(getItemId(position)));
        if (fragment != currentPrimaryItem) {
            if (currentPrimaryItem != null) {
                currentPrimaryItem.setMenuVisibility(false);
                currentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            currentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (currentTransaction != null) {
            currentTransaction.commitAllowingStateLoss();
            currentTransaction = null;
            fragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null) {
                View v = fragment.getView();
                if (v != null && v == view && makeFragmentName(((IdentifiedItem) object).id).equals(fragment.getTag())) {
                    return true;
                }
            }
        }
        return false;
    }

    private long getItemId(int position) {
        if (getCount() > position) {
            return getItemWithId(position).id;
        }
        return -1;
    }

    private static String makeFragmentName(long id) {
        return "android:switcher:" + id;
    }

}
