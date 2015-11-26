package kz.telecom.happydrive.ui.fragment;

<<<<<<< HEAD
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.ui.BaseActivity;

/**
 * Created by shgalym on 11/26/15.
 */
public class PortfolioPhotoDetailsFragment extends BaseFragment {
    public static final String EXTRA_FILE_OBJECT = "extra:fileobj";

    private FileObject mFileObject;

    public static BaseFragment newInstance(FileObject fileObject) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_FILE_OBJECT, fileObject);

        BaseFragment fragment = new PortfolioPhotoDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_photo_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mFileObject = getArguments().getParcelable(EXTRA_FILE_OBJECT);
        }

        if (mFileObject == null) {
            getActivity().onBackPressed();
            Toast.makeText(getContext(), "Передано не изображение", Toast.LENGTH_SHORT).show();
            return;
        }

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(mFileObject.name);
    }
}
