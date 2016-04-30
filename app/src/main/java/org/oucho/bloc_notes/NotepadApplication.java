package org.oucho.bloc_notes;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.oucho.bloc_notes.notes.NoteManager;

public class NotepadApplication extends Application {

	private NoteManager noteManager = null;


	public NotepadApplication() {
	}
	
	@Override
	public void onCreate() {

		this.noteManager = new NoteManager(getApplicationContext());
		super.onCreate();

	}

	public NoteManager getNoteManager() {

		return noteManager;
	}
	
	public void openLink(String url) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
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
