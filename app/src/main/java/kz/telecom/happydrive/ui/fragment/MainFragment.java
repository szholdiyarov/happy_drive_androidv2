package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.ui.StorageActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);

        BaseFragment fragment = new MainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Card card = getArguments().getParcelable(EXTRA_CARD);

        TextView usernameTextView = (TextView) view.findViewById(R.id.user_name);
        TextView positionTextView = (TextView) view.findViewById(R.id.user_position);

        String lastName = card.getLastName();
        String username = card.getFirstName();
        if (!Utils.isEmpty(lastName)) {
            if (!Utils.isEmpty(username)) {
                username = " " + lastName;
            } else {
                username = lastName;
            }
        }

        usernameTextView.setText(username);
        positionTextView.setText(card.getPosition());

        view.findViewById(R.id.photo_image_view).setOnClickListener(this);
        view.findViewById(R.id.video_image_view).setOnClickListener(this);
        view.findViewById(R.id.music_image_view).setOnClickListener(this);
        view.findViewById(R.id.document_image_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.photo_image_view) {
            intent = new Intent(getContext(), StorageActivity.class);
            intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.EXTRA_TYPE_PHOTO);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
