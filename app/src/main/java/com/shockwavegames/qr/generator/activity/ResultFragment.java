package com.shockwavegames.qr.generator.activity;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;


import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.UPCEANReader;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ResultFragment extends Fragment {

    View view;
    Context context;
    Bitmap bitmap;
    FloatingActionButton shareBtn, searchWebBtn, copyToClipboardBtn,saveToGallery;
    boolean is2d=false;
    ResultFragment(){
    }
    ResultFragment(boolean is2d){
        this.is2d=is2d;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate((!is2d)?R.layout.fragment_result:R.layout.fragment_result_two, container, false);
        context=getContext();
        InitVar();
        ShowCodeResult();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ShowCodeResult();
        }else {
            CleanUI();
            if(!is2d) {
                LinearLayout.LayoutParams paramsHeading;
                paramsHeading = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                paramsHeading.weight = 1.8f;
                view.findViewById(R.id.infoContainerCV).setLayoutParams(paramsHeading);
                paramsHeading.weight = 2.4f;
                view.findViewById(R.id.nativeAdSpaceResult).setLayoutParams(paramsHeading);
                paramsHeading.weight = 0.5f;
                view.findViewById(R.id.resultButtonContainer).setLayoutParams(paramsHeading);
            }
        }
    }

    void InitVar() {
        shareBtn = view.findViewById(R.id.shareButton);
        saveToGallery=view.findViewById(R.id.saveToGallery);
//        searchWebBtn = view.findViewById(R.id.searchWeb);
        copyToClipboardBtn = view.findViewById(R.id.copyToClipboard);
        AddListeners();

    }
    void AddListeners(){
        saveToGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToGallery(bitmap);
            }
        });
        copyToClipboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrText = getArguments().getString("qrText");
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", qrText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_LONG).show();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String body = getArguments().getString("qrType");
                String sub = getArguments().getString("qrText");
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.putExtra(Intent.EXTRA_TEXT, sub);
                startActivity(Intent.createChooser(intent, "Share using"));
            }
        });
        //        searchWebBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                  "https://www.google.com/search?q="
