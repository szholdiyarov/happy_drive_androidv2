package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class PortfolioPhotoFragment extends BaseFragment {
    public static final String EXTRA_CARD = "extra:card";

    private static final int INTENT_CODE_PHOTO_CAMERA = 30001;
    private static final int INTENT_CODE_PHOTO_GALLERY = 30002;

    private boolean isUpdating = false;
    private PhotoAdapter mAdapter;

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);

        BaseFragment fragment = new PortfolioPhotoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_photo, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mAdapter == null) {
            mAdapter = new PhotoAdapter(null);
            updateItems();
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setItems(new String[]{"Снять фото", "Из Галлереи", "Из облачного хранилища"},
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        try {
                                            if (which == 0) {
                                                Utils.openCamera(PortfolioPhotoFragment.this,
                                                        Utils.tempFileWithNow(getContext()),
                                                        INTENT_CODE_PHOTO_CAMERA);
                                            } else if (which == 1) {
                                                Utils.openGallery(PortfolioPhotoFragment.this, "", "image/*",
                                                        INTENT_CODE_PHOTO_GALLERY);
                                            } else {
                                            }
                                        } catch (Exception e) {
                                            Logger.e("TEST", e.getLocalizedMessage(), e);
                                        }
                                    }
                                }).show();
            }
        });

        final GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.portfolio_photo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        if (item.getItemId() == R.id.action_update_list) {
            updateItems();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    synchronized void updateItems() {
        isUpdating = true;

        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Загрузка...");
        dialog.setCancelable(false);
        dialog.show();

        new Thread() {
            @Override
            public void run() {
                boolean isSuccessful = false;

                try {
                    Map<String, List<ApiObject>> mapOfFolders =
                            ApiClient.getFiles(User.currentUser().photoFolderId, true, null);
                    List<ApiObject> apiObjects = mapOfFolders.get(ApiClient.API_KEY_FILES);
                    if (apiObjects != null) {
                        isSuccessful = true;
                        List<FileObject> files = new ArrayList<>(apiObjects.size());
                        for (ApiObject obj : apiObjects) {
                            if (obj instanceof FileObject) {
                                files.add((FileObject) obj);
                            }
                        }

                        mAdapter.setItems(files, false);
                    }
                } catch (Exception e) {
                    if (e instanceof NoConnectionError) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    View view = getView();
                                    if (view != null) {
                                        Snackbar.make(view, "Нет подключения к интернету",
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }

                final boolean success = isSuccessful;
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            if (success) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

                isUpdating = false;
            }
        }.start();
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_CODE_PHOTO_GALLERY) {
                try {
                    String path = null;
                    if (Build.VERSION.SDK_INT < 11) {
                        path = Utils.getRealPathFromURI_BelowAPI11(getContext(), data.getData());
                    } else if (Build.VERSION.SDK_INT < 19) {
                        path = Utils.getRealPathFromURI_API11to18(getContext(), data.getData());
                    } else {
                        path = Utils.getRealPathFromURI_API19(getContext(), data.getData());
                    }

                    final File file = new File(path);
                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setMessage("Сохранение...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            boolean isSuccessful = false;
                            try {
                                FileObject fileObject = ApiClient.uploadFile(User.currentUser().photoFolderId, file);
                                mAdapter.addItem(fileObject, false);
                                isSuccessful = true;
                            } catch (Exception e) {
                                Logger.e("TEST", e.getLocalizedMessage(), e);
                            }

                            final boolean success = isSuccessful;
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (success) {
                                            mAdapter.notifyDataSetChanged();
                                        } else {
                                            View view = getView();
                                            if (view != null) {
                                                Snackbar.make(view, "Не удалось сохранить...",
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    Logger.e("TEST", e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private class PhotoAdapter extends BaseAdapter {
        private List<FileObject> mItems;

        PhotoAdapter(List<FileObject> items) {
            mItems = items;
        }

        void setItems(List<FileObject> items, boolean dispatchChanges) {
            mItems = items;
            if (dispatchChanges) {
                notifyDataSetChanged();
            }
        }

        void addItem(FileObject item, boolean dispatchChanges) {
            if (mItems == null) {
                mItems = new ArrayList<>(1);
            }

            mItems.add(item);
            if (dispatchChanges) {
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.portfolio_photo_row, parent, false);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.portfolio_image);
                vh = new ViewHolder(imageView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            FileObject fileObject = (FileObject) getItem(position);
            NetworkManager.getPicasso()
                    .load(fileObject.url)
                    .error(R.drawable.image_album)
                    .placeholder(R.drawable.image_album)
                    .config(Bitmap.Config.RGB_565)
                    .into(vh.imageView);

            return convertView;
        }
    }

    private static class ViewHolder {
        final ImageView imageView;

        ViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }
}
