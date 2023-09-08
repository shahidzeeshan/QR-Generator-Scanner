package com.shockwavegames.qr.generator.activity;




import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class HistoryCustomAdapter extends ArrayAdapter<String> {

    List<AdapterData> adapterData;
//    String[] qrType;
//    String[] content;
//    String[] creationDate;
//    int[] icons;
    Context context;

    public  HistoryCustomAdapter(Context _context,List<AdapterData> _data){
        super(_context, R.layout.historycustomapplist);
        context=_context;
        adapterData=_data;

    }

    public List<AdapterData> getData() {
        return adapterData;
    }
    @Override
    public int getCount() {
        return adapterData.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        HistoryCustomAdapter.ViewHolderThreeObj viewHolder=new HistoryCustomAdapter.ViewHolderThreeObj();

        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.historycustomapplist, parent, false);

            convertView.setTag(viewHolder);
            viewHolder.icon = convertView.findViewById(R.id.typeIconIV);
            viewHolder.qrType = convertView.findViewById(R.id.qrTypeTV);
            viewHolder.content = convertView.findViewById(R.id.qrContentTV);
            viewHolder.creationDate=convertView.findViewById(R.id.qrCreationTV);

        }else {
            viewHolder=(HistoryCustomAdapter.ViewHolderThreeObj)convertView.getTag();

        }

        viewHolder.icon.setImageResource(adapterData.get(position).icon);
        viewHolder.qrType.setText(adapterData.get(position).qrType);
        viewHolder.content.setText(adapterData.get(position).content);
        viewHolder.creationDate.setText(adapterData.get(position).creationDate);
        return convertView;
    }
    static class ViewHolderThreeObj{
        ImageView icon;
        TextView qrType;
        TextView content;
        TextView creationDate;
    }
}