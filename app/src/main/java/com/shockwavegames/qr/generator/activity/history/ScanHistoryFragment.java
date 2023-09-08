package com.shockwavegames.qr.generator.activity.history;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.shockwavegames.qr.generator.activity.AdapterData;
import com.shockwavegames.qr.generator.activity.HistoryCustomAdapter;
import com.shockwavegames.qr.generator.activity.HistoryType;
import com.shockwavegames.qr.generator.activity.MainActivity;
import com.shockwavegames.qr.generator.activity.QRData;
import com.shockwavegames.qr.generator.activity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScanHistoryFragment extends Fragment {

    MainActivity mainActivity;
    HistoryCustomAdapter listViewAdapter;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_scan_history, container, false);
        mainActivity=((MainActivity)getActivity());
        PoplulateList();
        UpdateList();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        UpdateList();
    }
    public  void UpdateList(){

        if (mainActivity.scanQueueToAdd.size()>0) {

            int size=mainActivity.scanQueueToAdd.size();
            AdapterData temp;
            for(int i=size-1;i>=0;i--){
                temp=mainActivity.scanQueueToAdd.get(i);
                listViewAdapter.getData().add(0,temp);
            }
            listViewAdapter.notifyDataSetChanged();
            mainActivity.scanQueueToAdd.clear();
        }
    }
    void PoplulateList(){

        ListView listView=view.findViewById(R.id.listScansScanning);

        List<QRData> qrData=mainActivity.dbData;
        int size=qrData.size();

        List<QRData> onlyScanHistory=new ArrayList<>();
        QRData temp;
        for(int i=0;i<size;i++){
            temp=qrData.get(i);
            if(temp.historyType.equals(HistoryType.scanHistory.toString())){
                onlyScanHistory.add(temp);
            }
        }

        int tempArrSize=onlyScanHistory.size();
        List<AdapterData> adapterData=new ArrayList<>();
        for(int i=0;i<tempArrSize;i++){
            temp=onlyScanHistory.get(i);
            adapterData.add(new AdapterData(temp.qrType,temp.content,temp.creationTime,R.drawable.rainbow));
//icon function here
//            switch (temp.qrType){
//                case "Text":
//                    iconArr[i]=R.drawable.rainbow;
//                    break;
//                default:
//                    iconArr[i]=R.drawable.rainbow;
//                    break;
//            }
        }

        listViewAdapter=new HistoryCustomAdapter(getContext(),adapterData);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String qrContent = "";//link here
                String qrType=listViewAdapter.getData().get(position).qrType;
                JSONObject otherData=new JSONObject();
                switch (qrType){
                    case "Product":
                    case "ISBN":
                    case "EAN13":
                    case "EAN8":
                    case "ITF":
                    case "PDF 417":
                    case "UPC E":
                    case "UPC A":

                        qrContent=listViewAdapter.getData().get(position).content;
                        mainActivity.ChangeFragment("resultFragment2D",new String[]{qrContent,qrType,otherData.toString()});
                        break;
                    default://on empty fields i'll put a * instead
                        String[] temp=listViewAdapter.getData().get(position).content.split("\n~!");
                        switch (qrType){
                            case "Contact":
                            case "Website":
                            case "Text":
                            case "Clipboard":
                            case "Apps":
                                qrContent=listViewAdapter.getData().get(position).content;
                                break;
                            case "Event":
                                try {
                                    otherData.put("Title", temp[0]);
                                    otherData.put("Description", temp[1]);
                                    otherData.put("Start Date", temp[2]);
                                    otherData.put("End Date", temp[3]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                qrContent = String.format("BEGIN:VEVENT\n" +
                                        "SUMMARY:%s\n" +
                                        "DESCRIPTION:%s\n" +
                                        "DTSTART::%s\n" +
                                        "DTEND:%s\n" +
                                        "END:VEVENT", temp[0], temp[1], temp[2], temp[3]
                                );
                                break;
                            case "Geolocation":
                                qrContent=String.format("geo:%s,%s",temp[0],temp[1]);
                                try {
                                    otherData.put("Latitude",temp[0]);
                                    otherData.put("Longitude",temp[1]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "Email":
                                qrContent = String.format("MATMSG:TO:%s;SUB:%s;BODY:%s;;", temp[0],temp[1],temp[2]);
                                try {
                                    otherData.put("Email",temp[0]);
                                    otherData.put("Subject",temp[1]);
                                    otherData.put("Body",temp[2]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "vCard":
                                String[] nameSplit=temp[0].split(" ");
                                String firstName,lastName;
                                if(nameSplit.length==2){
                                    firstName=nameSplit[0];
                                    lastName=nameSplit[1];
                                }else {
                                    firstName=temp[0];
                                    lastName="";
                                }
                                nameSplit=temp[3].split(",");
                                String street,city="",country="";
                                if(nameSplit.length==3){
                                    street=nameSplit[0];
                                    city=nameSplit[1];
                                    country=nameSplit[2];
                                }else {
                                    street=temp[3];
                                }
                                qrContent=String.format("BEGIN:VCARD\n" +
                                        "VERSION:3.0\n" +
                                        "N:%s;%s;;;\n" +
                                        "FN:%s %s\n" +
                                        "ORG:%s\n" +
                                        "TITLE:%s\n" +
                                        "AD:;;%s;%s;;;%s\n" +//Street,city,state,zipcode,country
                                        "TEL;CELL:%s\n" +
                                        "EMAIL;WORK;INTERNET:%s\n" +
                                        "URL:%s\n" +
                                        "END:VCARD", lastName, firstName, firstName, lastName, temp[1], temp[2], street,city,country, temp[4], temp[5], temp[6]
                                );
                                try {
                                    otherData.put("Name",temp[0]);
                                    otherData.put("Company",temp[1]);
                                    otherData.put("Title",temp[2]);
                                    otherData.put("Address",temp[3]);
                                    otherData.put("Telephone",temp[4]);
                                    otherData.put("Email",temp[5]);
                                    otherData.put("URL",temp[6]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "SMS":
                                qrContent=String.format("smsto:%s:%s",temp[0],temp[1]);

                                try {
                                    otherData.put("Recipient",temp[0]);
                                    otherData.put("Content",temp[1]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "WiFi":
                                qrContent=String.format("WIFI:S:%s;T:%s;P:%s;;",temp[0],temp[1],temp[2]);

                                try {
                                    otherData.put("SSID",temp[0]);
                                    otherData.put("Password",temp[1]);
                                    otherData.put("Security Type",temp[2]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                        }
                        mainActivity.ChangeFragment("resultFragment",new String[]{qrContent,qrType,otherData.toString()});
                        break;
                }

                boolean isDarkTheme = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
                if (isDarkTheme) {
                    // The device is currently using the dark theme
                    listView.getChildAt(position).setBackgroundColor(ContextCompat.getColor(getContext(),android.R.color.background_dark));
                } else {
                    listView.getChildAt(position).setBackgroundColor(ContextCompat.getColor(getContext(),android.R.color.background_light));
                    // The device is currently using the light theme
                }
            }
        });
    }
}