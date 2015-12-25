package kz.telecom.happydrive.ui.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import kz.telecom.happydrive.data.ApiObject;

/**
 * Created by shgalym on 25.12.2015.
 */
public abstract class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {
    private final Object lock = new Object();
    final List<ApiObject> items = new ArrayList<>();
    final LayoutInflater inflater;
    private OnStorageItemClickListener mListener;
    private final Context mContext;

    public StorageAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setStorageItemClickListener(OnStorageItemClickListener listener) {
        mListener = listener;
    }

    public final void add(ApiObject item, boolean dispatch, boolean reversePos) {
        synchronized (lock) {
            boolean isAdded = false;
            if (item.isFolder() || reversePos) {
                for (int i = 0; i < items.size(); i++) {
                    if (!items.get(i).isFolder()) {
                        items.add(i, item);
                        isAdded = true;
                        break;
                    }
                }
            }

            if (!isAdded) {
                items.add(item);
            }
        }

        if (dispatch) {
            notifyDataSetChanged();
        }
    }

    public final void addAll(List<ApiObject> items, boolean dispatch) {
        synchronized (lock) {
            for (ApiObject i : items) {
                add(i, false, false);
            }
        }

        if (dispatch) {
            notifyDataSetChanged();
        }
    }

    public final void remove(ApiObject object, boolean dispatch) {
        synchronized (lock) {
            items.remove(object);
        }

        if (dispatch) {
            notifyDataSetChanged();
        }
    }

    public final void clear(boolean dispatch) {
        synchronized (lock) {
            items.clear();
        }

        if (dispatch) {
            notifyDataSetChanged();
        }
    }

    void dispatchItemClick(ApiObject object) {
        if (mListener != null) {
            mListener.onItemClick(object);
        }
    }

    void dispatchLongItemClick(ApiObject object) {
        if (mListener != null) {
            mListener.onItemLongClick(object);
        }
    }

    Context getContext() {
        return mContext;
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void onBind(ApiObject object);
    }

    public interface OnStorageItemClickListener {
        void onItemClick(ApiObject object);
        void onItemLongClick(ApiObject object);
    }
}
