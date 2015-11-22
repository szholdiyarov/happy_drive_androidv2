package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.User;

/**
 * Created by shgalym on 11/22/15.
 */
public class StoragePhotoFragment extends BaseFragment {
    private PhotoAdapter mAdapter;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public static BaseFragment newInstance(User user) {
        StoragePhotoFragment fragment = new StoragePhotoFragment();
        fragment.mUser = user;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_photo, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mAdapter == null) {
            mAdapter = new PhotoAdapter(null);
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.portfolio_rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<FileObject> list = new ArrayList<>();
        rv.setAdapter(mAdapter);
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
        private List<FileObject> mItems;

        PhotoAdapter(List<FileObject> items) {
            super();
            this.mItems = items;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.portfolio_photo_row, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.size() : 0;
        }
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.portfolio_image);
        }

        void bind(FileObject fileObject) {
        }
    }
}
