package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.ui.StorageActivity;

/**
 * Created by shgalym on 11/22/15.
 */
public class CloudFragment extends BaseFragment implements View.OnClickListener {
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

        final View switchViewContainer = view.findViewById(R.id.fragment_card_cloud_switcher_container);
        View cloudSwitcher = switchViewContainer.findViewById(R.id.fragment_card_cloud_switcher_cloud);
        cloudSwitcher.setClickable(true);
        cloudSwitcher.setSelected(true);

        View cardSwitcher = switchViewContainer.findViewById(R.id.fragment_card_cloud_switcher_card);
        cardSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                switchViewContainer.setSelected(false);
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.fragment_cloud_fl_photo).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_video).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_music).setOnClickListener(this);
        view.findViewById(R.id.fragment_cloud_fl_documents).setOnClickListener(this);
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
}