package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 4/15/16.
 */
public class PromoteInstagramFragment extends BaseFragment implements View.OnClickListener {
    private ImageView mImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_instagram, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_promote_instagram_btn).setOnClickListener(this);
        mImageView = (ImageView) view.findViewById(R.id.fragment_promote_instagram_iv);

        final String filePath = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext())
                .getString("temp_card_file_loc", null);
        if (filePath != null) {
            mImageView.setImageURI(Uri.fromFile(new File(filePath)));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fragment_promote_instagram_btn) {

            try {
                final File tempFile = Utils.tempFile(Environment.DIRECTORY_PICTURES, "jpg");
                View containerView = getView().findViewById(R.id.fragment_promote_instagram_container);
                Utils.takeScreenshot(containerView, tempFile);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setPackage("com.instagram.android");
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
                startActivity(shareIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "Приложение Instagram не найдено",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
