package com.shockwavegames.qr.generator.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomAdapter extends ArrayAdapter<String>{

    String[] names;
    int[] icons;
    Context context;
    public CustomAdapter(@NonNull Context _context, String[] _names,int[] _icons ){
        super(_context, R.layout.customapplist);
        names=_names;
        icons=_icons;
        context=_context;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder=new ViewHolder();
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customapplist, parent, false);
            convertView.setTag(viewHolder);

            viewHolder.icon = convertView.findViewById(R.id.appsIconIV);
            viewHolder.name = convertView.findViewById(R.id.appNameTV);
        }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }

        viewHolder.icon.setImageResource(icons[position]);
        viewHolder.name.setText(names[position]);
        return convertView;
    }
    static class ViewHolder{
        ImageView icon;
        TextView name;
    }
}
