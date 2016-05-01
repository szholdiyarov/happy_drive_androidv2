package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.PromoteActivity;

/**
 * Created by shgalym on 4/15/16.
 */
public class PromoteWhatsAppFragment extends BaseFragment implements View.OnClickListener {
    private EditText mEditText;

    private String initialText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_whatsapp, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_promote_whatsapp_btn).setOnClickListener(this);
        mEditText = (EditText) view.findViewById(R.id.fragment_promote_whatsapp_et);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
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
        if (viewId == R.id.fragment_promote_whatsapp_btn) {
            final String text = mEditText.getText().toString();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.whatsapp");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.setType("text/plain");

            try {
                startActivity(shareIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "Приложение WhatsApp не найдено",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
