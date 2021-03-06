package com.jasonduan.android.locationsaver;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for tab pager
 */
public class TabPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private FragmentManager fragmentManager;
    private List<TabPagerItem> items = new ArrayList<>();
    Fragment currentFragments[] = new Fragment[2];

    public TabPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        fragmentManager = manager;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case Constants.LOCATION_FRAGMENT_POSITION:
                return new LocationFragment();
            case Constants.LIST_FRAGMENT_POSITION:
                return new ListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TabPagerItem item = items.get(position);
        return item.title;
    }

    public void add(String title, int icon) {
        items.add(new TabPagerItem(title, icon));
    }

    public TabPagerItem getTabItem(int position) {
        return items.get(position);
    }

    public View getTabView(int position) {
        TabPagerItem item = getTabItem(position);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_tab, null, false);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        if (item.icon != 0) {
            image.setImageResource(item.icon);
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.GONE);
        }

        TextView text = (TextView) view.findViewById(R.id.text);
        if (item.title != null && !item.title.isEmpty()) {
            text.setText(item.title);
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }

        return view;
    }

    public interface TabPagerListener {
        void onPageSelected();
    }
}
