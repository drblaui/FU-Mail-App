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

public class ChangeMailDialogFragment extends DialogFragment {
    private String mail;

    static ChangeMailDialogFragment newInstance(String mail) {
        ChangeMailDialogFragment f = new ChangeMailDialogFragment();

        Bundle args = new Bundle();
        args.putString("mail", mail);
        f.setArguments(args);

        return f;
    }

    //Handle Login outside of here
    public interface ChangeMailDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private ChangeMailDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //Give Parent Caller the dialog
        try {
            listener = (ChangeMailDialogListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "must implement ChangeMailDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mail = getArguments().getString("mail");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.change_mail_dialog, null);
        EditText et = (EditText) layout.findViewById(R.id.mail_changer);
        et.setText(mail);

        System.out.println(mail);

        builder.setView(layout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(ChangeMailDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(ChangeMailDialogFragment.this);
                    }
                });
        return  builder.create();
    }
}
