package com.frienders.main.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.frienders.main.fragment.ChatsFragment;
import com.frienders.main.fragment.ContactsFragment;
import com.frienders.main.fragment.GinfoxGroupsFragment;
import com.frienders.main.fragment.RequestsFragment;
import com.frienders.main.fragment.SubscribedGroupsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return new SubscribedGroupsFragment();
            case 1:
                GinfoxGroupsFragment groupsFragment = new GinfoxGroupsFragment();
                return groupsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

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
                return "Your Groups";
            case 1:

                return "All Groups";

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
