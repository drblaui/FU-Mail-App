package me.drblau.fumailapp.ui.unread;

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

public class UnreadFragment extends Fragment {
    private UnreadViewModel unreadViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        unreadViewModel = ViewModelProviders.of(this).get(UnreadViewModel.class);
        View root = inflater.inflate(R.layout.fragment_unread, container, false);
        final TextView tw = root.findViewById(R.id.text_unread);
        unreadViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tw.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
