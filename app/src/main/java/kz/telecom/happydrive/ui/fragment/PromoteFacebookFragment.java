package kz.telecom.happydrive.ui.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

import java.io.File;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.PromoteActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 4/15/16.
 */
public class PromoteFacebookFragment extends BaseFragment implements View.OnClickListener {
    private EditText mEditText;

    private String initialText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_facebook, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_promote_facebook_btn).setOnClickListener(this);
        mEditText = (EditText) view.findViewById(R.id.fragment_promote_facebook_et);

        if (mEditText.getText().length() == 0) {
            final Card card = User.currentUser().card;
            String userText = card.getFirstName();
            if (!TextUtils.isEmpty(card.getLastName())) {
                userText += " " + card.getLastName();
            }

            String domainText = "";
            if (!TextUtils.isEmpty(card.getDomain())) {
                domainText = "Визитка доступна по адресу https://" + card.getDomain() + ".happy-drive.kz\n\n";
            }

            String text = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(PromoteActivity.SHARED_TEXT, getString(R.string.promote_shared_text,
                            domainText, userText));
            mEditText.setText(text);
            initialText = text;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null) {
            final Card card = User.currentUser().card;
            String userText = card.getFirstName();
            if (!TextUtils.isEmpty(card.getLastName())) {
                userText += " " + card.getLastName();
            }

            String domainText = "";
            if (!TextUtils.isEmpty(card.getDomain())) {
                domainText = "Визитка доступна по адресу https://" + card.getDomain() + ".happy-drive.kz\n\n";
            }

            String text = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(PromoteActivity.SHARED_TEXT, getString(R.string.promote_shared_text,
                            domainText, userText));
            mEditText.setText(text);
            initialText = text;
        } else if (mEditText != null) {
            final String text = mEditText.getText().toString();
            if (!text.equals(initialText) && !TextUtils.isEmpty(initialText)) {
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit().putString(PromoteActivity.SHARED_TEXT, text)
                        .apply();
            }
        }
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.fragment_promote_facebook_btn) {
            final Card card = User.currentUser().card;
            final String text = mEditText.getText().toString();
            final String filePath = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext())
                    .getString("temp_card_file_loc", null);
            File file = null;
            if (filePath != null) {
                file = new File(filePath);
            }

            final File imageFile = file;
            final ProgressDialog progDlg = new ProgressDialog(getContext());

            progDlg.show();
            new Thread() {
                @Override
                public void run() {
                    try {
                        FileObject fo = null;
                        if (imageFile != null) {
                            fo = ApiClient.uploadScreenshot(imageFile);
                        }

                        final FileObject fileObject = fo;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                                            .setContentTitle("Моя визитка")
                                            .setImageUrl(Uri.parse(fileObject.url))
                                            .setContentDescription(text)
                                            .setContentUrl(Utils.isEmpty(card.getDomain()) ?
                                                    Uri.parse("https://happy-drive.kz/card/get/" + card.id) :
                                                    Uri.parse("https://" + card.getDomain()
                                                            + ".happy-drive.kz"))
                                            .build();
                                    MessageDialog.show(PromoteFacebookFragment.this, shareLinkContent);
                                }
                            });
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    } finally {
                        progDlg.dismiss();
                    }
                }
            }.start();
//
//            Intent shareIntent = new Intent();
//            shareIntent.setAction(Intent.ACTION_SEND);
//            shareIntent.setPackage("com.facebook.orca");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
//            shareIntent.setType("text/plain");
//
//            try {
//                startActivity(shareIntent);
//            } catch (android.content.ActivityNotFoundException ex) {
//                Toast.makeText(getContext(), "Приложение Facebook не найдено",
//                        Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
