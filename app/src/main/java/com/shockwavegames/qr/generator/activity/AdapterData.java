package com.shockwavegames.qr.generator.activity;

public class AdapterData {
    public String qrType;
    public String content;
    public String creationDate;
    public int icon;

    public AdapterData(String _qrType,String _content,String _creationDate,int _icon){
        qrType=_qrType;
        content=_content;
        creationDate=_creationDate;
        icon=_icon;
    }
}
