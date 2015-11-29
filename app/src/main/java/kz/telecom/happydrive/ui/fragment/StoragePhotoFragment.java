package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.StorageActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class StoragePhotoFragment extends BaseFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final int INTENT_CODE_CAMERA = 11001;
    private static final int INTENT_CODE_GALLERY = 11002;

    private static final int LAST_ERROR_NO_ISSUES = 0;
    private static final int LAST_ERROR_NO_NETWORK = 1;
    private static final int LAST_ERROR_NO_DATA = 2;
    private static final int LAST_ERROR_DENIED = 3;
    private static final int LAST_ERROR_UNKNOWN = 4;

    private ContentLoadingProgressBar mProgressBar;
    private View mErrorContainerView;
    private TextView mErrorMsgTextView;
    private Button mErrorButton;

    private FolderObject mFolderObject;
    private Card mCard;
    private int mType;

    private PhotoAdapter mAdapter;
    private boolean mIsUpdating;
    private int mLastError = LAST_ERROR_NO_ISSUES;

    private File mTempPhotoFile;

    public static BaseFragment newInstance(FolderObject folderObject, Card card, int type) {
        if (folderObject == null || card == null) {
            throw new IllegalArgumentException("folderObject or card is null");
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(StorageActivity.EXTRA_FOLDER, folderObject);
        bundle.putParcelable(StorageActivity.EXTRA_CARD, card);
        bundle.putInt(StorageActivity.EXTRA_TYPE, type);
        BaseFragment fragment = new StoragePhotoFragment();
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
        return inflater.inflate(R.layout.fragment_storage_photo, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mFolderObject = arguments.getParcelable(StorageActivity.EXTRA_FOLDER);
        mCard = arguments.getParcelable(StorageActivity.EXTRA_CARD);
        mType = arguments.getInt(StorageActivity.EXTRA_TYPE);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(mFolderObject.name);

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.fragment_storage_progress_bar);
        mErrorContainerView = view.findViewById(R.id.fragment_storage_v_error_container);
        mErrorMsgTextView = (TextView) view.findViewById(R.id.fragment_storage_tv_error_msg);
        mErrorButton = (Button) view.findViewById(R.id.fragment_storage_btn_error);

        final GridView gridView = (GridView) view.findViewById(R.id.fragment_storage_photo_grid_view);
        gridView.setOnItemClickListener(this);

        View fab = view.findViewById(R.id.fragment_storage_fab);
        if (User.currentUser().card.compareTo(mCard) == 0) {
            fab.setOnClickListener(this);
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    new AlertDialog.Builder(getContext())
                            .setItems(new String[]{"Удалить"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    deleteData((ApiObject) mAdapter.getItem(position));
                                }
                            }).show();
                    return true;
                }
            });
        } else {
            fab.setVisibility(View.GONE);
        }

        mProgressBar.hide();
        mErrorContainerView.setVisibility(View.GONE);
        if (mAdapter == null) {
            mAdapter = new PhotoAdapter();
            updateData();
        }

        gridView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_storage, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        if (item.getItemId() == R.id.action_update_list) {
            updateData();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.fragment_storage_btn_error) {
            updateData();
        } else if (viewId == R.id.fragment_storage_fab) {
            new AlertDialog.Builder(getContext())
                    .setItems(new String[]{"Создать папку", "Снять фото", "Из галлереи",
                            "Из облачного хранилища"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (which == 0) {
                                createFolder();
                            } else if (which == 1) {
                                try {
                                    mTempPhotoFile = Utils.tempFile(Environment.DIRECTORY_PICTURES, "png");
                                    Utils.openCamera(StoragePhotoFragment.this, mTempPhotoFile, INTENT_CODE_CAMERA);
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Не удалось открыть камеру",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else if (which == 2) {
                                try {
                                    Utils.openGallery(StoragePhotoFragment.this, "", "image/*",
                                            INTENT_CODE_GALLERY);
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Не найдена галлерея",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                            }
                        }
                    }).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ApiObject apiObject = (ApiObject) mAdapter.getItem(position);
        if (apiObject instanceof FileObject) {
            FileObject fileObject = (FileObject) apiObject;
            BaseActivity activity = (BaseActivity) getActivity();
            activity.replaceContent(PortfolioPhotoDetailsFragment.newInstance(fileObject),
                    true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    }

    private synchronized void updateData() {
        if (mIsUpdating) {
            return;
        }

        mIsUpdating = true;
        mProgressBar.show();
        mErrorContainerView.setVisibility(View.GONE);
        new Thread() {
            @Override
            public void run() {
                try {
                    Map<String, List<ApiObject>> mapOfApiObjects =
                            ApiClient.getFiles(mFolderObject.id, mFolderObject.isPublic, null);

                    mAdapter.clear();
                    List<ApiObject> folderObjects = mapOfApiObjects.get(ApiClient.API_KEY_FOLDERS);
                    if (folderObjects != null && folderObjects.size() > 0) {
                        mAdapter.addItems(folderObjects);
                    }

                    List<ApiObject> fileObjects = mapOfApiObjects.get(ApiClient.API_KEY_FILES);
                    if (fileObjects != null && fileObjects.size() > 0) {
                        mAdapter.addItems(fileObjects);
                    }

                    mLastError = LAST_ERROR_NO_ISSUES;
                } catch (NoConnectionError noConnectionError) {
                    mLastError = LAST_ERROR_NO_NETWORK;
                } catch (ApiResponseError apiResponseError) {
                    if (apiResponseError.apiErrorCode ==
                            ApiResponseError.API_RESPONSE_CODE_ACCESS_DENIED) {
                        mLastError = LAST_ERROR_DENIED;
                    }
                } catch (Exception e) {
                    mLastError = LAST_ERROR_UNKNOWN;
                }

                if (getActivity() != null && getView() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.hide();
                            if (mLastError == LAST_ERROR_NO_ISSUES) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

                mIsUpdating = false;
            }
        }.start();
    }

    private synchronized void createFolder() {
    }

    private synchronized void deleteData(final ApiObject apiObject) {
        final String message = apiObject instanceof FolderObject ?
                "Вы уверены, что хотите удалить выбранную папку? Все содержимое папки тоже удалятся" :
                "Вы уверены, что хотите удалить \"" + ((FileObject) apiObject).name + "\" файл?";
        new AlertDialog.Builder(getContext())
                .setTitle("Удаление")
                .setMessage(message)
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Удаление...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new Thread() {
                            @Override
                            public void run() {
                                Exception anException = null;
                                try {
                                    if (apiObject instanceof FolderObject) {
                                        ApiClient.deleteFolder(((FolderObject) apiObject).id);
                                    } else {
                                        ApiClient.deleteFile(((FileObject) apiObject).id);
                                    }
                                } catch (Exception exception) {
                                    anException = exception;
                                }

                                if (anException == null) {
                                    try {
                                        User.currentUser().updateStorageSize();
                                    } catch (Exception ignored) {
                                    }
                                }

                                final Exception exception = anException;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            if (exception == null) {
                                                mAdapter.removeItem(apiObject);
                                                mAdapter.notifyDataSetChanged();
                                                DataManager.getInstance().bus
                                                        .post(new User.OnStorageSizeUpdatedEvent());
                                                if (apiObject instanceof FileObject && mFolderObject.isPublic &&
                                                        mFolderObject.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
                                                    DataManager.getInstance().bus
                                                            .post(new User.OnPortfolioPhotoDeletedEvent(mFolderObject.id));
                                                }
                                            } else if (exception instanceof NoConnectionError) {
                                                Toast.makeText(getContext(), "Нет подключения к интернету",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "Произошла ошибка при удалении файла",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }.start();
                    }
                }).setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_CODE_CAMERA || requestCode == INTENT_CODE_GALLERY) {
                File tempFile;
                if (requestCode == INTENT_CODE_CAMERA) {
                    if (mTempPhotoFile == null || mTempPhotoFile.length() <= 0) {
                        Toast.makeText(getContext(), "Камера не вернула изображение",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tempFile = mTempPhotoFile;
                    mTempPhotoFile = null;
                } else {
                    String path = null;
                    if (Build.VERSION.SDK_INT < 11) {
                        path = Utils.getRealPathFromURI_BelowAPI11(getContext(), data.getData());
                    } else if (Build.VERSION.SDK_INT < 19) {
                        path = Utils.getRealPathFromURI_API11to18(getContext(), data.getData());
                    } else {
                        path = Utils.getRealPathFromURI_API19(getContext(), data.getData());
                    }

                    tempFile = new File(path);
                }

                final File file = tempFile;

                final ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setMessage("Сохранение...");
                dialog.setCancelable(false);
                dialog.show();

                new Thread() {
                    @Override
                    public void run() {
                        boolean isSuccessful = false;
                        FileObject obj = null;

                        try {
                            obj = ApiClient.uploadFile(mFolderObject.id, file);
                            isSuccessful = true;
                        } catch (Exception ignored) {
                        }

                        if (isSuccessful) {
                            try {
                                User.currentUser().updateStorageSize();
                            } catch (Exception ignored) {
                            }
                        }

                        final FileObject fileObject = obj;
                        final boolean success = isSuccessful;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    if (success) {
                                        mAdapter.addItem(fileObject);
                                        mAdapter.notifyDataSetChanged();
                                        DataManager.getInstance().bus
                                                .post(new User.OnStorageSizeUpdatedEvent());
                                        if (mFolderObject.isPublic &&
                                                mFolderObject.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
                                            DataManager.getInstance().bus
                                                    .post(new User.OnPortfolioPhotoUploadEvent(fileObject));
                                        }
                                    } else {
                                        if (getView() != null) {
                                            Snackbar.make(getView(), "Не удалось сохранить",
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
        }
    }

    private class PhotoAdapter extends BaseAdapter {
        private static final int VIEW_TYPE_FILE = 0;
        private static final int VIEW_TYPE_FOLDER = 1;

        private List<ApiObject> mItems;

        public void addItems(List<ApiObject> items) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.addAll(items);
        }

        public void addItem(ApiObject item) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.add(item);
        }

        public void removeItem(ApiObject item) {
            if (mItems != null) {
                mItems.remove(item);
            }
        }

        public void clear() {
            if (mItems != null) {
                mItems.clear();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int viewType = getItemViewType(position);
            Object viewHolder;
            if (convertView == null) {
                if (viewType == VIEW_TYPE_FILE) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.layout_storage_photo_file_item, parent, false);
                    ImageView imageView = (ImageView) convertView
                            .findViewById(R.id.layout_storage_photo_imgview_file);
                    viewHolder = new FileViewHolder(imageView);
                } else {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.layout_storage_photo_folder_item, parent, false);
                    ImageView imageView = (ImageView) convertView
                            .findViewById(R.id.layout_storage_photo_imgview_folder);
                    TextView textView = (TextView) convertView
                            .findViewById(R.id.layout_storage_photo_tv_title);
                    viewHolder = new FolderViewHolder(imageView, textView);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = convertView.getTag();
            }

            if (viewType == VIEW_TYPE_FILE) {
                final FileObject fileObject = (FileObject) getItem(position);
                if (fileObject.getType() == ApiObject.TYPE_FILE_PHOTO) {
                    FileViewHolder fileViewHolder = (FileViewHolder) viewHolder;
                    ImageView imageView = fileViewHolder.imageView;
                    NetworkManager.getGlide()
                            .load(fileObject.url)
                            .centerCrop()
                            .error(R.drawable.image_album)
                            .placeholder(R.drawable.image_album_load)
                            .into(imageView);
                }
            } else {
                FolderViewHolder folderViewHolder = (FolderViewHolder) viewHolder;
                FolderObject folderObject = (FolderObject) getItem(position);
                folderViewHolder.textView.setText(folderObject.name);
            }

            return convertView;
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
            ApiObject apiObject = mItems.get(position);
            if (apiObject instanceof FileObject) {
                return ((FileObject) apiObject).id;
            } else if (apiObject instanceof FolderObject) {
                return ((FolderObject) apiObject).id;
            }

            return -1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position) instanceof FileObject ?
                    VIEW_TYPE_FILE : VIEW_TYPE_FOLDER;
        }
    }

    private static class FileViewHolder {
        final ImageView imageView;

        public FileViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }

    private static class FolderViewHolder {
        final ImageView imageView;
        final TextView textView;

        public FolderViewHolder(ImageView imageView, TextView textView) {
            this.imageView = imageView;
            this.textView = textView;
        }
    }
}
