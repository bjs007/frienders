package com.frienders.main.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.frienders.main.R;
import com.frienders.main.Search.GroupSearchFragment;
import com.frienders.main.fragment.GinfoxGroupsFragment;
import com.frienders.main.fragment.SubscribedGroupsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    Context context;
    public TabsAccessorAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                return new SubscribedGroupsFragment();
            case 1:
                GinfoxGroupsFragment groupsFragment = new GinfoxGroupsFragment();
                return groupsFragment;
            case 2:
                return new GroupSearchFragment();

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int i) {
        switch (i)
        {
            case 0:
                return context.getString(R.string.mygrouptab);
            case 1:

                return context.getString(R.string.allgrouptab);
            case 2:

                return context.getString(R.string.search);


            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
