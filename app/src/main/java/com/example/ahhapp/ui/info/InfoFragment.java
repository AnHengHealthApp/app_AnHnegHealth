package com.example.ahhapp.ui.info;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;

public class InfoFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        //返回建
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 返回上一個 Fragment（通常是 Home）
            Navigation.findNavController(v).popBackStack();
        });

        //頭像列
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });
        return view;
    }

    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料已回傳！" ,Toast.LENGTH_SHORT).show();
    }
}
