package com.shockwavegames.qr.generator.activity;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;



import  android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.shockwavegames.qr.generator.activity.history.CreateHistoryFragment;
import com.shockwavegames.qr.generator.activity.history.ScanHistoryFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Menu bottomMenu;
    Bundle bundleParams;

    CreateQrFragment createQrFragment;
    ScanQrFragment scanQrFragment;
    HistoryFragment historyFragment;
    ResultFragment resultFragment;
    ResultFragment resultFragment2D;
    CreateQrOptionsFragment createQrOptionsFragment;

    SettingsFragment settingsFragment;
    AppListFragment appListFragment;

    FloatingActionButton scanBtn;
    long backPressedTime;
    Toast backToast;

    //History sqllite stuff

    private AppDatabase appDatabase;
    private UserDao userDao;


    public List<QRData> dbData;
    public List<AdapterData> creationQueueToAdd=new ArrayList<>();
    public List<AdapterData> scanQueueToAdd=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//enablig bak button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();

        InitVar();
        FetchHistoryDataFromDB();
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        ChangeFragment("scanQrFragment");
        return super.onOptionsItemSelected(item);
    }

    void InitVar() {
        createQrFragment = new CreateQrFragment();
        createQrOptionsFragment=new CreateQrOptionsFragment();
        scanQrFragment = new ScanQrFragment();
        resultFragment = new ResultFragment();
        resultFragment2D=new ResultFragment(true);
        historyFragment=new HistoryFragment();
        appListFragment=new AppListFragment();
        settingsFragment=new SettingsFragment();


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        scanBtn=findViewById(R.id.scanBtn);
        bottomNavigationView.setBackground(null);
        bottomMenu=bottomNavigationView.getMenu();
        ToggleMenuCheckable(false);//unselect menuitem
       scanBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               OpenScanner();
               ChangeFragment("scanQrFragment");
               ToggleMenuCheckable(false);
           }
       });
        InitKeyBoardListener();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                ToggleMenuCheckable(true);
                switch (item.getItemId()) {
                    case R.id.create:
                        ChangeFragment("createQrOptionsFragment");
                        return true;
                    case R.id.history:
                        ChangeFragment("historyFragment");
                       return true;
                    case R.id.setting:
                        ChangeFragment("settingsFragment");
                        return true;
                }
                return false;
            }
        });

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my_database").build();
        userDao = appDatabase.userDao();

        ChangeFragment("scanQrFragment");
    }
    public  void ChangeFragment(String fragmentName,String[] param) {
        bundleParams = new Bundle();
        bundleParams.putString("qrText", param[0]);//generalized content
        int len=param.length;
        if(len>=2){
            bundleParams.putString("qrType", param[1]);
        }
        if(len>=3){
            bundleParams.putString("resultData", param[2]);
        }
        ChangeFragment(fragmentName);
    }

    void ChangeFragment(String fragmentName) {

    FragmentManager fragmentManager=getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    List<Fragment> fragments=fragmentManager.getFragments();
    for (Fragment fragment:fragments) {
        if(fragment.isVisible()&&!fragment.getTag().equals(fragmentName)){
            switch (fragment.getTag()){
                case "createQrOptionsFragment":
                    fragmentTransaction.hide(createQrOptionsFragment);
                    break;
                case "createQrFragment":
                    fragmentTransaction.hide(createQrFragment);
                    break;
                case "resultFragment":
                    fragmentTransaction.hide(resultFragment);
                    break;
                case "scanQrFragment":
                    fragmentTransaction.hide(scanQrFragment);
                    break;
                case "historyFragment":
                    fragmentTransaction.hide(historyFragment);
                    break;
                case "appListFragment":
                    fragmentTransaction.hide(appListFragment);
                    break;
                case "resultFragment2D":
                    fragmentTransaction.hide(resultFragment2D);
                    break;
                case "settingsFragment":
                    fragmentTransaction.hide(settingsFragment);
                    break;

            }

        }
    }

    fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);

    switch (fragmentName) {
        case "createQrOptionsFragment":
            if(bundleParams!=null) {
                createQrOptionsFragment.setArguments(bundleParams);
            }
            if (createQrOptionsFragment.isAdded()) {
                fragmentTransaction.show(createQrOptionsFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, createQrOptionsFragment, "createQrOptionsFragment");
            }
            break;
        case "createQrFragment":

            if(bundleParams!=null) {
                createQrFragment.setArguments(bundleParams);
            }
            if (createQrFragment.isAdded()) {
                fragmentTransaction.show(createQrFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, createQrFragment, "createQrFragment");
            }
            break;
        case "scanQrFragment":

            if(bundleParams!=null) {
                scanQrFragment.setArguments(bundleParams);
            }
            if (scanQrFragment.isAdded()) {
                fragmentTransaction.show(scanQrFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, scanQrFragment, "scanQrFragment");
            }
            break;
        case "resultFragment":

            if(bundleParams!=null) {
                resultFragment.setArguments(bundleParams);
            }
            if (resultFragment.isAdded()) {
                fragmentTransaction.show(resultFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, resultFragment, "resultFragment");
            }
            break;
        case "resultFragment2D":
            if(bundleParams!=null) {
                resultFragment2D.setArguments(bundleParams);
            }
            if (resultFragment2D.isAdded()) {
                fragmentTransaction.show(resultFragment2D);
            } else {
                fragmentTransaction.add(R.id.constraint, resultFragment2D, "resultFragment2D");
            }
            break;
        case "historyFragment":

            if(bundleParams!=null) {
                historyFragment.setArguments(bundleParams);
            }
            if (historyFragment.isAdded()) {
                fragmentTransaction.show(historyFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, historyFragment, "historyFragment");
            }
            break;
        case "settingsFragment":
            if(bundleParams!=null) {
                settingsFragment.setArguments(bundleParams);
            }
            if (settingsFragment.isAdded()) {
                fragmentTransaction.show(settingsFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, settingsFragment, "settingsFragment");
            }
            break;
        case "appListFragment":
            if(bundleParams!=null) {
                appListFragment.setArguments(bundleParams);
            }
            if (appListFragment.isAdded()) {
                fragmentTransaction.show(appListFragment);
            } else {
                fragmentTransaction.add(R.id.constraint, appListFragment, "appListFragment");
            }
            break;
    }

    bundleParams=null;
    fragmentTransaction.commit();
}
     void OpenScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
