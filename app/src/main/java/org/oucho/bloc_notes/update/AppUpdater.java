package org.oucho.bloc_notes.update;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.oucho.bloc_notes.R;

import java.net.URL;

public class AppUpdater {
    private final Context context;
    private final Preferences libraryPreferences;
    private String xmlUrl;
    private Integer showEvery;


    public AppUpdater(Context context) {
        this.context = context;
        this.libraryPreferences = new Preferences(context);
        this.showEvery = 1;
    }


    public AppUpdater setUpdateXML(@NonNull String xmlUrl) {
        this.xmlUrl = xmlUrl;
        return this;
    }

    /**
     * afficher mise à jour disponible tout les X démarrage
     */
    public AppUpdater showEvery(Integer times) {
        this.showEvery = times;
        return this;
    }


    public void start() {

        GetAsyncXML.LatestAppVersion latestAppVersion = new GetAsyncXML.LatestAppVersion(context, xmlUrl, new LibraryListener() {

            @Override
            public void onSuccess(Update update) {


                if (Utils.isUpdateAvailable(Utils.getAppInstalledVersion(context), update.getLatestVersion())) {



                    Integer successfulChecks = libraryPreferences.getSuccessfulChecks();

                    if (Utils.isAbleToShow(successfulChecks, showEvery)) {

                        final URL apk = update.getUrlToDownload();

                        String content = getDescriptionUpdate(context, update);


                        Activity activity = (Activity) context;

                        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), content, Snackbar.LENGTH_LONG);
                        snackbar.setAction(context.getResources().getString(R.string.appupdater_btn_update), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Utils.goToUpdate(context, apk);
                            }
                        }).show();

                    }
                    libraryPreferences.setSuccessfulChecks(successfulChecks + 1);
                }
            }

            @Override
            public void onFailed(Error error) {
                if (error == Error.XML_URL_MALFORMED) {
                    throw new IllegalArgumentException("XML file is not valid!");
                }
            }
        });

        latestAppVersion.execute();
    }

    interface LibraryListener {
        void onSuccess(Update update);

        void onFailed(Error error);
    }

    private String getDescriptionUpdate(Context context, Update update) {

        return String.format(context.getResources().getString(R.string.update_available), update.getLatestVersion());

    }

}
