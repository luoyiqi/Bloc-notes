package org.oucho.bloc_notes.notes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import org.oucho.bloc_notes.NotepadApplication;
import org.oucho.bloc_notes.R;
import org.oucho.bloc_notes.note_list.LinkListDialog;
import org.oucho.bloc_notes.note_list.LinkListDialog.LinkListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Note {
	private String text;
	private String fileName = "";
	NoteManager noteManager = null;
	private final List<String> hyperlinks = new ArrayList<>();

	private static final int BUFFER_SIZE = 512;

	public Note(NoteManager noteManager) {
		this.noteManager = noteManager;
		this.text = "";
	}

	public Note(NoteManager noteManager, String content) {
		this(noteManager);
		if (content == null)
			setText("");
		else
			setText(content);
	}

	private Note(NoteManager noteManager, CharSequence content) {
		this(noteManager, content.toString());
	}

	public String getText()
	{
		return text;
	}

	private void findHyperlinks()
	{
		hyperlinks.clear();
		String[] words = this.text.toLowerCase(Locale.getDefault()).split("[\\s]");
		for (String word : words)
		{
			if (word.startsWith("http://") || word.startsWith("https://") || word.startsWith("www."))
			{
				if (word.startsWith("www.")) word = "http://"+word;
				hyperlinks.add(word.trim());
			}
		}
	}

	/**
	 * Gets the list of hyperlinks found in the text
	 */
	public List<String> getHyperlinks()
	{
		return hyperlinks;
	}

	public void setText(String t)
	{
		this.text = t;
		findHyperlinks();
	}

	private String getStart()
	{
		String s = text.trim();
		int end = Math.min(100, s.length());
		int nlPos = s.indexOf('\n');
		if (nlPos > 0)
        {
            end = Math.min(end, nlPos);
        }
		return s.substring(0, end);
	}

	public String toString()
	{
		return getStart();
	}

	public int getID()
	{
		return noteManager.notes.indexOf(this);
	}

	void delete(Context context)
	{
		if (!TextUtils.isEmpty(fileName))
		{
			context.deleteFile(fileName);
		}
	}

	public void popupHyperlinks(final AppCompatActivity activity)
	{
		if (hyperlinks.isEmpty()) return;
		
		LinkListDialog dialog = new LinkListDialog();
		dialog.setHyperlinks(this.hyperlinks);
		dialog.setLinkListener(new LinkListener()
		{
			public void onLinkClicked(String url)
			{
				((NotepadApplication)activity.getApplication()).openLink(url);
			}
		});
		dialog.show(activity.getSupportFragmentManager(), "Link selector");
	}

	public void copyToClipboard(NotepadApplication application)
	{
		application.setClipboardString(text);
	}

	public boolean findChanges(String newVersion)
	{
		return (text.equals(newVersion));
	}

	public void share(Context context)
	{
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(android.content.Intent.EXTRA_TITLE,
				context.getString(R.string.shareEntityName));
		share.putExtra(android.content.Intent.EXTRA_TEXT, text);
		share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(share,
				context.getString(R.string.sharePromptText)));
	}

	public void saveToFile(Context context) throws IOException {

		if (TextUtils.isEmpty(fileName)) {
			fileName = noteManager.generateFilename();
		}

		FileOutputStream file = context.openFileOutput(fileName, Context.MODE_PRIVATE);

		byte[] buffer = text.getBytes();
		file.write(buffer);
		file.flush();
		file.close();

	}


	public static Note newFromFile(NoteManager noteManager, Context context, String filename)
			throws IOException {

		FileInputStream inputFileStream = context.openFileInput(filename);
		StringBuilder stringBuilder = new StringBuilder();
		byte[] buffer = new byte[BUFFER_SIZE];
        int len;

		while ((len = inputFileStream.read(buffer)) > 0)
		{
			String line = new String(buffer, 0, len);
			stringBuilder.append(line);

			buffer = new byte[Note.BUFFER_SIZE];
		}

		Note n = new Note(noteManager, stringBuilder.toString().trim());
		n.fileName = filename;

		inputFileStream.close();

		return n;
	}
	
	public static Note newFromClipboard(NoteManager noteManager, NotepadApplication application) {

		CharSequence string = application.getClipboardString();
		if (string == null) return null;
		return new Note(noteManager, string);
	}
}