//                    ChangeFragment("createQrOptionsFragment");
                    Bundle bundle=new Bundle();
                    bundle.putString("permissionType","Camera_Permission");
                    Intent intent = new Intent(this, PermissionDialogActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag("scanQrFragment").isHidden()) {
            ChangeFragment("scanQrFragment");
        } else {
            if (System.currentTimeMillis() < backPressedTime + 2000) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                if (backToast != null)
                    backToast.cancel();
                backToast = Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }
     void HideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
         if(view!=null)
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    void ToggleMenuCheckable(Boolean checkable){
        bottomMenu.setGroupCheckable(0,checkable,true);
    }

    private void InitKeyBoardListener() {
        // Threshold for minimal keyboard height.
        final int MIN_KEYBOARD_HEIGHT_PX = 450;
        // Top-level window decor view.
        final View decorView = getWindow().getDecorView();
        // Register global layout listener.
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            // Retrieve visible rectangle inside window.
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        findViewById(R.id.bottomMenu).setVisibility(View.INVISIBLE);
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        findViewById(R.id.bottomMenu).setVisibility(View.VISIBLE);
                    }
                }
                // Save current decor view height for the next call.
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });
    }
    void InsertHistoryData(QRData qrData) {
        // Insert a user into the database
        //Usage
        new InsertTask(qrData).execute();
    }

    void FetchHistoryDataFromDB(){
        new QueryTask().execute();
    }


    private class InsertTask extends AsyncTask<Void, Void, Void> {
        private QRData user;

        InsertTask(QRData user) {
            this.user = user;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userDao.insert(user);
            return null;
        }
    }
    private class QueryTask extends AsyncTask<Void, Void, List<QRData>> {

        @Override
        protected List<QRData> doInBackground(Void... voids) {
            dbData=userDao.getAll();
            return dbData;
        }

//        @Override
//        protected void onPostExecute(List<QRData> users) {
//            for (QRData user : users) {
//                Log.d("MainActivity", "id: " + user.uid + ", createdate: " + user.creationTime);
//            }
//        }
    }
}