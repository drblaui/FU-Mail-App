package me.drblau.fumailapp.ui.empty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import me.drblau.fumailapp.R;

public class EmptyFragment extends Fragment {
    private EmptyViewModel emptyViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        emptyViewModel = ViewModelProviders.of(this).get(EmptyViewModel.class);
        View root = inflater.inflate(R.layout.fragment_empty, container, false);
        final TextView tw = root.findViewById(R.id.text_empty);
        emptyViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tw.setText(s);
            }
        });
        return root;
    }
}
