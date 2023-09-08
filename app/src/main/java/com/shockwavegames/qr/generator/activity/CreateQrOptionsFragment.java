package com.shockwavegames.qr.generator.activity;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipboardManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CreateQrOptionsFragment extends Fragment {

    View view;
    CardView copyFromClipboardCV;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_create_qr_options, container, false);

//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Create QR");
//        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        InitVar();

        copyFromClipboardCV.post(new Runnable() {
            @Override
            public void run() {
                AttachListeners();//waiting for UI to render
            }
        });
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Create QR");
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        }
    }
    void InitVar() {
        copyFromClipboardCV = view.findViewById(R.id.copyFromClipboardContainer);
    }
    void AttachListeners() {
        GridLayout container=view.findViewById(R.id.buttonContainer);
        int childCount=container.getChildCount();


        for(int i=0;i<childCount;i++) {
         int  id = container.getChildAt(i).getId();
            view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateQR(getResources().getResourceEntryName(id));
                }
            });
        }


    }

    void CreateQR(String id){
        switch (id) {
            case "smsContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","SMS"});
                break;
            case "websiteContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Website"});
                break;
            case "copyFromClipboardContainer":
                ClipboardManager clipboardManager=(ClipboardManager)getContext().getSystemService(CLIPBOARD_SERVICE);
                String clipboardTxt=(String) clipboardManager.getPrimaryClip().getItemAt(0).getText();
                if(!clipboardTxt.isEmpty()) {
                    ((MainActivity) getActivity()).ChangeFragment("resultFragment", new String[]{clipboardTxt, "Clipboard"});


                    QRData qrData = new QRData();
                    qrData.qrType = "Clipboard";
                    qrData.historyType=HistoryType.createHistory.toString();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    qrData.creationTime=dateFormat.format(new Date());
                    //icon function here
                    ((MainActivity)getActivity()).creationQueueToAdd.add(0,new AdapterData(qrData.qrType,clipboardTxt, qrData.creationTime, R.drawable.rainbow));

                    qrData.content=clipboardTxt;
                    ((MainActivity)getActivity()).InsertHistoryData(qrData);
                }else {
                    Toast.makeText(getContext(), "Clipboard Is Empty", Toast.LENGTH_SHORT).show();
                }
                break;
            case "textContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Text"});
                break;
            case "wifiContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","WiFi"});
                break;
            case "contactContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Contact"});
                break;
            case "vCardContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","vCard"});
                break;
            case "emailContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Email"});
                break;
            case "geoContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Geolocation"});
                break;
            case "appsContainer":
//                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Apps"});
                ((MainActivity)getActivity()).ChangeFragment("appListFragment");
                break;
            case "eventContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Event"});
                break;
            case "productContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Product"});
                break;
            case "isbnContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","ISBN"});
                break;
            case "ean13Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","EAN13"});
                break;
            case "ean8Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","EAN8"});
                break;
            case "itfContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","ITF"});
                break;
            case "pdf417Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","PDF 417"});
                break;
            case "dataMatrixContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Data Matrix"});
                break;
            case "aztecContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Aztec"});
                break;
            case "upcaContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","UPC A"});
                break;
            case "upceContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","UPC E"});
                break;
            case "code128Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Code 128"});
                break;
            case "code93Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Code 93"});
                break;
            case "code39Container":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Code 39"});
                break;
            case "codabarContainer":
                ((MainActivity)getActivity()).ChangeFragment("createQrFragment",new String[]{"","Codabar"});
                break;

        }
    }
}