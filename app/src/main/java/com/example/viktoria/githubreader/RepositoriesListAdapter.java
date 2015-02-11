package com.example.viktoria.githubreader;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viktoria on 11.02.15.
 */
public class RepositoriesListAdapter extends ArrayAdapter<Repository> {
    Context context;
    List<Repository> repositories;
    int layoutResourceId; //layout of row

    public RepositoriesListAdapter(Context context, int layoutResourceId,
                                   List<Repository> repositories) {
        super(context, layoutResourceId, repositories);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.repositories = repositories;
    }

    //holder pattern used to avoids frequent call of findViewById()
    static class RepositoriesHolder {
        TextView repName;
        TextView repLang;
        TextView forksCount;
        TextView starsCount;

    }

    @Override
    public int getCount() {
        return repositories.size();
    }

    @Override
    public Repository getItem(int position) {
        return repositories.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RepositoriesHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new RepositoriesHolder();
            holder.repName = (TextView) row.findViewById(R.id.repName);
            holder.repLang = (TextView) row.findViewById(R.id.repLang);
            holder.forksCount = (TextView) row.findViewById(R.id.forksCount);
            holder.starsCount = (TextView) row.findViewById(R.id.starsCount);
            row.setTag(holder);
        } else {
            holder = (RepositoriesHolder) row.getTag();
        }
        Repository rep_item = getItem(position);
        if (rep_item != null) {
            holder.repName.setText(rep_item.getName());
            if (!rep_item.getLanguage().isEmpty()) {
                holder.repLang.setText("Language: " + rep_item.getLanguage());
                holder.repLang.setVisibility(View.VISIBLE);
            } else {
                holder.repLang.setVisibility(View.GONE);
            }
            holder.forksCount.setText(String.valueOf(rep_item.getForks()));
            holder.starsCount.setText(String.valueOf(rep_item.getWatchers()));
        }
        return row;
    }
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}