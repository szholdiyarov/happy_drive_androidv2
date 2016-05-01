package kz.telecom.happydrive.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 4/15/16.
 */
public class PromoteLinkFragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_link, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_promote_link_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.fragment_promote_link_btn) {
            Card card = User.currentUser().card;
            final String url = Utils.isEmpty(card.getDomain()) ?
                    "https://happy-drive.kz/card/get/" + card.id :
                    "https://" + card.getDomain() + ".happy-drive.kz";
            ClipboardManager clipboardManager = (ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Моя визитка", url);
            clipboardManager.setPrimaryClip(clipData);

            Toast.makeText(getContext(), "Ссылка скопирована в буфер обмена",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
