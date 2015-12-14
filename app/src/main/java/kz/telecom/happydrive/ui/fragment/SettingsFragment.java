package kz.telecom.happydrive.ui.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.CardEditActivity;
import kz.telecom.happydrive.ui.ChangePasswordActivity;
import kz.telecom.happydrive.util.Logger;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by darkhan on 24.11.15.
 */
public class SettingsFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = Logger.makeLogTag("SettingsFragment");

    private ContentLoadingProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;
    private DataAdapter mAdapter = new DataAdapter();
    private ListView mListView;

    private boolean isLoading;
    private boolean isVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(getContext(), R.color.colorPrimary)));
        actionBar.setTitle(R.string.action_settings);

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.fragment_settings_progress_bar);
        mListView = (ListView) view.findViewById(R.id.fragment_settings_list_view);
        mListView.setVisibility(View.INVISIBLE);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);

        loadData();
    }

    private void loadData() {
        if (isLoading) {
            return;
        }

        mProgressBar.show();
        isLoading = true;
        new Thread() {
            @Override
            public void run() {
                boolean isSuccessful = false;
                try {
                    if (User.currentUser().updateCard()) {
                        isVisible = User.currentUser().card.isVisible();
                        isSuccessful = true;
                    }
                } catch (Exception ignored) {
                }

                final boolean successful = isSuccessful;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setVisibility(View.VISIBLE);
                            mAdapter.notifyDataSetInvalidated();
                            mProgressBar.hide();

                            if (!successful) {
                                Toast.makeText(getContext(), "Нет подключения к интернету",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                isLoading = false;
            }
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int itemId = (int) id;
        if (itemId == R.id.action_change_background) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Изменить фон")
                    .setItems(new String[]{"Снять фото", "Из Галлереи"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    try {
                                        if (which == 0) {
                                            EasyImage.openCamera(SettingsFragment.this);
                                        } else if (which == 1) {
                                            EasyImage.openGallery(SettingsFragment.this);
                                        }
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.getLocalizedMessage(), e);
                                        Toast.makeText(getContext(), "Произошла ошибка во время запуска Intent'а." +
                                                " Пожалуйста, сообщите в службу поддержки.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
        } else if (itemId == R.id.action_edit_card) {
            startActivity(new Intent(getContext(), CardEditActivity.class));
        } else if (itemId == R.id.action_change_password) {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        } else if (itemId == R.id.action_toggle_card_visibility) {
            if (!isVisible && User.currentUser().card.getCategoryId() <= 0) {
                Toast.makeText(getContext(), "Вы не можете открыть доступ к " +
                        "визитке пока не заполните обязательные поля", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetInvalidated();
                return;
            }

            toggleCardVisibility(!isVisible);
        } else if (itemId == R.id.action_sign_out) {
            User.currentUser().signOut();
            DataManager.getInstance().bus.post(new User.SignedOutEvent());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    private void toggleCardVisibility(final boolean visible) {
        if (isLoading) {
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("Сохранение...");
            mProgressDialog.setCancelable(false);
        }

        isLoading = true;
        mProgressDialog.show();
        new Thread() {
            @Override
            public void run() {
                boolean isSuccessful = false;
                try {
                    if (ApiClient.setVisibility(visible) && User.currentUser().updateCard()) {
                        isVisible = User.currentUser().card.isVisible();
                        isSuccessful = true;
                    }
                } catch (Exception ignored) {
                }

                final boolean successful = isSuccessful;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetInvalidated();
                            mProgressDialog.dismiss();

                            if (!successful) {
                                Toast.makeText(getContext(), "Нет подключения к интернету",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                isLoading = false;
            }
        }.start();

    }

    private void uploadFile(final File file) {
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
                    if (User.currentUser().changeBackground(file)) {
                        GlideCacheSignature.invalidateBackgroundKey();
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                DataManager.getInstance().bus
                                        .post(new Card.OnCardUpdatedEvent(User.currentUser().card));
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

    private static class ViewHolder {
        final ImageView imageView;
        final TextView titleTextView;
        final SwitchCompat switchCompat;

        ViewHolder(ImageView imgView, TextView tv, SwitchCompat sc) {
            imageView = imgView;
            titleTextView = tv;
            switchCompat = sc;
        }
    }

    private class DataAdapter extends BaseAdapter {
        private static final int VIEW_TYPE_COMMON = 0;
        private static final int VIEW_TYPE_SWITCH = 1;

        private static final int STRIDE = 4;
        private final Object[] ITEMS = new Object[]{
                // action id, Image resource, Text resource, view type
                R.id.action_change_background, R.drawable.ic_card_24p, "Изменить фон", VIEW_TYPE_COMMON,
                R.id.action_edit_card, R.drawable.ic_mode_edit_black_24dp, "Редактировать визитку", VIEW_TYPE_COMMON,
                R.id.action_change_password, R.drawable.ic_refresh_black_24dp, "Сменить пароль", VIEW_TYPE_COMMON,
                R.id.action_toggle_card_visibility, R.drawable.ic_lock_black_24dp, "Показать визитку", VIEW_TYPE_SWITCH,
                R.id.action_sign_out, R.drawable.ic_exit_to_app_black_24dp, "Выйти", VIEW_TYPE_COMMON
        };

        @Override
        public int getCount() {
            return ITEMS.length / STRIDE;
        }

        @Override
        public Object getItem(int position) {
            throw new UnsupportedOperationException("do not call this method");
        }

        @Override
        public long getItemId(int position) {
            return (Integer) ITEMS[position * STRIDE];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_settings_row, parent, false);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.layout_settings_row_image_view);
                TextView textView = (TextView) convertView.findViewById(R.id.layout_settings_row_tv_title);
                SwitchCompat switchCompat = (SwitchCompat) convertView.findViewById(R.id.layout_settings_row_switch_compat);

                final View view = convertView;
                switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked != isVisible) {
                            onItemClick(mListView, view, position, getItemId(position));
                        }
                    }
                });

                viewHolder = new ViewHolder(imageView, textView, switchCompat);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final int viewType = getItemViewType(position);
            viewHolder.switchCompat.setVisibility(viewType == VIEW_TYPE_COMMON ?
                    View.GONE : View.VISIBLE);

            viewHolder.switchCompat.setChecked(isVisible);
            viewHolder.titleTextView.setText((String) ITEMS[position * STRIDE + 2]);
            viewHolder.imageView.setImageResource((int) ITEMS[position * STRIDE + 1]);
            viewHolder.imageView.setColorFilter(Color.GRAY);

            if (position == 1 && User.currentUser().card.getCategoryId() <= 0) {
                viewHolder.titleTextView.setText("Создать визитку");
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return (int) ITEMS[position * STRIDE + 3];
        }

        @Override
        public int getViewTypeCount() {
            List<Integer> viewTypes = new ArrayList<>();
            for (int i = 0; i < ITEMS.length; i += STRIDE) {
                Integer type = (Integer) ITEMS[i + 3];
                if (!viewTypes.contains(type)) {
                    viewTypes.add(type);
                }
            }

            return viewTypes.size();
        }
    }
}