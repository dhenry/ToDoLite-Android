package com.couchbase.todolite.ui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.todolite.R;
import com.couchbase.todolite.helper.LiveQueryRecyclerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ListsAdapter extends LiveQueryRecyclerAdapter<ListsAdapter.ViewHolder> implements View.OnClickListener {

    private OnItemClickListener onItemClickListener;

    public ListsAdapter(Context context, LiveQuery liveQuery) {
        super(context, liveQuery);
    }

    @Override
    public ListsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.recycler_view_item_row, viewGroup, false);
        ViewHolder holder = new ViewHolder(view, i);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ListsAdapter.ViewHolder listViewHolder = (ListsAdapter.ViewHolder) viewHolder;
        final Document task = (Document) getItem(position);
        listViewHolder.textView.setText((String) task.getProperty("title"));
        listViewHolder.textView.setOnClickListener(this);
        listViewHolder.textView.setTag(position);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.listRowText) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, (Integer) view.getTag());
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    /*
    The ViewHolder class extends the RecyclerView.ViewHolder and
    is responsible for storing the inflated views in order to
    recycle them. It's a parameter type on the ListAdapter class.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.listRowText)
        TextView textView;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            /*
            With Butter Knife, not need to set the view fields one by one
            using the findById method. Instead, we use annotations and call
            the inject method here.
             */
            ButterKnife.inject(this, itemView);
        }

    }
}
