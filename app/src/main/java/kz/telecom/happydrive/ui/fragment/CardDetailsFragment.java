package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.ui.CardEditActivity;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardDetailsFragment extends BaseFragment {
    private View stubView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        DataManager.getInstance().bus.register(this);
        updateView(view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_card_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            startActivity(new Intent(getContext(), CardEditActivity.class));
        }

        return false;
    }

    public void updateView(View view) {
        Card card = Card.getUserCard(getContext());

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fragment_card_details_progress_bar);
        progressBar.setVisibility(View.GONE);

        if (card == null) {
            TextView textView = (TextView) view.findViewById(R.id.stub_error_tv_msg);
            if (textView == null) {
                stubView = ((ViewStub) view.findViewById(R.id.stub_error)).inflate();
            }

            Button actionButton = (Button) view.findViewById(R.id.stub_error_btn_action);
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), CardEditActivity.class));
                }
            });
        } else {
            if (stubView != null) {
                ((ViewGroup) view).removeView(stubView);
            }

            TextView userName = (TextView) view.findViewById(R.id.username);
            if (userName == null) {
                ((ViewStub) view.findViewById(R.id.fragment_card_details_stub)).inflate();
            }

            String userText = card.firstName;
            if (!TextUtils.isEmpty(card.lastName)) {
                userText += " " + card.lastName;
            }

            if (!TextUtils.isEmpty(card.middleName)) {
                userText += " " + card.middleName;
            }

            userName = (TextView) view.findViewById(R.id.username);
            userName.setText(userText);

            TextView position = (TextView) view.findViewById(R.id.position);
            position.setText(card.position);

            TextView companyName = (TextView) view.findViewById(R.id.company_name);
            if (!TextUtils.isEmpty(card.companyName)) {
                companyName.setVisibility(View.VISIBLE);
                companyName.setText(card.companyName);
            } else {
                companyName.setVisibility(View.GONE);
            }

            TextView phoneNumber = (TextView) view.findViewById(R.id.phone);
            phoneNumber.setText(card.phoneNumber);

            View emailBlock = view.findViewById(R.id.email_block);
            if (!TextUtils.isEmpty(card.email)) {
                TextView email = (TextView) view.findViewById(R.id.email);
                email.setText(card.email);
                emailBlock.setVisibility(View.VISIBLE);
            } else {
                emailBlock.setVisibility(View.GONE);
            }

            View addressBlock = view.findViewById(R.id.address_block);
            if (!TextUtils.isEmpty(card.workAddress)) {
                TextView address = (TextView) view.findViewById(R.id.address);
                address.setText(card.workAddress);
                addressBlock.setVisibility(View.VISIBLE);
            } else {
                addressBlock.setVisibility(View.GONE);
            }

            TextView aboutTextView = (TextView) view.findViewById(R.id.about);
            if (!TextUtils.isEmpty(card.about)) {
                aboutTextView.setText(card.about);
                aboutTextView.setVisibility(View.VISIBLE);
            } else {
                aboutTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroyView();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardUpdate(Card.OnCardUpdateEvent ignored) {
        updateView(getView());
    }
}