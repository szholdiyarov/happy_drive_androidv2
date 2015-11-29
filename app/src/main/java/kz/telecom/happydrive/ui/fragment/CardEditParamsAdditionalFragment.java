package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.ui.CardEditActivity;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardEditParamsAdditionalFragment extends BaseFragment implements CardEditActivity.IsSavable {
    private static final String EXTRA_CARD = "extra:card";

    private EditText mEmailEditText;
    private EditText mWebsiteEditText;
    private EditText mAddressEditText;
    private EditText mAboutEditText;

    private Card mCard;

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);
        BaseFragment fragment = new CardEditParamsAdditionalFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_edit_params_additional, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCard = getArguments().getParcelable(EXTRA_CARD);

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_email);
        mWebsiteEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_website);
        mAddressEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_address);
        mAboutEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_about);

        mEmailEditText.setText(mCard.getEmail());
        mAddressEditText.setText(mCard.getAddress());
        mAboutEditText.setText(mCard.getShortDesc());
    }

    @Override
    public boolean readyForSave() {
        if (getView() == null) {
            return false;
        }

        mCard.setEmail(mEmailEditText.getText().toString());
        mCard.setAddress(mAddressEditText.getText().toString());
        mCard.setShortDesc(mAboutEditText.getText().toString());

        return true;
    }
}
