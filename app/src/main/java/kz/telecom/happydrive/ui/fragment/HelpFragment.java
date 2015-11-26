package kz.telecom.happydrive.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kz.telecom.happydrive.R;

/**
 * Created by darkhan on 24.11.15.
 */
public class HelpFragment extends BaseFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

}
