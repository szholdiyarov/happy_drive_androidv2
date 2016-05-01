package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.ui.StorageActivity;

/**
 * Created by shgalym on 11/22/15.
 */
public class CloudFragment extends BaseFragment implements View.OnClickListener {
    private TextView mPhotoCounterTextView;
    private TextView mVideoCounterTextView;
    private TextView mMusicCounterTextView;
    private TextView mDocumentsCounterTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cloud, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            final MainActivity activity = (MainActivity) getActivity();
            view.findViewById(R.id.fragment_card_toolbar_fake_drawer_toggler)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.toggleDrawer();
                        }
                    });
        } catch (ClassCastException cce) {
            throw new IllegalStateException("used to work with MainActivity");
        }

        view.findViewById(R.id.fragment_cloud_fl_photo).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_video).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_music).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_documents).setOnClickListener(this);

        mPhotoCounterTextView = (TextView) view.findViewById(R.id.fragment_cloud_fl_photo_counter);
        mVideoCounterTextView = (TextView) view.findViewById(R.id.fragment_cloud_fl_video_counter);
        mMusicCounterTextView = (TextView) view.findViewById(R.id.fragment_cloud_fl_music_counter);
        mDocumentsCounterTextView = (TextView) view.findViewById(R.id.fragment_cloud_fl_documents_counter);

        updateCounters();
        DataManager.getInstance().bus.register(this);
    }

    @Override
    public void onDestroyView() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        final int viewId = view.getId();
        if (viewId == R.id.fragment_cloud_fl_photo) {
            List<FolderObject> folders = User.currentUser().privateFolders;
            for (FolderObject obj : folders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, User.currentUser().card);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_PHOTO);
                }
            }
        } else if (viewId == R.id.fragment_cloud_fl_video) {
            List<FolderObject> folders = User.currentUser().privateFolders;
            for (FolderObject obj : folders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_VIDEO) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, User.currentUser().card);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_VIDEO);
                }
            }
        } else if (viewId == R.id.fragment_cloud_fl_music) {
            List<FolderObject> folders = User.currentUser().privateFolders;
            for (FolderObject obj : folders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_MUSIC) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, User.currentUser().card);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_MUSIC);
                }
            }
        } else if (viewId == R.id.fragment_cloud_fl_documents) {
            List<FolderObject> folders = User.currentUser().privateFolders;
            for (FolderObject obj : folders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_DOCUMENT) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, User.currentUser().card);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_DOCUMENT);
                }
            }
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void updateCounters() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (User.currentUser() != null) {
                        User.updatePrivateFolderCounters(User.currentUser());

                        if (getView() != null) {
                            getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    List<FolderObject> folders = User.currentUser().privateFolders;
                                    for (FolderObject obj : folders) {
                                        if (obj.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
                                            mPhotoCounterTextView.setText(obj.filesCount + "");
                                        } else if (obj.getType() == ApiObject.TYPE_FOLDER_VIDEO) {
                                            mVideoCounterTextView.setText(obj.filesCount + "");
                                        } else if (obj.getType() == ApiObject.TYPE_FOLDER_MUSIC) {
                                            mMusicCounterTextView.setText(obj.filesCount + "");
                                        } else if (obj.getType() == ApiObject.TYPE_FOLDER_DOCUMENT) {
                                            mDocumentsCounterTextView.setText(obj.filesCount + "");
                                        }
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception ignored) {
                    Log.e("test", "", ignored);
                }
            }
        }.start();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onStorageSizeUpdated(User.OnStorageSizeUpdatedEvent event) {
        updateCounters();
    }
}