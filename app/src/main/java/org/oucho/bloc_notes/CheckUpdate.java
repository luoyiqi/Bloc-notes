package org.oucho.bloc_notes;

import android.app.Activity;

import org.oucho.bloc_notes.update.AppUpdater;
import org.oucho.bloc_notes.update.enums.Display;


class CheckUpdate {

    private static final String updateURL = "http://oucho.free.fr/app_android/Bloc-notes/update_blocnotes.xml";

    public static void onStart(Activity activity){

        new AppUpdater(activity)
                .setUpdateXML(updateURL)
                .setDisplay(Display.SNACKBAR)
                .start();
    }

    public static void withInfo(Activity activity) {
        new AppUpdater(activity)
                .setUpdateXML(updateURL)
                .setDisplay(Display.DIALOG)
                .showAppUpdated()
                .start();
    }

}
