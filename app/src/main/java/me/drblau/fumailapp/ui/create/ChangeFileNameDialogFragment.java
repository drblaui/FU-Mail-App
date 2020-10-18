package me.drblau.fumailapp.ui.create;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import me.drblau.fumailapp.R;

public class ChangeFileNameDialogFragment extends DialogFragment {
    private String filename;
    private String fileType;

    static ChangeFileNameDialogFragment newInstance(String filename) {
        ChangeFileNameDialogFragment f = new ChangeFileNameDialogFragment();

        Bundle args = new Bundle();
        args.putString("filename", filename);
        f.setArguments(args);

        return f;
    }

    public interface ChangeFileNameDialogListener {
        //Prevent interference
        public void onFilenameDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private ChangeFileNameDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //Give parent caller the dialog
        try {
            listener = (ChangeFileNameDialogListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "must implement ChangeFileNameDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getArguments().getString("filename");
        //User should not be allowed to edit Filetype(May produce errors)
        fileType = name.substring(name.lastIndexOf("."));
        filename = name.replace(fileType, "");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.change_filename_dialog, null);
        EditText et = (EditText) layout.findViewById(R.id.filename_changer);
        et.setText(filename);
        TextView tw = (TextView) layout.findViewById(R.id.filetype);
        tw.setText(fileType);

        //Hide old name so we can find it later and change it
        tw = (TextView) layout.findViewById(R.id.filename_old);
        tw.setText(filename + fileType);

        builder.setView(layout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onFilenameDialogPositiveClick(ChangeFileNameDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(ChangeFileNameDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
