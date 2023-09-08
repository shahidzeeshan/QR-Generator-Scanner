package com.shockwavegames.qr.generator.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AppListFragment extends Fragment {

    Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        context=getContext();
        try {
            getallapps(view);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return view;
    }



    public void getallapps(View view) throws PackageManager.NameNotFoundException {

        ArrayList<String> names=new ArrayList<>();
        ArrayList<String> packageNames=new ArrayList<>();
        ArrayList<Integer> icon  =new ArrayList<>();



        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
//            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
//                // This is a system app, it cannot be uninstalled by the user.
////            } else {
//            if(!packageInfo.packageName.toLowerCase().contains("com.android")&&!packageInfo.packageName.toLowerCase().contains("com.google.android")
//            &&!packageInfo.packageName.toLowerCase().contains("android")) {
                names.add(packageInfo.loadLabel(pm).toString());
                try {
                    Drawable d = getResources().getDrawable(packageInfo.icon);
                    icon.add(packageInfo.icon);
                    // my_image exists
                } catch (Resources.NotFoundException e) {
                    // my_image does not exist
                    icon.add(R.drawable.rainbow);
                }

                packageNames.add(packageInfo.packageName);
                // This is a user app and can be uninstalled by the user.
//            }
//            }
        }

        ListView listView=view.findViewById(R.id.listApps);
        Object[] nameobjArr= names.toArray();
        Object[] packageNameObj= packageNames.toArray();
        String[] nameArr=new String[nameobjArr.length];
        String[] packageNameArr=new String[nameobjArr.length];

        int[] iconArr= new int[icon.size()];
        for (int i = 0; i < icon.size(); i++) {
            iconArr[i] = icon.get(i);
            nameArr[i]=(String)nameobjArr[i];
            packageNameArr[i]=(String)packageNameObj[i];
        }
        CustomAdapter customAdapter=new CustomAdapter(context,nameArr,iconArr);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String qrContent="https://play.google.com/store/apps/details?id=".concat(packageNameArr[position]);//link here
                String qrType="Apps";
                ((MainActivity)getActivity()).ChangeFragment("resultFragment",new String[]{qrContent,qrType});
            }
        });
    }



}