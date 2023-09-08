package com.shockwavegames.qr.generator.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;


import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScanQrFragment extends Fragment {

    private CodeScanner mCodeScanner;
    ResultFragment resultFragment=new ResultFragment();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity activity = getActivity();
        View root=inflater.inflate(R.layout.fragment_scan_qr, container, false);
        // Inflate the layout for this fragment

        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);

        ImageButton flashButton = root.findViewById(R.id.flash_button);
        flashButton.setImageResource(R.drawable.ic_code_scanner_auto_focus_off);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                ImageView imageView = root.findViewById(R.id.image_view);
//                ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "translationY", 0, 250, 0);
//                animator.setInterpolator(new LinearInterpolator());
//                animator.setDuration(2000);
//                animator.setRepeatCount(ValueAnimator.INFINITE);
//                animator.start();




                boolean changeToState=!mCodeScanner.isFlashEnabled();
                mCodeScanner.setFlashEnabled(changeToState);

                if(changeToState){
                    flashButton.setImageResource(R.drawable.insta);
                    //enabled icon
                }else {
                    flashButton.setImageResource(R.drawable.ic_code_scanner_auto_focus_off);
                }
            }
        });

        mCodeScanner.setTouchFocusEnabled(false);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setAutoFocusInterval(1);


        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ScanFunction(result);
                    }
                });
            }
        });
        return root;
    }

    void InsertIntoDB(String qrType, String content){
        QRData qrData = new QRData();
        qrData.qrType = qrType;
        qrData.historyType=HistoryType.scanHistory.toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        qrData.creationTime=dateFormat.format(new Date());
        //icon function here
        ((MainActivity)getActivity()).scanQueueToAdd.add(0,new AdapterData(qrType,content, qrData.creationTime, R.drawable.rainbow));
        qrData.content=content;
        ((MainActivity)getActivity()).InsertHistoryData(qrData);
    }
    void ScanFunction(@NonNull Result result){

        String qrType = "Text";
        JSONObject jsonObject = new JSONObject();
        String resultTextLowerCase = result.getText().toLowerCase();
        String resultTextNormal = result.getText();
        BarcodeFormat barcodeFormat = result.getBarcodeFormat();
        String dbEntryData="";

        if(barcodeFormat.equals(BarcodeFormat.QR_CODE)) {
            if (Pattern.compile("WIFI:S:(.*?);T:(.*?);P:(.*)(?=;|$)").matcher(resultTextNormal).matches()||Pattern.compile("WIFI:T:(.*?);S:(.*?);P:(.*)(?=;|$)").matcher(resultTextNormal).matches()) {
                qrType = "WiFi";
                String regex;
                int regexType=1;
                String wifiString1 = "WIFI:S:Ndsfdkjf/;&^%$$#*();T:WPA / WPA2;P:123;!@#$%^&;";
                if(Pattern.compile("WIFI:S:(.*?);T:(.*?);P:(.*)(?=;|$)").matcher(wifiString1).matches()){
                    regex="WIFI:S:(.*?);T:(.*?);P:(.*)(?=;|$)";
                }else {
                    regex = "WIFI:T:(.*?);S:(.*?);P:(.*)(?=;|$)";
                    regexType = 2;
                }

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(wifiString1);
                String ssid="",password="",encryptionType="";
                if (matcher.find()) {
                    if(regexType==2) {
                        encryptionType = matcher.group(1); // "WPA / WPA2"
                        ssid = matcher.group(2); // "Ndsfdkjf/;&^%$$#*()"
                        password = matcher.group(3); // "123;!@#$%^&"
                    }else {
                        encryptionType = matcher.group(2); // "WPA / WPA2"
                        ssid = matcher.group(1); // "Ndsfdkjf/;&^%$$#*()"
                        password = matcher.group(3); // "123;!@#$%^&"
                    }
                    // Do something with the extracted fields
                }


                dbEntryData=ssid.concat("\n~! ").concat(password).concat("\n~! ").concat(encryptionType);
                try {
                    jsonObject.put("SSID", ssid);
                    jsonObject.put("Password", password);
                    jsonObject.put("Security Type", encryptionType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (Pattern.compile("^smsto:(\\+?\\d{1,15}):(.*)$").matcher(resultTextNormal).matches()) {
                qrType = "SMS";
                Pattern pattern = Pattern.compile("^smsto:(\\+?\\d{1,15}):(.*)$");
                Matcher matcher = pattern.matcher(result.getText());
                String recipient = "", content = "";
                if (matcher.find()) {
                    recipient = matcher.group(1);
                    content = matcher.group(2);
                }

                dbEntryData=recipient.concat("\n~! ").concat(content);
                try {
                    jsonObject.put("Recipient", recipient);
                    jsonObject.put("Content", content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (Pattern.compile("tel:[\\d\\s+-]+").matcher(resultTextNormal).matches()) {
                qrType = "Contact";
                dbEntryData=resultTextNormal;
            }else if ((resultTextLowerCase.contains("begin:vcard") && resultTextLowerCase.contains("end:vcard"))||resultTextLowerCase.contains("mecard:")) {
                qrType = "vCard";
                boolean isMeCard=false;
                if(resultTextLowerCase.contains("mecard:")) {
                    isMeCard = true;
                }

                try {
                    Pattern p;
                    String requiredString = "";
                    if(!isMeCard) {
                        p = Pattern.compile("FN:([^;]*);([^\\n]*)");
                    }else {
                        p = Pattern.compile("N:(.*?);");
                    }
                    Matcher m = p.matcher(resultTextNormal);
                    if (m.find()) {
                        if(!isMeCard) {
                            requiredString = m.group(1).concat(" ").concat(m.group(2));
                        }else {
                            requiredString = m.group(1);
                        }
                    }
                    jsonObject.put("Name", requiredString);
                    dbEntryData=requiredString;

                    requiredString = "";
                    if(!isMeCard) {
                        p = Pattern.compile("ORG:(.*)\n");
                    }else {
                        p = Pattern.compile("ORG:(.*?);");
                    }
                    m = p.matcher(resultTextNormal);
                    if (m.find()) {//no case coz both are using group 1 in this case
                        requiredString = m.group(1);
                    }
                    jsonObject.put("Company", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);

                    requiredString = "";
                    p = Pattern.compile("TITLE:(.*)\n");
                    m = p.matcher(resultTextNormal);
                    if (m.find()) {
                        requiredString = m.group(1);
                    }
                    jsonObject.put("Title", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);

                    requiredString = "";
                    if(!isMeCard) {
                        p = Pattern.compile("ADR:(.*)");
                    }else {
                        p = Pattern.compile("ADR:(.*?);");
                    }
                    m = p.matcher(resultTextNormal);
                    if (m.find()) {
                        requiredString = m.group(1);
                        requiredString = requiredString.replace(";;;", ",").replace(";;", ",").replace(";", ", ").replaceFirst("^,(?!$)", "");

                    }
                    jsonObject.put("Address", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);

                    requiredString = "";
                    if(!isMeCard) {
                        p = Pattern.compile("TEL;(CELL|cell):([+]?[0-9-]+)");
                    }else {
                        p = Pattern.compile("TEL:(.*?);");
                    }
                    m = p.matcher(resultTextNormal);
                    if(!isMeCard) {
                        if (m.find() && m.groupCount() == 2) {
                            requiredString = m.group(2);
                        } else {
                            p = Pattern.compile("TEL;(work|WORK|home|HOME);(voice|VOICE):([+]?[0-9-]+)");
                            m = p.matcher(resultTextNormal);
                            if (m.find() && m.groupCount() >= 3) {
                                requiredString = m.group(3);
                            }
                        }
                    }else {
                        if (m.find()) {
                            requiredString = m.group(1);
                        }
                    }
                    jsonObject.put("Telephone", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);


                    requiredString = "";
                    if(!isMeCard) {
                        p = Pattern.compile("EMAIL;(WORK|work|home|HOME);INTERNET:(.*)\n");
                    }else {
                        p = Pattern.compile("EMAIL:(.*?);");
                    }
                    m = p.matcher(resultTextNormal);
                    if (m.find()) {
                        if(!isMeCard) {
                            requiredString = m.group(2);
                        }else {
                            requiredString = m.group(1);
                        }
                    }
                    jsonObject.put("Email", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);

                    requiredString = "";
                    p = Pattern.compile("URL:(.*)\n");
                    m = p.matcher(resultTextNormal);
                    if (m.find()) {
                        requiredString = m.group(1);
                    }
                    jsonObject.put("URL", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (
                    Pattern.compile("MATMSG:TO:([^;]*);SUB:([^;]*);BODY:([^;]*);;").matcher(resultTextNormal).matches() ||
                            Pattern.compile("mailto:([^\\?]*)\\?subject=([^&]*)&body=(.*)").matcher(resultTextNormal).matches() ||
                            Pattern.compile("SMTP:([^:]*):([^:]*):(.*)").matcher(resultTextNormal).matches()) {
                qrType = "Email";

                Pattern pattern = null;
                Matcher matcher = null;

                if (resultTextLowerCase.contains("matmsg:to:")) {
                    pattern = Pattern.compile("MATMSG:TO:([^;]*);SUB:([^;]*);BODY:([^;]*);;");
                    matcher = pattern.matcher(resultTextNormal);
                } else if (resultTextLowerCase.contains("mailto:email")) {
                    pattern = Pattern.compile("mailto:([^\\?]*)\\?subject=([^&]*)&body=(.*)");
                    matcher = pattern.matcher(resultTextNormal);
                } else if (resultTextLowerCase.contains("smtp:")) {
                    pattern = Pattern.compile("SMTP:([^:]*):([^:]*):(.*)");
                    matcher = pattern.matcher(resultTextNormal);
                }

                if (matcher != null && matcher.find()) {
                    try {
                        dbEntryData=matcher.group(1).concat("\n~! ").concat(matcher.group(2)).concat("\n~! ").concat(matcher.group(3));
                        jsonObject.put("Email", matcher.group(1));
                        jsonObject.put("Subject", matcher.group(2));
                        jsonObject.put("Body", matcher.group(3));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Pattern.compile("(GEO|geo):([-+]?\\d*\\.\\d+|\\d+),([-+]?\\d*\\.\\d+|\\d+)").matcher(resultTextNormal).matches()) {
                qrType = "Geolocation";
                Pattern pattern = Pattern.compile("(GEO|geo):([-+]?\\d*\\.\\d+|\\d+),([-+]?\\d*\\.\\d+|\\d+)");
                Matcher matcher = pattern.matcher(resultTextNormal);

                if (matcher != null && matcher.find()) {
                    try {
                        dbEntryData=matcher.group(2).concat("\n~! ").concat(matcher.group(3));
                        jsonObject.put("Latitude", matcher.group(2));
                        jsonObject.put("Longitude", matcher.group(3));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultTextLowerCase.contains("play.google.com/store/apps/details?id=")) {
                qrType = "Apps";
                dbEntryData=resultTextNormal;

            } else if (resultTextLowerCase.contains("begin:vevent") && resultTextLowerCase.contains("end:vevent")) {
                qrType = "Event";


                Pattern p = Pattern.compile("SUMMARY:(.*)\n");
                String requiredString = "";
                Matcher m = p.matcher(resultTextNormal);
                if (m.find()) {
                    requiredString = m.group(1);
                }
                try {
                    dbEntryData=requiredString;
                    jsonObject.put("Title", requiredString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                p = Pattern.compile("DESCRIPTION:(.*)\n");
                requiredString = "";
                m = p.matcher(resultTextNormal);
                if (m.find()) {
                    requiredString = m.group(1);
                }
                try {
                    jsonObject.put("Description", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                p = Pattern.compile("DTSTART:(.*)\n");
                requiredString = "";
                m = p.matcher(resultTextNormal);
                if (m.find()) {
                    requiredString = m.group(1);
                }
                try {
                    jsonObject.put("Start Date", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                p = Pattern.compile("DTEND:(.*)\n");
                requiredString = "";
                m = p.matcher(resultTextNormal);
                if (m.find()) {
                    requiredString = m.group(1);
                }
                try {
                    jsonObject.put("End Date", requiredString);
                    dbEntryData=dbEntryData.concat("\n~! ").concat(requiredString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (Pattern.compile("(((http|https)://)(www.))?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)").matcher(resultTextNormal).matches()) {
                Pattern patt = Pattern.compile("(((http|https)://)(www.))?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
                Matcher matcher = patt.matcher(result.getText());
                if (matcher.matches()) {
                    qrType = "Website";
                    dbEntryData=resultTextNormal;
                }
            }else {
                //text case
                dbEntryData=resultTextNormal;
            }
            ((MainActivity) getActivity()).ChangeFragment("resultFragment", new String[]{resultTextNormal, qrType, jsonObject.toString()});
            InsertIntoDB(qrType,dbEntryData);
        }else {//2D barcodes
            switch (barcodeFormat){
                case EAN_13:
                    qrType="EAN13";
                    break;
                case EAN_8:
                    qrType="EAN8";
                    break;
                case ITF:
                    qrType="ITF";
                    break;
                case DATA_MATRIX:
                    qrType="Data Matrix";
                    break;
                case PDF_417:
                    qrType="PDF 417";
                    break;
                case AZTEC:
                    qrType="Aztec";
                    break;
                case  UPC_E:
                    qrType="UPC E";
                    break;
                case UPC_A:
                    qrType="UPC A";
                    break;
                case CODE_128:
                    qrType="Code 128";
                    break;
                case CODE_93:
                    qrType="Code 93";
                    break;
                case CODE_39:
                    qrType="Code 39";
                    break;
                case CODABAR:
                    qrType="Codabar";
                    break;
            }
            if(barcodeFormat.equals(BarcodeFormat.AZTEC)||barcodeFormat.equals(BarcodeFormat.DATA_MATRIX)){
                ((MainActivity) getActivity()).ChangeFragment("resultFragment", new String[]{resultTextNormal, qrType, jsonObject.toString()});
            }else {
                ((MainActivity) getActivity()).ChangeFragment("resultFragment2D", new String[]{resultTextNormal, qrType, jsonObject.toString()});
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            mCodeScanner.releaseResources();
        }else {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            mCodeScanner.startPreview();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).OpenScanner();
        mCodeScanner.startPreview();
        mCodeScanner.setZoom(0);
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


}