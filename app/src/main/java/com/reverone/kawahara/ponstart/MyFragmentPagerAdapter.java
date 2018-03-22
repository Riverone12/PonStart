package com.reverone.kawahara.ponstart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.reverone.kawahara.ponstart.common.MyFragmentPagerAdapterBase;

/*
 * スワイプでページを切り替える仕組み
 * Created by kawahara on 2017/05/17.
 */

class MyFragmentPagerAdapter extends MyFragmentPagerAdapterBase<PageItem> {
    MyFragmentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public Fragment getFragment(PageItem item, int position) {
        Fragment fragment = null;
        if (item.contentType == PageItem.ContentType.SOUND_EFFECT) {
            fragment = new PlaySoundEffectFragment();
        }
        else if (item.contentType == PageItem.ContentType.SONG) {
            fragment = new PlaySongFragment();
        }
        if (fragment != null) {
            Bundle arg = new Bundle();
            arg.putInt("page", item.page);
            arg.putInt("color", item.color);
            fragment.setArguments(arg);
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return (getItem(position).title);
    }
}

class PageItem {
    enum ContentType {
        SOUND_EFFECT,
        SONG
    }

    ContentType contentType;
    int page;
    int color;
    String title;
}

