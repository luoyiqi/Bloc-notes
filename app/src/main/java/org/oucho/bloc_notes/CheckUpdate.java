package org.oucho.bloc_notes;

import android.app.Activity;

import org.oucho.bloc_notes.update.AppUpdater;
import org.oucho.bloc_notes.update.enums.Duration;
import org.oucho.bloc_notes.update.enums.Ecran;
import org.oucho.bloc_notes.update.enums.UpdateFrom;


public class CheckUpdate {

    private static final String updateURL = "http://oucho.free.fr/app_android/Bloc-notes/update_blocnotes.xml";

    public static void onStart(Activity activity){

        new AppUpdater(activity)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML(updateURL)
                .showEvery(5)
                .setDisplay(Ecran.SNACKBAR)
                .setDuration(Duration.NORMAL)
                .start();
    }

    public static void withInfo(Activity activity) {
        new AppUpdater(activity)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML(updateURL)
                .setDisplay(Ecran.DIALOG)
                .showAppUpdated(true)
                .start();
    }

}
