package com.shockwavegames.qr.generator.activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.tabs.TabLayout;
import com.shockwavegames.qr.generator.activity.history.CreateHistoryFragment;
import com.shockwavegames.qr.generator.activity.history.ScanHistoryFragment;


public class HistoryFragment extends Fragment {


    View view;
    TabLayout tabLayout;
    ViewPager viewPager;
    CreateHistoryFragment createHistoryFragment;
    ScanHistoryFragment scanHistoryFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_history, container, false);
        AddSubFragments();
        return view;
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        createHistoryFragment.onHiddenChanged(hidden);
        scanHistoryFragment.onHiddenChanged(hidden);
    }
    public void AddSubFragments(){
        tabLayout=view.findViewById(R.id.tabLayout);
        viewPager=view.findViewById(R.id.viewPager);
        ViewPagerAdapter adapter=new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        createHistoryFragment=new CreateHistoryFragment();
        adapter.addFragment(createHistoryFragment,"Create History");
        scanHistoryFragment=new ScanHistoryFragment();
        adapter.addFragment(scanHistoryFragment,"Scan History");
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }


}

