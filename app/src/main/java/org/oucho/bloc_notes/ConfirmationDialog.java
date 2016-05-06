package org.oucho.bloc_notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {
    private static ConfirmationDialogListener listener;
    private final Bundle bundle;

    public ConfirmationDialog() {
        this.bundle = new Bundle();
    }

    public static ConfirmationDialog newInstance(Activity activity, String question, int id) {

        try {
            listener = (ConfirmationDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ConfirmationDialogListener");
        }
        ConfirmationDialog dialog = new ConfirmationDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("dialogId", id);
        bundle.putString("text", question);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void setArguments(Bundle bundle) {
        this.bundle.putAll(bundle);
        super.setArguments(bundle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(bundle.getString("text"));
        builder.setTitle(R.string.app_name);

        builder.setPositiveButton(getString(android.R.string.yes), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener.onYesClicked(bundle);

            }
        });
        builder.setNegativeButton(getString(android.R.string.no), new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                listener.onNoClicked();

            }
        });

        return builder.create();
    }

    public interface ConfirmationDialogListener {
        void onYesClicked(Bundle bundle);

        void onNoClicked();
    }
}
