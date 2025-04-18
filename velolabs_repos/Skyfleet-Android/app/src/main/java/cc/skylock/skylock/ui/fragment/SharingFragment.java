package cc.skylock.skylock.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.HomePageActivity;

/**
 * Created by Velo Labs Android on 07-06-2016.
 */
public class SharingFragment extends Fragment {

    private FragmentTransaction fragmentTransaction;
    private Fragment sharingChildFragment_Home = null;
    public static SharingFragment sharingFragment;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sharing, null);
        sharingChildFragment_Home = SharingChildFragment_Home.newInstance();
        ((HomePageActivity)getActivity()).setFragment(sharingChildFragment_Home, true, "SharingChildFragment_Home");
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static SharingFragment newInstance() {
        if (sharingFragment == null) {
            sharingFragment = new SharingFragment();
        }
        return sharingFragment;
    }


}
