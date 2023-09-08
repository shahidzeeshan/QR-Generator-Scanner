package com.shockwavegames.qr.generator.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.FormatException;
import com.google.zxing.oned.EAN8Reader;
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEANReader;
import com.google.zxing.oned.UPCEReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateQrFragment extends Fragment {

    View view;
    String wifiNetwork="";
    ActivityResultLauncher<Intent> activityResultLauncherContactRead;
    String pageType="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         view =inflater.inflate(R.layout.fragment_create_qr, container, false);
        PopulateUI(getArguments().getString("qrType"));


        activityResultLauncherContactRead = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressLint({"Range", "ResourceType"})
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                            assert result.getData() != null;
                            Uri contactData=result.getData().getData();

                            Cursor cursor;
                            Cursor cursor2;

                            cursor = getActivity().getContentResolver().query(contactData,null,null,null,null);
//                          cursor= getActivity().managedQuery(contactData,null,null,null,null);//for old version

                            if(cursor.moveToFirst()) {
                                if(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0")&&(pageType.equals("SMS")||pageType.equals("Contact"))){
                                    Toast.makeText(getContext(),  "Contact Does Not Hava A Number", Toast.LENGTH_SHORT).show();
                                    ((EditText) view.findViewById(1000)).setText("");
                                    if(pageType.equals("Contact")){
                                        ((EditText) view.findViewById(1001)).setText("");
                                    }
                                }else {
                                    ((EditText) view.findViewById(1000)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,ContactsContract.CommonDataKinds.Phone.NUMBER));

                                    switch (pageType) {
                                        case "Contact":
                                            ((EditText) view.findViewById(1001)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                            break;
                                        case "vCard":
                                            ((EditText) view.findViewById(1007)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.Email.DATA));
                                            ((EditText) view.findViewById(1001)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));

                                            ((EditText) view.findViewById(1002)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                                            ((EditText) view.findViewById(1008)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.Website.URL));
                                            ((EditText) view.findViewById(1003)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.Organization.COMPANY));
                                            ((EditText) view.findViewById(1004)).setText(FetchvCardData(contactData,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.Organization.TITLE));
                                            EditText address = ((EditText) view.findViewById(1005));
                                            String addComplete=FetchvCardData(contactData,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.StructuredPostal.STREET);
                                            String temp=FetchvCardData(contactData,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.StructuredPostal.CITY);
                                            addComplete.concat((temp!=null)?",".concat(temp):",");
                                            temp=FetchvCardData(contactData,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                                                    ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
                                            addComplete.concat((temp!=null)?",".concat(temp):",");

                                            address.setText(addComplete);
                                            break;
                                    }

                                }
                            }
                        }
                    }
                });
        return view;
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            CleanUI();
            pageType = "";
        }else {
            PopulateUI(getArguments().getString("qrType"));
        }
    }

    @SuppressLint("ResourceType")
    void PopulateUI(String pageType) {
        this.pageType=pageType;
        LinearLayout uiContainer=view.findViewById(R.id.createContainerLl);
        Context context=getContext();

        ImageView logoImg=view.findViewById(R.id.logoIv);
        logoImg.setImageResource(R.drawable.rainbow);//place in switch for each icon

        TextView logoTxt=view.findViewById(R.id.logoTv);


        switch (pageType){
            case "ITF":
            case "Product":
            case "EAN13":
            case "EAN8":
            case "ISBN":
            case "Data Matrix":
            case "PDF 417":
            case "Aztec":
            case "UPC A":
            case "UPC E":
            case "Code 128":
            case "Code 93":
            case "Code 39":
            case "Codabar":
                logoTxt.setText(pageType);
                int wordLimit=12;
                int optional=0;


                EditText textProduct=new EditText(context);
                LinearLayout.LayoutParams paramsProduct=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsProduct.setMargins(30,100,30,10);
                textProduct.setLayoutParams(paramsProduct);


                textProduct.setInputType(InputType.TYPE_CLASS_NUMBER);
                switch (pageType){
                    case "Data Matrix":
                    case "PDF 417":
                    case "Aztec":
                    case "Code 128":
                    case "Code 93":
                    case "Code 39":

                        wordLimit=0;
                        textProduct.setHint("Text");
                        textProduct.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case "Product":
                        textProduct.setHint("Product Code");
                        break;
                    case "ISBN":
                        textProduct.setHint("Code");
                        break;
                    case "EAN13":
                        optional=1;
                        textProduct.setHint("12 Digits +1 Checksum(optional)");
                        break;
                    case "EAN8":
                        wordLimit=8;
                        textProduct.setHint("7 Digits +1 Checksum");
                        break;
                    case "UPC A":
                        wordLimit=11;
                        optional=1;
                        textProduct.setHint("11 Digits +1 Checksum (Optional)");
                        break;
                    case "UPC E":
                        wordLimit=7;
                        optional=1;
                        textProduct.setHint("7 Digits +1 Checksum (Optional)");
                        break;
                    case "ITF":
                    case "Codabar":
                        wordLimit=0;
                        textProduct.setHint("Digits");
                        break;
                    default:
                        textProduct.setHint("12 Digits");
                        break;

                }


                textProduct.setId(1000);
                uiContainer.addView(textProduct);

                InputFilter[] editFiltersProduct=textProduct.getFilters();
                InputFilter[] newFiltersProduct=new InputFilter[editFiltersProduct.length+1];
                System.arraycopy(editFiltersProduct,0,newFiltersProduct,0,editFiltersProduct.length);
                newFiltersProduct[editFiltersProduct.length]=new InputFilter.LengthFilter(wordLimit+optional);
                if(wordLimit!=0) {
                    textProduct.setFilters(newFiltersProduct);
                }

                TextView wordCountProduct=new TextView(context);

                LinearLayout.LayoutParams websiteParamProduct= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                websiteParamProduct.gravity=Gravity.LEFT;
                wordCountProduct.setId(1002);
                wordCountProduct.setText("0/".concat(String.valueOf(wordLimit+optional)));
                websiteParamProduct.setMargins(40,0,0,0);
                wordCountProduct.setLayoutParams(websiteParamProduct);

                int finalOptional = optional;
                int finalWordLimit = wordLimit;
                if(wordLimit!=0) {
                    textProduct.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            wordCountProduct.setText(String.format("%d/".concat(String.valueOf(finalWordLimit + finalOptional)), textProduct.length()));
                        }
                    });
                }
                if(wordLimit!=0) {//don't show if 0 wordlimit
                    uiContainer.addView(wordCountProduct);
                }
                break;
            case "Event":
                RelativeLayout relativeLayoutEvent=new RelativeLayout(context);
                RelativeLayout.LayoutParams paramEvent=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText titleEvent=new EditText(context);
                EditText descriptionEvent=new EditText(context);
                EditText startDate=new EditText(context);
                EditText endDate=new EditText(context);

                startDate.setClickable(false);
                startDate.setCursorVisible(false);
                startDate.setFocusable(false);
                startDate.setFocusableInTouchMode(false);
                endDate.setClickable(false);
                endDate.setCursorVisible(false);
                endDate.setFocusable(false);
                endDate.setFocusableInTouchMode(false);

                titleEvent.setHint("Title");
                descriptionEvent.setHint("Description");
                startDate.setHint("Start Date");
                endDate.setHint("End Date");

                titleEvent.setId(1000);
                descriptionEvent.setId(1001);
                startDate.setId(1002);
                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(1002);
                    }
                });
                endDate.setId(1003);
                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(1003);
                    }
                });


                logoTxt.setText(pageType);

                LinearLayout.LayoutParams paramOthersEvent=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                paramOthersEvent.setMargins(30, 100, 30, 10);

                titleEvent.setLayoutParams(paramOthersEvent);

                relativeLayoutEvent.setLayoutParams(paramEvent);
                relativeLayoutEvent.addView(titleEvent);

                uiContainer.addView(relativeLayoutEvent);

                paramOthersEvent.setMargins(30, 50, 30, 10);

                descriptionEvent.setLayoutParams(paramOthersEvent);
                uiContainer.addView(descriptionEvent);

                startDate.setLayoutParams(paramOthersEvent);
                uiContainer.addView(startDate);
                endDate.setLayoutParams(paramOthersEvent);
                uiContainer.addView(endDate);
                break;
            case "Geolocation":
                RelativeLayout relativeLayoutGeo=new RelativeLayout(context);
                RelativeLayout.LayoutParams paramGeo=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText latitude=new EditText(context);
                EditText longitude=new EditText(context);

                latitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                longitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

                latitude.setHint("Latitude");
                longitude.setHint("Longitude");

                latitude.setId(1000);
                longitude.setId(1001);

                logoTxt.setText(pageType);

                LinearLayout.LayoutParams emailGeo=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                emailGeo.setMargins(30, 100, 30, 10);

                latitude.setLayoutParams(emailGeo);
                relativeLayoutGeo.setLayoutParams(paramGeo);
                relativeLayoutGeo.addView(latitude);
                uiContainer.addView(relativeLayoutGeo);

                emailGeo.setMargins(30, 50, 30, 10);

                longitude.setLayoutParams(emailGeo);

                uiContainer.addView(longitude);
                break;
            case "Email":
                RelativeLayout relativeLayoutEmail=new RelativeLayout(context);
                RelativeLayout.LayoutParams paramEmail=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText emailAddr=new EditText(context);
                EditText emailSubject=new EditText(context);
                EditText emailBody=new EditText(context);

                emailAddr.setHint("Email");
                emailSubject.setHint("Subject");
                emailBody.setHint("Body");

                emailAddr.setId(1000);
                emailSubject.setId(1001);
                emailBody.setId(1002);

                logoTxt.setText(pageType);

                LinearLayout.LayoutParams emailParam=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                emailParam.setMargins(30, 100, 30, 10);

                emailAddr.setLayoutParams(emailParam);
                relativeLayoutEmail.setLayoutParams(paramEmail);
                relativeLayoutEmail.addView(emailAddr);
                uiContainer.addView(relativeLayoutEmail);

                emailParam.setMargins(30, 50, 30, 10);

                emailSubject.setLayoutParams(emailParam);
                emailBody.setLayoutParams(emailParam);

                uiContainer.addView(emailSubject);
                uiContainer.addView(emailBody);

                break;
            case "vCard":
                RelativeLayout relativeLayoutVCard=new RelativeLayout(context);
                RelativeLayout.LayoutParams paramVCard=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText vCardFirstName=new EditText(context);
                EditText vCardLastName=new EditText(context);
                EditText vCardCompany=new EditText(context);
                EditText vCardJobTitle=new EditText(context);
                EditText vCardAddress=new EditText(context);
                EditText vCardPhoneNumber=new EditText(context);
                EditText vCardEmail=new EditText(context);
                EditText vCardWebsite=new EditText(context);

                vCardFirstName.setHint("First Name");
                vCardLastName.setHint("Last Name");
                vCardCompany.setHint("Company");
                vCardJobTitle.setHint("Job Title");
                vCardAddress.setHint("Address");
                vCardPhoneNumber.setHint("Phone Number");
                vCardEmail.setHint("Email");
                vCardWebsite.setHint("Website");

                ImageButton importContactvCardIV=new ImageButton(context);
                importContactvCardIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AccessContacts();
                    }
                });

                vCardPhoneNumber.setId(1000);
                vCardFirstName.setId(1001);
                vCardLastName.setId(1002);
                vCardCompany.setId(1003);
                vCardJobTitle.setId(1004);
                vCardAddress.setId(1005);
                vCardEmail.setId(1007);
                vCardWebsite.setId(1008);

                RelativeLayout.LayoutParams paramvCard2=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramvCard2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,vCardFirstName.getId());
                importContactvCardIV.setImageResource(R.drawable.insta);
                importContactvCardIV.setBackgroundColor(android.R.color.transparent);
                importContactvCardIV.setLayoutParams(paramvCard2);


                logoTxt.setText(pageType);


                LinearLayout.LayoutParams websiteParamvCard=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                websiteParamvCard.setMargins(30, 100, 30, 10);


                //filters to limit length
                InputFilter[] editFiltersVCard=vCardPhoneNumber.getFilters();
                InputFilter[] newFiltersVCard=new InputFilter[editFiltersVCard.length+1];
                System.arraycopy(editFiltersVCard,0,newFiltersVCard,0,editFiltersVCard.length);
                newFiltersVCard[editFiltersVCard.length]=new InputFilter.LengthFilter(15);
                vCardPhoneNumber.setFilters(newFiltersVCard);

                editFiltersVCard=vCardFirstName.getFilters();
                newFiltersVCard=new InputFilter[editFiltersVCard.length+1];
                System.arraycopy(editFiltersVCard,0,newFiltersVCard,0,editFiltersVCard.length);
                newFiltersVCard[editFiltersVCard.length]=new InputFilter.LengthFilter(70);
                vCardFirstName.setFilters(newFiltersVCard);
                vCardLastName.setFilters(newFiltersVCard);



                vCardPhoneNumber.setInputType(InputType.TYPE_CLASS_NUMBER);

                vCardFirstName.setLayoutParams(websiteParamvCard);
                relativeLayoutVCard.setLayoutParams(paramVCard);
                relativeLayoutVCard.addView(vCardFirstName);
                relativeLayoutVCard.addView(importContactvCardIV);
                uiContainer.addView(relativeLayoutVCard);

                websiteParamvCard.setMargins(30, 50, 30, 10);

                vCardLastName.setLayoutParams(websiteParamvCard);
                vCardPhoneNumber.setLayoutParams(websiteParamvCard);
                vCardCompany.setLayoutParams(websiteParamvCard);
                vCardJobTitle.setLayoutParams(websiteParamvCard);
                vCardAddress.setLayoutParams(websiteParamvCard);
                vCardEmail.setLayoutParams(websiteParamvCard);
                vCardWebsite.setLayoutParams(websiteParamvCard);

                //aliging importbutton

                uiContainer.addView(vCardLastName);
                uiContainer.addView(vCardPhoneNumber);
                uiContainer.addView(vCardCompany);
                uiContainer.addView(vCardJobTitle);
                uiContainer.addView(vCardAddress);
                uiContainer.addView(vCardEmail);
                uiContainer.addView(vCardWebsite);

                break;
            case "Contact":
                RelativeLayout relativeLayoutContact=new RelativeLayout(context);
                RelativeLayout.LayoutParams paramContact=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText contactName=new EditText(context);
                EditText contact=new EditText(context);

                contact.setHint("Phone Number");
                contactName.setHint("Name (Optional)");
                ImageButton importContactCIV=new ImageButton(context);
                importContactCIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AccessContacts();
                    }
                });

                contact.setId(1000);

                RelativeLayout.LayoutParams paramContact2=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramContact2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,contact.getId());
                importContactCIV.setImageResource(R.drawable.insta);
                importContactCIV.setBackgroundColor(android.R.color.transparent);
                importContactCIV.setLayoutParams(paramContact2);


                logoTxt.setText(pageType);

                LinearLayout.LayoutParams websiteParamContact=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                websiteParamContact.setMargins(30, 100, 30, 10);


                //filters to limit length
                InputFilter[] editFiltersContact=contact.getFilters();
                InputFilter[] newFiltersContact=new InputFilter[editFiltersContact.length+1];
                System.arraycopy(editFiltersContact,0,newFiltersContact,0,editFiltersContact.length);
                newFiltersContact[editFiltersContact.length]=new InputFilter.LengthFilter(15);
                contact.setFilters(newFiltersContact);

                editFiltersContact=contactName.getFilters();
                newFiltersContact=new InputFilter[editFiltersContact.length+1];
                System.arraycopy(editFiltersContact,0,newFiltersContact,0,editFiltersContact.length);
                newFiltersContact[editFiltersContact.length]=new InputFilter.LengthFilter(70);
                contactName.setFilters(newFiltersContact);



                contact.setInputType(InputType.TYPE_CLASS_NUMBER);
                contactName.setId(1001);
                contact.setLayoutParams(websiteParamContact);
                contactName.setLayoutParams(websiteParamContact);


                relativeLayoutContact.setLayoutParams(paramContact);
                relativeLayoutContact.addView(contactName);
                relativeLayoutContact.addView(importContactCIV);
                uiContainer.addView(relativeLayoutContact);

                uiContainer.addView(contact);
                break;
            case "SMS":
                RelativeLayout relativeLayout=new RelativeLayout(context);
                RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


                EditText recipient=new EditText(context);
                EditText content=new EditText(context);
                TextView wordCount=new TextView(context);
                ImageButton importContactIV=new ImageButton(context);
                importContactIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AccessContacts();
                    }
                });
                recipient.setId(1000);

                RelativeLayout.LayoutParams param1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                param1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,recipient.getId());
                importContactIV.setImageResource(R.drawable.insta);
                importContactIV.setBackgroundColor(android.R.color.transparent);
                importContactIV.setLayoutParams(param1);


                logoTxt.setText(pageType);
                LinearLayout.LayoutParams websiteParam=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                websiteParam.setMargins(30, 100, 30, 10);


                //filters to limit length
                InputFilter[] editFilters=recipient.getFilters();
                InputFilter[] newFilters=new InputFilter[editFilters.length+1];
                System.arraycopy(editFilters,0,newFilters,0,editFilters.length);
                newFilters[editFilters.length]=new InputFilter.LengthFilter(15);
                recipient.setFilters(newFilters);

                editFilters=content.getFilters();
                newFilters=new InputFilter[editFilters.length+1];
                System.arraycopy(editFilters,0,newFilters,0,editFilters.length);
                newFilters[editFilters.length]=new InputFilter.LengthFilter(300);
                content.setFilters(newFilters);
             content.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                 }

                 @Override
                 public void onTextChanged(CharSequence s, int start, int before, int count) {

                 }

                 @Override
                 public void afterTextChanged(Editable s) {
                        wordCount.setText(String.format("%d/300",content.length()));
                 }
             });


                recipient.setInputType(InputType.TYPE_CLASS_NUMBER);
                content.setId(1001);
                recipient.setLayoutParams(websiteParam);

                recipient.setHint("Recipient Number");
                content.setHint("Content");
                websiteParam=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,800);
                content.setLayoutParams(websiteParam);

                websiteParam= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                websiteParam.gravity=Gravity.LEFT;
                wordCount.setId(1002);
                wordCount.setText("0/300");
                websiteParam.setMargins(40,0,0,0);
                wordCount.setLayoutParams(websiteParam);

                relativeLayout.setLayoutParams(param);
                relativeLayout.addView(recipient);
                relativeLayout.addView(importContactIV);
                uiContainer.addView(relativeLayout);
                uiContainer.addView(content);
                uiContainer.addView(wordCount);
                break;

            case "Text"://one row
            case "Website":

                EditText text=new EditText(context);
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(30,100,30,10);
                text.setLayoutParams(params);

                logoTxt.setText(pageType);
                text.setHint(pageType);

                switch (pageType){
                    case "Website":
                        text.setText("https://");
                        break;
                }

                text.setId(1000);
                uiContainer.addView(text);
                break;
            case "WiFi":
                logoTxt.setText("WiFi");
                EditText ssid=new EditText(context);
                EditText password=new EditText(context);
                Spinner dropdown=new Spinner(context);

                LinearLayout.LayoutParams ssidParam=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                ssidParam.setMargins(30,100,30,10);
                ssid.setId(1000);
                password.setId(1001);
                dropdown.setId(1002);
                ssid.setLayoutParams(ssidParam);
                password.setLayoutParams(ssidParam);
                ssid.setHint("SSID");
                password.setHint("Password");

                String[] items=new String[]{"WPA/WPA2/WPA3","WEP"};
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,items);
                dropdown.setAdapter(adapter);
                dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String networkType=parent.getItemAtPosition(position).toString();
                        switch (networkType){
                            case "WPA/WPA2/WPA3":
                                wifiNetwork="WPA";
                                break;
                            case "WEP":
                                wifiNetwork="WEP";
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                uiContainer.addView(ssid);
                uiContainer.addView(password);
                uiContainer.addView(dropdown);
                break;
        }

        //creating button
        ImageButton generateQrBtn=new ImageButton(context);
        generateQrBtn.setId(1100);
        generateQrBtn.setImageResource(R.drawable.insta);
        LinearLayout.LayoutParams paramsCreateBtn=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCreateBtn.gravity=Gravity.RIGHT;
        generateQrBtn.setLayoutParams(paramsCreateBtn);
        generateQrBtn.setPadding(0,20,150,0);
        generateQrBtn.setBackgroundColor(android.R.color.transparent);
        uiContainer.addView(generateQrBtn);

        generateQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject otherData=new JSONObject();
                String dbEntryData="";

                String qrContent="";
                String qrType=pageType;
                //below 2 used for 2d barcodes onlly
                String productSt=((EditText)view.findViewById(1000)).getText().toString();
                int len=productSt.length();

                switch (pageType) {//add for barcode etc cases in which qrtype will change
                    case "Product":
                    case "ISBN":
                        if(len<12){
                            Toast.makeText(context, "Content Length Should Be 12.Your content length is ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;
                        break;
                    case "EAN13":
                        if(len<12||len>13){
                            Toast.makeText(context, "Content Length Should Be 12,13.Your content length is ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (len==13&&!IsCorrectEAN13Checksum(productSt)) {
                            Toast.makeText(context, "Content Does Not Match Checksum", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;
                        break;
                    case "ITF":
                        if(len%2!=0){
                            Toast.makeText(context, "Number Of Digits Should Be EVEN", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(len>=80){
                            Toast.makeText(context, "Content Cannot Be More Than 80 digits Long.But Got ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;
                        break;
                    case "Data Matrix":
                    case "PDF 417":
                    case "Aztec":
                    case "Codabar":
                        if(len>=300) {
                            Toast.makeText(context, "Content Cannot Be More Than 80 digits Long.But Got ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                            return;
                        }else if(!productSt.isEmpty()) {
                            qrContent = productSt;
                        }else {
                            Toast.makeText(context, "Field Cannot Be Empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;

                        break;
                    case "Code 128":
                    case "Code 93":
                    case "Code 39":

                        if(len>=80) {
                        Toast.makeText(context, "Content Cannot Be More Than 80 digits Long.But Got ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                        return;
                    }
                        if(!productSt.isEmpty()) {
                            qrContent = productSt;
                        }else {
                            Toast.makeText(context, "Field Cannot Be Empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;
                        break;
                    case "UPC E":
                        try {
                            if(len<7||len>8){
                                Toast.makeText(context, "Content Length Should Be 7,8.Your content length is ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (len==8&&!IsCorrectUPCEChecksum(productSt)) {
                                Toast.makeText(context, "Content Does Not Match Checksum", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (FormatException e) {
                            e.printStackTrace();
                        }
                        qrContent = productSt;
                        break;
                    case "EAN8":

                        if(len<7||len>8){
                            Toast.makeText(context, "Content Length Should Be 7,8.Your content length is ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (len==8&&!IsCorrectEAN8Checksum(productSt)) {
                            Toast.makeText(context, "Content Does Not Match Checksum", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        qrContent = productSt;
                        break;
                    case "UPC A":
                        try {
                            if(len<11||len>12){
                                Toast.makeText(context, "Content Length Should Be 11,12.Your content length is ".concat(String.valueOf(len)), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (len==12&&!IsCorrectUPCEChecksum(productSt)) {
                                Toast.makeText(context, "Content Does Not Match Checksum", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (FormatException e) {
                            e.printStackTrace();
                        }
                        qrContent = productSt;
                        break;
                    case "Event":
                        String titleEv,description,startDate,endDate;

                        titleEv=((EditText) view.findViewById(1000)).getText().toString().trim();
                        description=((EditText) view.findViewById(1001)).getText().toString().trim();
                        startDate=((EditText) view.findViewById(1002)).getText().toString().trim();
                        endDate=((EditText) view.findViewById(1003)).getText().toString().trim();
                        if(!titleEv.isEmpty()&&!description.isEmpty()&&!startDate.isEmpty()&&!endDate.isEmpty()) {
                            qrContent = String.format("BEGIN:VEVENT\n" +
                                    "SUMMARY:%s\n" +
                                    "DESCRIPTION:%s\n" +
                                    "DTSTART::%s\n" +
                                    "DTEND:%s\n" +
                                    "END:VEVENT", titleEv, description, ConvertDateFormat(startDate), ConvertDateFormat(endDate)
                            );
                            try {
                                String stDate=ConvertDateFormat(startDate);
                                String enDate=ConvertDateFormat(endDate);

                                dbEntryData=titleEv.concat("\n~! ").concat(description).concat("\n~! ").concat(stDate).concat("\n~! ").concat(enDate);

                                otherData.put("Title", titleEv);
                                otherData.put("Description", description);
                                otherData.put("Start Date", ConvertDateFormat(startDate));
                                otherData.put("End Date", ConvertDateFormat(endDate));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "Geolocation":

                        String latitude = ((EditText) view.findViewById(1000)).getText().toString().trim();
                        String longitude = ((EditText) view.findViewById(1001)).getText().toString().trim();
                        if (!latitude.isEmpty() && !longitude.isEmpty()) {
                            qrContent=String.format("geo:%s,%s",latitude,longitude);

                            dbEntryData=latitude.concat("\n~! ").concat(longitude);

                            try {
                                otherData.put("Latitude",latitude);
                                otherData.put("Longitude",longitude);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "Email":
                        String emailET=((EditText) view.findViewById(1000)).getText().toString().trim();
                        String subject=((EditText) view.findViewById(1001)).getText().toString().trim();
                        String body=((EditText) view.findViewById(1002)).getText().toString().trim();
                        if(isEmailOK(emailET)) {
                            if(!subject.isEmpty()&&!body.isEmpty()) {
                                qrContent = String.format("MATMSG:TO:%s;SUB:%s;BODY:%s;;", emailET,subject,body);

                                dbEntryData=emailET.concat("\n~! ").concat(subject).concat("\n~! ").concat(body);
                                try {
                                    otherData.put("Email",emailET);
                                    otherData.put("Subject",subject);
                                    otherData.put("Body",body);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            Toast.makeText(context, "Invalid Email!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;

                    case "vCard":
                        String fName,lName,company,title,adr,tel,email,url;

                        String st=((EditText) view.findViewById(1001)).getText().toString().trim();
                        int fillField=0;
                        fName=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        st=((EditText) view.findViewById(1002)).getText().toString().trim();
                        lName=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        st=((EditText) view.findViewById(1003)).getText().toString().trim();
                        company=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        st=((EditText) view.findViewById(1004)).getText().toString().trim();
                        title=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        st=((EditText) view.findViewById(1005)).getText().toString().trim();
                        adr=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        String street="",city="",country="";
                        String[] splitAddress=null;
                        splitAddress=adr.split(",");

                        if(splitAddress.length==3) {
                            st=splitAddress[0].trim();
                            street = (st.isEmpty())?"":st;
                            st=splitAddress[1].trim();
                            city = (st.isEmpty())?"":st;
                            st=splitAddress[2].trim();
                            country = (st.isEmpty())?"":st;
                            if(!st.isEmpty()){
                                fillField++;
                            }
                        }else if(adr!=null){
                            st=adr.trim();
                            street=(st.isEmpty())?"":st;
                            if(!st.isEmpty()){
                                fillField++;
                            }
                        }
                        st=((EditText) view.findViewById(1000)).getText().toString().trim();
                        tel=(st.isEmpty())?"":st;
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        st=((EditText) view.findViewById(1007)).getText().toString().trim();
                        email=(st.isEmpty())?"":st;

                        String urlTxt=((EditText)view.findViewById(1008)).getText().toString().trim();
                        st= urlTxt.trim();
                        url=(st.isEmpty())?"":st;
                        if(!url.isEmpty()&&!url.contains("http")){
                            url="https://".concat(url);
                        }
                        if(!st.isEmpty()){
                            fillField++;
                        }
                        if(fillField<1){
                            Toast.makeText(context, "All Fields Are Empty!", Toast.LENGTH_SHORT).show();
                            return;
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
                                "END:VCARD", lName, fName, fName, lName, company, title, street,city,country, tel, email, url
                        );
                        dbEntryData=fName.concat("\n~! ").concat(lName).concat("\n~! ").concat(company).concat("\n~! ").concat(title).concat("\n~! ").concat(street).concat("\n~! ").concat(city).concat("\n~! ").concat(country).concat("\n~! ").concat(tel).concat("\n~! ").concat(email).concat("\n~! ").concat(url);
                        try {
                            otherData.put("Name",fName.concat(" ").concat(lName));
                            otherData.put("Company",company);
                            otherData.put("Title",title);
                            otherData.put("Address",adr);
                            otherData.put("Telephone",tel);
                            otherData.put("Email",email);
                            otherData.put("URL",url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Contact":

                        ((EditText)view.findViewById(1001)).getText().toString().trim();
                        String contactNumber=((EditText)view.findViewById(1000)).getText().toString().trim();
                        if(contactNumber.length()>15){
                            Toast.makeText(context, "Invalid Recipient", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!contactNumber.isEmpty()) {
                           qrContent="tel:".concat(contactNumber);
                            dbEntryData=qrContent;
                        }

                        break;
                    case  "SMS":

                        String recipient=((EditText)view.findViewById(1000)).getText().toString().trim();
                        String content=((EditText)view.findViewById(1001)).getText().toString().trim();
                        if(recipient.length()>15){
                            Toast.makeText(context, "Invalid Recipient", Toast.LENGTH_SHORT).show();
                        }
                        if(!recipient.isEmpty()&&!content.isEmpty()) {
                            qrContent=String.format("smsto:%s:%s",recipient,content);

                            dbEntryData=recipient.concat("\n~! ").concat(content);
                            try {
                                otherData.put("Recipient",recipient);
                                otherData.put("Content",content);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "Website":

                        String website=((EditText)view.findViewById(1000)).getText().toString().trim();
                        Pattern patt = Pattern.compile("(((http|https)://)(www.))?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
                        Matcher matcher = patt.matcher(website);
                        if(!matcher.matches()){
                            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!website.isEmpty()) {
                            qrContent = website;//for ios it works
                            if (!website.contains("http")) {
                                website = "https://".concat(website);
                            }
                            dbEntryData = website;
                        }
                        break;
                    case "Text":
                        qrContent=((EditText)view.findViewById(1000)).getText().toString().trim();
                        dbEntryData=qrContent;
                        break;
                    case "WiFi":

                        String ssid=((EditText)view.findViewById(1000)).getText().toString().trim();
                        String password=((EditText)view.findViewById(1001)).getText().toString().trim();


                        if(!ssid.trim().isEmpty()&&!password.isEmpty()&&!wifiNetwork.isEmpty()){
                            qrContent=String.format("WIFI:S:%s;T:%s;P:%s;;",ssid,wifiNetwork,password);

                            dbEntryData=ssid.concat("\n~! ").concat(password).concat("\n~! ").concat(wifiNetwork);

                            try {
                                otherData.put("SSID",ssid);
                                otherData.put("Password",password);
                                otherData.put("Security Type",wifiNetwork);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }

                if(qrContent.isEmpty()) {
                    Toast.makeText(context, "Please Fill All Required Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
        switch (qrType){
            case "Product":
            case "ISBN":
            case "EAN13":
            case "EAN8":
            case "ITF":
            case "PDF 417":
            case "UPC E":
            case "UPC A":
                ((MainActivity)getActivity()).ChangeFragment("resultFragment2D",new String[]{qrContent,qrType,otherData.toString()});
                InsertIntoDB(qrType,qrContent);
                break;
            default:
                InsertIntoDB(qrType,dbEntryData);
                ((MainActivity)getActivity()).ChangeFragment("resultFragment",new String[]{qrContent,qrType,otherData.toString()});
                break;
        }

            }
        });
    }
    void InsertIntoDB(String qrType, String content){
        QRData qrData = new QRData();
        qrData.qrType = qrType;
        qrData.historyType=HistoryType.createHistory.toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        qrData.creationTime=dateFormat.format(new Date());
        //icon function here
        ((MainActivity)getActivity()).creationQueueToAdd.add(0,new AdapterData(qrType,content, qrData.creationTime, R.drawable.rainbow));
//        ((MainActivity)getActivity()).UpdateCreateHistoryList();
//        List<ContentData> content=new ArrayList<>();
//        content.add(new ContentData("wifi ssid","Data 1"));
//        content.add(new ContentData("password","Data 2"));
//        Gson gson = new Gson();
//        qrData.content=gson.toJson(content);

        qrData.content=content;
        ((MainActivity)getActivity()).InsertHistoryData(qrData);
    }
    void AccessContacts(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS);
        }else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            activityResultLauncherContactRead.launch(intent);
        }
    }
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Intent intent = new Intent(getContext(), PermissionDialogActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("permissionType", "Contacts_Read");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
    void CleanUI(){
        LinearLayout resultLL = view.findViewById(R.id.createContainerLl);
        resultLL.removeAllViewsInLayout();
    }

    @SuppressLint("Range")
    String FetchvCardData(Uri uri,String contentType,String requiredCol){
        Cursor cont=getActivity().getContentResolver().query(uri, null, null, null, null);
        if(!cont.moveToNext()){
            return "";
        }
        int colIndexForId=cont.getColumnIndex(ContactsContract.Contacts._ID);
        String contact_ID=cont.getString(colIndexForId);

        String selection = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.CONTACT_ID+ " = ?";
        String[] selectionArgs = new String[] { contentType, contact_ID };
        Cursor nameCur = getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, selection, selectionArgs, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);

        while (nameCur.moveToNext()) {
            String var = nameCur.getString(nameCur.getColumnIndex(requiredCol));
            return var;
        }
        nameCur.close();
        return "";
    }
    boolean isEmailOK(String emailAddr){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);

        if (pat.matcher(emailAddr).matches()) {
            return true;
        }
        return false;
    }
    private void showDatePickerDialog(int id) {
        // Create a new DatePickerDialog
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Set the selected date in the input field

                String date = String.format(Locale.getDefault(), "%d/%d/%d", dayOfMonth, month + 1, year);
                ((EditText)getView().findViewById(id)).setText(date);
            }
        }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }
    String ConvertDateFormat(String inputDate) {
// Create a SimpleDateFormat object with the input date format
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

// Parse the input date string into a Date object
        try {
            Date date = inputFormat.parse(inputDate);

// Create a SimpleDateFormat object with the output date format
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
// Format the date object into the output date string

            assert date != null;
            return outputFormat.format(date);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return "";
    }

    public boolean IsCorrectEAN13Checksum(String barcode) {
        int sum = 0;
        for (int i = 0; i < barcode.length()-1; i++) {
            int weight = 3 - (i % 2) * 2; // 1 for even digits, 3 for odd digits
            int digit = barcode.charAt(i) - '0'; // convert char to int
            sum += weight * digit;
        }
        int chksm=(10 - (sum % 10));

        int lastDigit=Character.getNumericValue(barcode.charAt(12));
        if(((char)chksm)==lastDigit) {
            return true;
        }
        return false; // return checksum digit
    }

    public boolean IsCorrectEAN8Checksum(String code) {

        int[] codeDigits = new int[8];
        for (int i = 0; i < 8; i++) {
            codeDigits[i] = code.charAt(i) - '0'; // convert char to int
            if (codeDigits[i] < 0) return false;
        }

        int sum1 = codeDigits[1] + codeDigits[3] + codeDigits[5];
        int sum2 = 3 * (codeDigits[0] + codeDigits[2] + codeDigits[4] + codeDigits[6]);

        int checksumValue = sum1 + sum2;
        int checksumDigit = 10 - (checksumValue % 10);
        if (checksumDigit == 10) checksumDigit = 0;

        int lastDigit=Character.getNumericValue(code.charAt(7));
        if(((char)checksumDigit)==lastDigit) {
            return true;
        }
        return false; // return checksum digit
    }


    boolean IsCorrectUPCEChecksum(CharSequence s) throws FormatException {
        int length = s.length();
        if (length == 0) {
            return false;
        }
        int check = Character.digit(s.charAt(length - 1), 10);
        return  (getStandardUPCEANChecksum(s.subSequence(0, length - 1)) == check);
    }

    int getStandardUPCEANChecksum(CharSequence s) throws FormatException {
        int length = s.length();
        int sum = 0;
        for (int i = length - 1; i >= 0; i -= 2) {
            int digit = s.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw FormatException.getFormatInstance();
            }
            sum += digit;
        }
        sum *= 3;
        for (int i = length - 2; i >= 0; i -= 2) {
            int digit = s.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw FormatException.getFormatInstance();
            }
            sum += digit;
        }
        return (1000 - sum) % 10;
    }


}