package com.hashtagmentions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class CustomMentionAdapter extends ArrayAdapter<UsersList> {
    private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<UsersList> items;
    private ArrayList<UsersList> itemsAll;
    private ArrayList<UsersList> suggestions;
    private int viewResourceId;
    private Context context;

    public CustomMentionAdapter(@NonNull Context context,int viewResourceId, ArrayList<UsersList> items) {
        super(context,0,items);
        this.items = items;
        this.itemsAll = (ArrayList<UsersList>) items.clone();
        this.suggestions = new ArrayList<UsersList>();
        this.viewResourceId=viewResourceId;
        this.context=context;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(viewResourceId, parent, false);
        }

        TextView strName = (TextView) convertView.findViewById(R.id.userName);
        strName.setText(getItem(position).getName());

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((UsersList)(resultValue)).getName();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (UsersList usersList : itemsAll) {
                    if(usersList.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())){
                        suggestions.add(usersList);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UsersList> filteredList = (ArrayList<UsersList>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (UsersList c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };

}
