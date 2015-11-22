package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.util.Logger;

/**
 * Created by shgalym on 11/22/15.
 */
public class StoragePhotoFragment extends BaseFragment {
    private PhotoAdapter mAdapter;
    private List<FileObject> mItems;
    private User mUser;

    private boolean isUpdating = false;

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
            mAdapter = new PhotoAdapter();
            updateFiles();
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.portfolio_rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<FileObject> list = new ArrayList<>();
        rv.setAdapter(mAdapter);
    }

    void updateFiles() {
        if (!isUpdating) {
            new Thread() {
                @Override
                public void run() {
                    isUpdating = true;

                    FolderObject photoFolder = null;

                    try {
                        Map<String, List<ApiObject>> objectMap = ApiClient.getFiles(0);
                        List<ApiObject> folderList = objectMap.get(ApiClient.API_KEY_FOLDERS);
                        if (folderList != null && folderList.size() > 0) {
                            for (ApiObject obj : folderList) {
                                FolderObject folder = obj.isFolder() ? (FolderObject) obj : null;
                                if (folder != null && "фото".equalsIgnoreCase(folder.name)) {
                                    photoFolder = folder;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Something went wrong while getting private folder",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    if (photoFolder != null) {
                        try {
                            Map<String, List<ApiObject>> objectMap = ApiClient.getFiles(photoFolder.id);
                            Logger.i("TEST", "obj map: " + objectMap);
                        } catch (Exception e) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Something went wrong while getting photo files",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }

                    isUpdating = false;
                }
            }.start();
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
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
