package com.example.randyp.bulletindesolde.Activities.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.randyp.bulletindesolde.R;

public class Checkout_bottom_sheetFragment extends BottomSheetDialogFragment {

    public static Checkout_bottom_sheetFragment newInstance() {
        return new Checkout_bottom_sheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payout_bottom_sheet, container,false);

        /**
         * Getting the instances of the views and adding listeners to the views
         */

        return view;
    }
}
