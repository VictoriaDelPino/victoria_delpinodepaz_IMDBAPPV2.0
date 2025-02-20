package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.PhoneCode;

public class PhoneCodeAdapter extends ArrayAdapter<PhoneCode> {
    public PhoneCodeAdapter(Context context, List<PhoneCode> phoneCodes) {
        super(context, 0, phoneCodes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textView);
        PhoneCode phoneCode = getItem(position);
        textView.setText(phoneCode.getFlagEmoji() + " " + phoneCode.getPhoneCode());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