//            }
//        });
    }

    void ShowCodeResult() {
        CreateDynamicInfo();//populate UI
        String qrText = getArguments().getString("qrText");

        BarcodeFormat tempBarcode = BarcodeFormat.QR_CODE;

        ImageView imageView = view.findViewById(R.id.qrResultImg);

        switch (getArguments().getString("qrType")) {
            case "Product":
            case "EAN13":
            case "ISBN":
                tempBarcode=BarcodeFormat.EAN_13;
                break;
            case "EAN8":
                tempBarcode=BarcodeFormat.EAN_8;
                break;
            case "ITF":
                tempBarcode=BarcodeFormat.ITF;
                break;
            case "Data Matrix":
                tempBarcode=BarcodeFormat.DATA_MATRIX;
                break;
            case "PDF 417":
                tempBarcode=BarcodeFormat.PDF_417;
                break;
            case "Aztec":
                tempBarcode=BarcodeFormat.AZTEC;
                break;
            case "UPC E":
                tempBarcode=BarcodeFormat.UPC_E;
                break;
            case "UPC A":
                tempBarcode=BarcodeFormat.UPC_A;
                break;
            case "Code 128":
                tempBarcode=BarcodeFormat.CODE_128;
                break;
            case "Code 93":
                tempBarcode=BarcodeFormat.CODE_93;
                break;
            case "Code 39":
                tempBarcode=BarcodeFormat.CODE_39;
                break;
            case "Codabar":
                tempBarcode=BarcodeFormat.CODABAR;
                break;
        }

        BarcodeFormat barcodeFormat = tempBarcode;
        String finalQrText = qrText;
        imageView.post(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = view.findViewById(R.id.qrResultImg);

                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {


                    int leftPadding=50,rightPadding=50,topPadding=20,bottomPadding=20;

                    int width = imageView.getWidth();
                    int height = imageView.getHeight();



                    BitMatrix bitMatrix = multiFormatWriter.encode(finalQrText, barcodeFormat, width, height);
                    Bitmap originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    int btWidth=bitMatrix.getWidth();
                    int btHeight=bitMatrix.getHeight();

                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if(i<btWidth&&j<btHeight) {
                                originalBitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    }
                    width += leftPadding + rightPadding;
                    height += topPadding + bottomPadding;

                    Bitmap paddedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(paddedBitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(originalBitmap, leftPadding, topPadding, null);

                    imageView.setImageBitmap(paddedBitmap);


                }
                catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
        ((MainActivity) getActivity()).HideKeyboard();
    }

    @SuppressLint("ResourceType")
    void CreateDynamicInfo() {
        String qrType=getArguments().getString("qrType");

        ((TextView)view.findViewById(R.id.codeTypeTV)).setText(qrType);
        LinearLayout resultLL = view.findViewById(R.id.resultLl);
        String[] fields = null;
        JSONObject jsonObject = null;
        try {
//            LinearLayout.LayoutParams infoContainerParam=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
//            CardView infoContainerCV=view.findViewById(R.id.infoContainerCV);
            LinearLayout linearLayout;
            LinearLayout.LayoutParams paramsLayout;
            TextView heading;
            LinearLayout.LayoutParams paramsHeading;


            switch (qrType) {
                case "Product":
                case "EAN13":
                case "EAN8":
                case "ISBN":
                case "ITF":
                case "Data Matrix":
                case "PDF 417":
                case "Aztec":
                case "UPC E":
                case "UPC A":
                case "Code 128":
                case "Code 93":
                case "Code 39":
                case "Codabar":
                    String headingTxtPr = " Content:";//heading
                    if(qrType.equals("EAN13") && getArguments().getString("qrText").length()==12) {
                        ((TextView)view.findViewById(R.id.codeTypeTV)).setText("EAN 13 (Product)");
                    }

                    linearLayout = new LinearLayout(context);
                    paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout.setId(1200);
                    paramsLayout.setMargins(0,100,0,10);
                    linearLayout.setLayoutParams(paramsLayout);



                    heading = new TextView(context);
                    paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsHeading.weight = 1;
                    heading.setGravity(Gravity.END);
                    heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    heading.setText(headingTxtPr);
                    heading.setLayoutParams(paramsHeading);
                    linearLayout.addView(heading);


                    heading = new TextView(context);
                    paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsHeading.setMargins(30, 0, 0, 0);
                    paramsHeading.weight = 2f;

                    heading.setText(getArguments().getString("qrText"));

                    heading.setLayoutParams(paramsHeading);

                    linearLayout.addView(heading);
                    resultLL.addView(linearLayout);
                    break;
                case "Event":
                    jsonObject = new JSONObject(getArguments().getString("resultData"));
                    fields = new String[]{"Title","Description","Start Date","End Date"};

                    paramsHeading = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    paramsHeading.weight = 4;
                    view.findViewById(R.id.infoContainerCV).setLayoutParams(paramsHeading);
                    paramsHeading.weight=2.2f;
                    view.findViewById(R.id.nativeAdSpaceResult).setLayoutParams(paramsHeading);
                    for(int i=0;i< fields.length;i++) {
                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,0,0,10);
                        linearLayout.setLayoutParams(paramsLayout);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);

                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;

                        heading.setText((i==2||i==3)?ConvertDateFormat(jsonObject.getString(fields[i])):jsonObject.getString(fields[i]));
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }
                    break;
                case "Geolocation":
                    jsonObject = new JSONObject(getArguments().getString("resultData"));
                    fields = new String[]{"Latitude","Longitude"};
                    for(int i=0;i< fields.length;i++) {

                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,(i==0)?30:10,0,10);
                        linearLayout.setLayoutParams(paramsLayout);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);
                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;

                        heading.setText(jsonObject.getString(fields[i]));
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }
                    break;
                case "Email":
                    jsonObject = new JSONObject(getArguments().getString("resultData"));
                    fields = new String[]{"Email","Subject","Body"};

                    for(int i=0;i< fields.length;i++) {

                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,(i==0)?30:10,0,10);
                        linearLayout.setLayoutParams(paramsLayout);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);
                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;

                        heading.setText(jsonObject.getString(fields[i]));
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }
                    break;
                case "vCard":
                    jsonObject = new JSONObject(getArguments().getString("resultData"));
                    fields = new String[]{"Name","Company","Title","Address","Telephone","Email","URL"};

                    paramsHeading = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    paramsHeading.weight = 4;
                    view.findViewById(R.id.infoContainerCV).setLayoutParams(paramsHeading);
                    paramsHeading.weight=2.2f;
                    view.findViewById(R.id.nativeAdSpaceResult).setLayoutParams(paramsHeading);
                    for(int i=0;i< fields.length;i++) {

                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,0,0,10);
                        linearLayout.setLayoutParams(paramsLayout);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);
                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;
                        if(i==6) {
                            String st=jsonObject.getString(fields[i]).trim();
                            if(!st.isEmpty()&&!st.contains("http")){
                                st="https://".concat(st);
                            }
                            heading.setText(st);
                        }else {
                            heading.setText(jsonObject.getString(fields[i]));
                        }
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }

                    break;
                case "SMS"://2 row elements
                    jsonObject = new JSONObject(getArguments().getString("resultData"));
                    fields = new String[]{"Recipient", "Content"};

                    for(int i=0;i<fields.length;i++) {

                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,100,0,10);
                        linearLayout.setLayoutParams(paramsLayout);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);
                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;
                        heading.setText(jsonObject.getString(fields[i]));
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }
                    break;
                case  "Clipboard"://one row elements
                case  "Text":
                case "Website":
                case "Contact":
                case "Apps":
                    String headingTxt = "";
                    switch (getArguments().getString("qrType")) {
                        case "Contact":
                            headingTxt = "Contact:";
                            break;
                        case "Clipboard":
                            headingTxt = "Clipboard Text:";
                            break;
                        case "Text":
                            headingTxt = "Text:";
                     break;
                        case "Website":
                            headingTxt = "Website:";
                            break;
                        case "Apps":
                            headingTxt="App:";
                            break;
                    }

                    linearLayout = new LinearLayout(context);
                    paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout.setId(1200);
                    paramsLayout.setMargins(0,100,0,10);
                    linearLayout.setLayoutParams(paramsLayout);



                    heading = new TextView(context);
                    paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsHeading.weight = 1;
                    heading.setGravity(Gravity.END);
                    heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    heading.setText(headingTxt);
                    heading.setLayoutParams(paramsHeading);
                    linearLayout.addView(heading);


                    heading = new TextView(context);
                    paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsHeading.setMargins(30, 0, 0, 0);
                    paramsHeading.weight = 2f;
                    switch (getArguments().getString("qrType")){
                        case "Contact":
                            heading.setText(getArguments().getString("qrText").replace("tel:",""));
                            break;
                        default:
                            heading.setText(getArguments().getString("qrText"));
                            break;
                    }

                    heading.setLayoutParams(paramsHeading);

                    linearLayout.addView(heading);
                    resultLL.addView(linearLayout);
                    break;
                case "WiFi":

                    fields = new String[]{"SSID", "Password", "Security Type"};
                    jsonObject = new JSONObject(getArguments().getString("resultData"));

                    for (int i = 0; i < 3; i++) {


                        linearLayout = new LinearLayout(context);
                        paramsLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setId(1200);
                        paramsLayout.setMargins(0,0,0,10);
                        linearLayout.setLayoutParams(paramsLayout);



                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.weight = 1;
                        heading.setGravity(Gravity.END);
                        heading.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        heading.setText(fields[i].concat(":"));
                        heading.setLayoutParams(paramsHeading);
                        linearLayout.addView(heading);


                        heading = new TextView(context);
                        paramsHeading = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsHeading.setMargins(30, 0, 0, 0);
                        paramsHeading.weight = 2f;
                        heading.setText(jsonObject.getString(fields[i]));
                        heading.setLayoutParams(paramsHeading);

                        linearLayout.addView(heading);
                        resultLL.addView(linearLayout);
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void CleanUI(){
        LinearLayout resultLL = view.findViewById(R.id.resultLl);
        resultLL.removeAllViewsInLayout();
    }
    //save bitmap to gallery


    private void saveImageToGallery(Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentValues values = contentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, context.getContentResolver().openOutputStream(uri));
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    context.getContentResolver().update(uri, values, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));

            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".png";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    private ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }
    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(context, "Image Saved To Photos", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    String ConvertDateFormat(String inputDate) {
// Create a SimpleDateFormat object with the input date format
        inputDate=inputDate.trim();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

// Parse the input date string into a Date object
        try {
            Date date = inputFormat.parse(inputDate);

// Create a SimpleDateFormat object with the output date format
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
// Format the date object into the output date string

            assert date != null;
            return outputFormat.format(date);
        }catch (ParseException e){
            return inputDate;
//            e.printStackTrace();
        }
    }
}