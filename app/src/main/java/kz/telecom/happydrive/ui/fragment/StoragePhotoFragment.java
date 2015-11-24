package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
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
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class StoragePhotoFragment extends BaseFragment {
    private static final int INTENT_CODE_GALLERY = 50001;

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
//        if (mAdapter == null) {
//            mAdapter = new PhotoAdapter();
//            updateFiles();
//        }
//
//        RecyclerView rv = (RecyclerView) view.findViewById(R.id.portfolio_rv);
//        rv.setHasFixedSize(true);
//        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
//
//        List<FileObject> list = new ArrayList<>();
//        rv.setAdapter(mAdapter);
//
//        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Utils.openGallery(StoragePhotoFragment.this, "", "image/*", INTENT_CODE_GALLERY);
//            }
//        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photo_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        updateFiles();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_CODE_GALLERY) {
                try {
                    Uri selectedImageUri = data.getData();
                    final File file = new File(selectedImageUri.getPath());

                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setMessage("Сохранение...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                ApiClient.uploadFile(0, file);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Something went wrong while uploading file",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    }.start();
                } catch (Exception ee) {
                    Toast.makeText(getContext(), "something went wrong while parsing photo file",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void updateFiles() {
        if (!isUpdating) {
            new Thread() {
                @Override
                public void run() {
                    isUpdating = true;

                    FolderObject photoFolder = null;

                    try {
                        Map<String, List<ApiObject>> objectMap = ApiClient.getFiles(0, false, null);
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
                            Map<String, List<ApiObject>> objectMap = ApiClient.getFiles(photoFolder.id, false, null);
                            if (mItems == null) {
                                mItems = new ArrayList<>();
                            } else {
                                mItems.clear();
                            }

                            List<ApiObject> fileList = objectMap.get(ApiClient.API_KEY_FILES);
                            if (fileList != null && fileList.size() > 0) {
                                for (ApiObject obj : fileList) {
                                    FileObject file = !obj.isFolder() ? (FileObject) obj : null;
                                    if (file != null) {
                                        mItems.add(file);
                                    }
                                }
                            }

                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

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
