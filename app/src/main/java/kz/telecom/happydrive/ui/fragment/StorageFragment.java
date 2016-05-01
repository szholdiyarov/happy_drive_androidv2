package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.StorageActivity;
import kz.telecom.happydrive.ui.StorageDetailsActivity;
import kz.telecom.happydrive.ui.StorageMediaPlayerActivity;
import kz.telecom.happydrive.ui.widget.AutoGridLayoutManager;
import kz.telecom.happydrive.ui.widget.DocumentAdapter;
import kz.telecom.happydrive.ui.widget.ItemOffsetDecoration;
import kz.telecom.happydrive.ui.widget.MusicAdapter;
import kz.telecom.happydrive.ui.widget.PhotoAdapter;
import kz.telecom.happydrive.ui.widget.StorageAdapter;
import kz.telecom.happydrive.ui.widget.VideoAdapter;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by shgalym on 24.12.2015.
 */
public class StorageFragment extends BaseFragment implements View.OnClickListener,
        StorageAdapter.OnStorageItemClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = Logger.makeLogTag("StorageFragment");

    private static final int GET_CONTENT_REQUEST_CODE = 1201;

    private static final int LAST_ERROR_NO_ISSUES = 0;
    private static final int LAST_ERROR_NO_NETWORK = 1;
    private static final int LAST_ERROR_NO_DATA = 2;
    private static final int LAST_ERROR_DENIED = 3;
    private static final int LAST_ERROR_UNKNOWN = 4;

    private RecyclerView mRecyclerView;
    private ContentLoadingProgressBar mProgressBar;
    private SmoothProgressBar mSmoothProgressBar;
    private ProgressDialog mProgressDialog;

    private View mErrorContainerView;
    private TextView mErrorMsgTextView;
    private Button mErrorButton;

    private FolderObject mFolderObject;
    private Card mCard;
    private int mType;

    private StorageAdapter mAdapter;
    private boolean mIsUpdating;
    private int mLastError = LAST_ERROR_NO_ISSUES;

    public static BaseFragment newInstance(FolderObject folderObject, Card card, int type) {
        if (folderObject == null || card == null) {
            throw new IllegalArgumentException("folderObject or card is null");
        }

        StorageFragment fragment = new StorageFragment();
        fragment.mFolderObject = folderObject;
        fragment.mCard = card;
        fragment.mType = type;
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
        return inflater.inflate(R.layout.fragment_storage, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_storage_toolbar_fake_drawer_back)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        TextView toolbarTextView = (TextView) view.findViewById(R.id.fragment_storage_toolbar_fake_tv_title);
        toolbarTextView.setText(mFolderObject.name.toUpperCase());

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.fragment_storage_progress_bar);
        mProgressBar.hide();

        mSmoothProgressBar = (SmoothProgressBar) view.findViewById(R.id.fragment_storage_smooth_progress_bar);
        mSmoothProgressBar.setVisibility(View.GONE);

        mErrorContainerView = view.findViewById(R.id.fragment_storage_v_error_container);
        mErrorMsgTextView = (TextView) view.findViewById(R.id.fragment_storage_tv_error_msg);
        mErrorButton = (Button) view.findViewById(R.id.fragment_storage_btn_error);
        mErrorButton.setOnClickListener(this);

        mErrorContainerView.setVisibility(View.GONE);
        if (mAdapter == null) {
            if (mType == StorageActivity.TYPE_PHOTO) {
                mAdapter = new PhotoAdapter(getContext());
            } else if (mType == StorageActivity.TYPE_VIDEO) {
                mAdapter = new VideoAdapter(getContext());
            } else if (mType == StorageActivity.TYPE_MUSIC) {
                mAdapter = new MusicAdapter(getContext());
            } else {
                mAdapter = new DocumentAdapter(getContext());
            }

            mAdapter.setStorageItemClickListener(this);
            updateData();
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_storage_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        if (mType == StorageActivity.TYPE_PHOTO) {
            mRecyclerView.setLayoutManager(new AutoGridLayoutManager(getContext(),
                    getResources().getDimensionPixelSize(R.dimen.storage_image_width),
                    LinearLayoutManager.VERTICAL, false));
            mRecyclerView.addItemDecoration(new ItemOffsetDecoration(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                            getResources().getDisplayMetrics())));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                    LinearLayoutManager.VERTICAL, false));
        }

        View fab = view.findViewById(R.id.fragment_storage_fab);
        fab.setOnClickListener(this);
        if (User.currentUser().card.compareTo(mCard) != 0) {
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fragment_storage_btn_error) {
            if (mLastError == LAST_ERROR_NO_NETWORK) {
                updateData();
            }
        } else if (view.getId() == R.id.fragment_storage_fab) {
            final List<StorageAction> actions = new ArrayList<>();
            actions.add(new StorageAction(StorageAction.ACTION_CREATE_FOLDER));
            if (mType == StorageActivity.TYPE_PHOTO) {
                actions.add(new StorageAction(StorageAction.ACTION_TAKE_PHOTO));
                actions.add(new StorageAction(StorageAction.ACTION_PICK_PHOTO));
            } else if (mType == StorageActivity.TYPE_VIDEO) {
                actions.add(new StorageAction(StorageAction.ACTION_PICK_VIDEO));
            } else if (mType == StorageActivity.TYPE_MUSIC) {
                actions.add(new StorageAction(StorageAction.ACTION_PICK_MUSIC));
            } else if (mType == StorageActivity.TYPE_DOCUMENT) {
                actions.add(new StorageAction(StorageAction.ACTION_PICK_DOC));
            }

            new AlertDialog.Builder(getContext())
                    .setItems(StorageAction.toStringArray(actions), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            handleAction(actions.get(which));
                        }
                    }).show();
        }
    }

    @Override
    public void onItemClick(ApiObject object) {
        BaseActivity activity = (BaseActivity) getActivity();
        if (object.isFolder()) {
            FolderObject folderObject = (FolderObject) object;
            activity.replaceContent(newInstance(folderObject, mCard, mType),
                    true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        } else {
            final int type = object.getType();
            switch (type) {
                case ApiObject.TYPE_FILE_PHOTO: {
                    Intent intent = new Intent(activity, StorageDetailsActivity.class);
                    intent.putExtra(StorageDetailsActivity.EXTRA_FILE, object);
                    intent.putExtra(StorageDetailsActivity.EXTRA_CARD, mCard);
                    startActivity(intent);
                    break;
                }
                case ApiObject.TYPE_FILE_MUSIC:
                case ApiObject.TYPE_FILE_VIDEO: {
                    Intent intent = new Intent(activity, StorageMediaPlayerActivity.class);
                    intent.putExtra(StorageMediaPlayerActivity.EXTRA_FILE, object);
                    startActivity(intent);
                    break;
                }
                case ApiObject.TYPE_FILE_DOCUMENT:
                default: {
                    FileObject fileObject = (FileObject) object;
                    final File destFile = new File(getContext().getExternalCacheDir(),
                            mFolderObject.id + File.separator + fileObject.name);

                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Logger.i(TAG, "Открытие файла:" + destFile.getAbsolutePath());
                            Logger.i(TAG, "размер файла: " + destFile.length());

                            String fileExtension = Utils.fileExtension(destFile.getAbsolutePath());
                            if (fileExtension != null && fileExtension.length() > 0) {
                                fileExtension = fileExtension.substring(1);
                            }

                            Intent newIntent = new Intent(Intent.ACTION_VIEW);
                            String mimeType = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(fileExtension);
                            if (mimeType == null) {
                                mimeType = "application/" + fileExtension;
                            }
                            newIntent.setDataAndType(Uri.fromFile(destFile), mimeType);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                getContext().startActivity(newIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getContext(), "Не найдено приложение для открытия этого файла",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    if (!destFile.exists() || destFile.length() == 0) {
                        if (!destFile.getParentFile().mkdirs()) {
                            Toast.makeText(getContext(), "Не удалось загрузить файл", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Открытие файла...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new AsyncTask<String, Integer, String>() {
                            private PowerManager.WakeLock mWakeLock;

                            @Override
                            protected String doInBackground(String... params) {
                                InputStream input = null;
                                OutputStream output = null;
                                HttpURLConnection connection = null;
                                try {
                                    URL url = new URL(params[0]);
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.addRequestProperty("Auth-Token", User.currentUser().token);
                                    connection.connect();

                                    // expect HTTP 200 OK, so we don't mistakenly save error report
                                    // instead of the file
                                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                        return "Server returned HTTP " + connection.getResponseCode()
                                                + " " + connection.getResponseMessage();
                                    }

                                    // this will be useful to display download percentage
                                    // might be -1: server did not report the length
                                    int fileLength = connection.getContentLength();

                                    // download the file
                                    input = connection.getInputStream();
                                    output = new FileOutputStream(destFile);

                                    byte data[] = new byte[4096];
                                    long total = 0;
                                    int count;
                                    while ((count = input.read(data)) != -1) {
                                        // allow canceling with back button
                                        if (isCancelled()) {
                                            input.close();
                                            return null;
                                        }
                                        total += count;
                                        // publishing the progress....
                                        if (fileLength > 0) // only if total length is known
                                            publishProgress((int) (total * 100 / fileLength));
                                        output.write(data, 0, count);
                                    }
                                } catch (Exception e) {
                                    return e.toString();
                                } finally {
                                    try {
                                        if (output != null)
                                            output.close();
                                        if (input != null)
                                            input.close();
                                    } catch (IOException ignored) {
                                    }

                                    if (connection != null)
                                        connection.disconnect();
                                }
                                return null;
                            }

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                // take CPU lock to prevent CPU from going off if the user
                                // presses the power button during download
                                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                        getClass().getName());
                                mWakeLock.acquire();
                            }

                            @Override
                            protected void onProgressUpdate(Integer... progress) {
                                super.onProgressUpdate(progress);
                                // if we get here, length is known, now set indeterminate to false
                                progressDialog.setIndeterminate(false);
                                progressDialog.setMax(100);
                                progressDialog.setProgress(progress[0]);
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                mWakeLock.release();
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                if (result != null) {
                                    Toast.makeText(getContext(), "Не удалось загрузить", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                runnable.run();
                            }
                        }.execute(fileObject.url);
                    } else {
                        runnable.run();
                    }
                }
            }
        }
    }

    @Override
    public void onItemLongClick(final ApiObject object) {
        final List<StorageAction> actions = new ArrayList<>();
        if (object instanceof FileObject) {
            actions.add(new StorageAction(StorageAction.ACTION_DOWNLOAD, object));
        }

        if (User.currentUser().card.compareTo(mCard) == 0) {
            actions.add(new StorageAction(StorageAction.ACTION_DELETE, object));
        }

        new AlertDialog.Builder(getContext())
                .setItems(StorageAction.toStringArray(actions), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        handleAction(actions.get(which));
                    }
                }).show();
    }

    private void handleAction(StorageAction action) {
        if (action.action == StorageAction.ACTION_DELETE) {
            deleteStorageItem(action.object);
        } else if (action.action == StorageAction.ACTION_CREATE_FOLDER) {
            createFolder();
        } else if (action.action == StorageAction.ACTION_DOWNLOAD) {
            FileObject fileObject = (FileObject) action.object;
            DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileObject.url));
            request.addRequestHeader("Auth-Token", User.currentUser().token);
            request.addRequestHeader("Content-Type", "application/octet-stream");
            request.addRequestHeader("Content-Disposition", "attachment; filename=\"" + fileObject.name + "\"");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileObject.name);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            manager.enqueue(request);
        } else if (action.action == StorageAction.ACTION_TAKE_PHOTO) {
            EasyImage.openCamera(this);
        } else {
            Intent intent = FileUtils.createGetContentIntent();
            if (action.action == StorageAction.ACTION_PICK_PHOTO) {
                intent.setType("image/*");
            } else if (action.action == StorageAction.ACTION_PICK_VIDEO) {
                intent.setType("video/*");
            } else if (action.action == StorageAction.ACTION_PICK_MUSIC) {
                intent.setType("audio/*");
            }

            try {
                startActivityForResult(Intent.createChooser(intent, "Выберите файл"),
                        GET_CONTENT_REQUEST_CODE);
            } catch (ActivityNotFoundException afe) {
                Toast.makeText(getContext(), "Приложение не найдено", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_storage, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update_list) {
            updateData();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                    mAdapter.clear(false);
                    List<ApiObject> folderObjects = mapOfApiObjects.get(ApiClient.API_KEY_FOLDERS);
                    if (folderObjects != null && folderObjects.size() > 0) {
                        mAdapter.addAll(folderObjects, false);
                    }

                    List<ApiObject> fileObjects = mapOfApiObjects.get(ApiClient.API_KEY_FILES);
                    if (fileObjects != null && fileObjects.size() > 0) {
                        mAdapter.addAll(fileObjects, false);
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
                            } else if (mLastError == LAST_ERROR_NO_NETWORK) {
                                mErrorContainerView.setVisibility(View.VISIBLE);
                                mErrorMsgTextView.setText(R.string.no_connection);
                                mErrorButton.setText(R.string.retry);
                            } else {
                                Snackbar.make(getView(), "Произошла ошибка",
                                        Snackbar.LENGTH_LONG)
                                        .setAction(R.string.retry, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                updateData();
                                            }
                                        }).show();
                            }
                        }
                    });
                }

                mIsUpdating = false;
            }
        }.start();
    }

    private synchronized void createFolder() {
        FrameLayout frameLayout = new FrameLayout(getContext());

        final int margin = Utils.dipToPixels(18f, getResources().getDisplayMetrics());
        FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fParams.setMargins(margin, 0, margin, 0);

        final EditText editText = new AppCompatEditText(getContext());
        editText.setLayoutParams(fParams);
        editText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setHint("Название для новой папки");
        editText.setSingleLine();

        frameLayout.addView(editText);
        new AlertDialog.Builder(getContext())
                .setView(frameLayout)
                .setTitle("Создание папки")
                .setCancelable(false)
                .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String folderName = editText.getText().toString();
                        if (Utils.isEmpty(folderName)) {
                            editText.setError("Поле не может быть пустым");
                            return;
                        }

                        dialog.dismiss();
                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Создание...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    final FolderObject folderObject = ApiClient.createFolder(mFolderObject.id,
                                            mFolderObject.isPublic, folderName);
                                    mAdapter.add(folderObject, false, false);
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                } catch (final Exception e) {
                                    if (getActivity() != null && getView() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();

                                                String message;
                                                if (e instanceof NoConnectionError) {
                                                    message = "Нет подключения к интернету";
                                                } else if (e instanceof ApiResponseError) {
                                                    int apiErrorCode = ((ApiResponseError) e).apiErrorCode;
                                                    if (apiErrorCode == ApiResponseError.API_RESPONSE_CODE_ACCESS_DENIED) {
                                                        message = "У вас нет доступа";
                                                    } else {
                                                        message = "Произошла ошибка. Сообщите " +
                                                                "разработчикам код ошибки: " + apiErrorCode;
                                                    }
                                                } else {
                                                    message = "Произошла неизвестная ошибка";
                                                }

                                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        }.start();

                    }
                }).setNegativeButton("Отмена", null)
                .show();
    }

    public void uploadFile(final File file) {
        if (file == null || file.length() < 0) {
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("Сохранение...");
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    final FileObject fileObject = ApiClient.uploadFile(mFolderObject.id, file);

                    try {
                        User.currentUser().updateStorageSize();
                    } catch (Exception ignored) {
                    }

                    mAdapter.add(fileObject, false, true);
                    if (getActivity() != null && getView() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                DataManager.getInstance().bus
                                        .post(new User.OnStorageSizeUpdatedEvent());

                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (final Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                if (e instanceof NoConnectionError) {
                                    if (getView() != null) {
                                        Snackbar.make(getView(), R.string.no_connection,
                                                Snackbar.LENGTH_LONG)
                                                .setAction("Повторить", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mProgressDialog.show();
                                                        uploadFile(file);
                                                    }
                                                }).show();
                                    } else {
                                        Toast.makeText(getContext(), R.string.no_connection,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else if (e instanceof ApiResponseError) {
                                    final int errorCode = ((ApiResponseError) e).apiErrorCode;
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Ошибка")
                                            .setMessage("Произошла ошибка во время сохранения файла. " +
                                                    "Пожалуйста, сообщите код ошибки службе поддержки: " + errorCode)
                                            .show();
                                } else {
                                    Toast.makeText(getContext(), "Произошла неизвестная ошибка. Повторите еще раз." +
                                                    " Если ошибка повторяется, пожалуйста, обратитесь в службу поддержки",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private synchronized void deleteStorageItem(final ApiObject apiObject) {
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
                                                mAdapter.remove(apiObject, true);
                                                DataManager.getInstance().bus
                                                        .post(new User.OnStorageSizeUpdatedEvent());
                                                if (apiObject instanceof FileObject && mFolderObject.isPublic &&
                                                        mFolderObject.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final File file = FileUtils.getFile(getContext(), data.getData());
                uploadFile(file);
            }
        } else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new EasyImage.Callbacks() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                }

                @Override
                public void onImagePicked(File file, EasyImage.ImageSource imageSource) {
                    uploadFile(file);
                }

                @Override
                public void onCanceled(EasyImage.ImageSource imageSource) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }

                    if (imageSource == EasyImage.ImageSource.CAMERA) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                        if (photoFile != null) {
                            photoFile.delete();
                        }
                    }
                }
            });
        }
    }

    static class StorageAction {
        static final int ACTION_CREATE_FOLDER = 1;
        static final int ACTION_TAKE_PHOTO = 2;
        static final int ACTION_TAKE_VIDEO = 3;
        static final int ACTION_PICK_PHOTO = 4;
        static final int ACTION_PICK_VIDEO = 5;
        static final int ACTION_PICK_MUSIC = 6;
        static final int ACTION_PICK_DOC = 7;
        static final int ACTION_DOWNLOAD = 8;
        static final int ACTION_DELETE = 9;

        final int action;
        final ApiObject object;

        StorageAction(int action) {
            this.action = action;
            this.object = null;
        }

        StorageAction(int action, ApiObject object) {
            this.action = action;
            this.object = object;
        }

        @Override
        public String toString() {
            switch (action) {
                case ACTION_CREATE_FOLDER:
                    return "Создать папку";
                case ACTION_TAKE_PHOTO:
                    return "Снять фото";
                case ACTION_TAKE_VIDEO:
                    return "Снять видео";
                case ACTION_PICK_PHOTO:
                    return "Из галереи";
                case ACTION_PICK_VIDEO:
                    return "Из галереи";
                case ACTION_PICK_MUSIC:
                    return "Из галереи";
                case ACTION_PICK_DOC:
                    return "Из галереи";
                case ACTION_DOWNLOAD:
                    return "Скачать";
                case ACTION_DELETE:
                    return "Удалить";
                default:
                    return "UNKNOWN_ACTION";
            }
        }

        static String[] toStringArray(List<StorageAction> actions) {
            String[] val = new String[actions.size()];
            for (int i = 0; i < actions.size(); i++) {
                val[i] = actions.get(i).toString();
            }

            return val;
        }
    }
}
