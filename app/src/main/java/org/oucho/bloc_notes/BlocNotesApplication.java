package org.oucho.bloc_notes;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

public class BlocNotesApplication extends Application {

    private GestionNotes gestionNotes = null;


    public BlocNotesApplication() {
    }

    @Override
    public void onCreate() {

        this.gestionNotes = new GestionNotes(getApplicationContext());
        super.onCreate();

    }

    public GestionNotes getGestionNotes() {

        return gestionNotes;
    }

    public CharSequence getClipboardString() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = manager.getPrimaryClip();

        String textToPaste = clip.getItemAt(0).coerceToText(this).toString();

        if (TextUtils.isEmpty(textToPaste)) return null;
        return textToPaste;
    }

    public void setClipboardString(CharSequence string) {

        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text label", string);
        manager.setPrimaryClip(clipData);

    }
}
