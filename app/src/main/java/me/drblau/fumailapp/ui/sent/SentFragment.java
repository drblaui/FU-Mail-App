package me.drblau.fumailapp.ui.sent;

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

public class SentFragment extends Fragment {
    private SentViewModel sentViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sentViewModel = ViewModelProviders.of(this).get(SentViewModel.class);
        View root = inflater.inflate(R.layout.fragment_sent, container, false);
        final TextView tw = root.findViewById(R.id.text_sent);
        sentViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tw.setText(s);
            }
        });
        return root;
    }
}
