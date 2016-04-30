package org.oucho.bloc_notes.update;

import android.content.Context;
import android.os.AsyncTask;

@SuppressWarnings("unused")
class GetAsyncXML {

    static class LatestAppVersion extends AsyncTask<Void, Void, Update> {
        private final Context context;
        private final String xmlUrl;
        private final AppUpdater.LibraryListener listener;

        public LatestAppVersion(Context context, String xmlUrl, AppUpdater.LibraryListener listener) {
            this.context = context;
            this.xmlUrl = xmlUrl;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (Utils.isNetworkAvailable(context)) {

                if ((xmlUrl == null || !Utils.isStringAnUrl(xmlUrl))) {

                    listener.onFailed(Error.XML_URL_MALFORMED);
                    cancel(true);
                }
            } else {
                listener.onFailed(Error.NETWORK_NOT_AVAILABLE);
                cancel(true);
            }
        }

        @Override
        protected Update doInBackground(Void... voids) {


            try {

                return Utils.getLatestAppVersionXml(xmlUrl);

            } catch (Exception e) {
                cancel(true);
            }

            return null;

        }

        @Override
        protected void onPostExecute(Update update) {
            super.onPostExecute(update);

            if (Utils.isStringAVersion(update.getLatestVersion()))
                listener.onSuccess(update);

        }
    }

}
