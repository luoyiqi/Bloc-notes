package org.oucho.bloc_notes;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



public class Note {
    private static final int BUFFER_SIZE = 512;
    GestionNotes gestionNotes = null;
    private String text;
    private String fileName = "";

    public Note(GestionNotes gestionNotes) {
        this.gestionNotes = gestionNotes;
        this.text = "";
    }

    public Note(GestionNotes gestionNotes, String content) {
        this(gestionNotes);
        if (content == null)
            setText("");
        else
            setText(content);
    }

    private Note(GestionNotes gestionNotes, CharSequence content) {
        this(gestionNotes, content.toString());
    }

    public static Note newFromFile(GestionNotes gestionNotes, Context context, String filename)
            throws IOException {

        FileInputStream inputFileStream = context.openFileInput(filename);
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        while ((len = inputFileStream.read(buffer)) > 0) {
            String line = new String(buffer, 0, len);
            stringBuilder.append(line);

            buffer = new byte[Note.BUFFER_SIZE];
        }

        Note n = new Note(gestionNotes, stringBuilder.toString().trim());
        n.fileName = filename;

        inputFileStream.close();

        return n;
    }

    public static Note newFromClipboard(GestionNotes gestionNotes, BlocNotesApplication application) {

        CharSequence string = application.getClipboardString();
        if (string == null) return null;
        return new Note(gestionNotes, string);
    }

    public String getText() {
        return text;
    }

    public void setText(String t) {
        this.text = t;
    }


    private String getStart() {
        String s = text.trim();
        int end = Math.min(100, s.length());
        int nlPos = s.indexOf('\n');
        if (nlPos > 0) {
            end = Math.min(end, nlPos);
        }
        return s.substring(0, end);
    }

    public String toString() {
        return getStart();
    }

    public int getID() {
        return gestionNotes.notes.indexOf(this);
    }

    void delete(Context context) {
        if (!TextUtils.isEmpty(fileName)) {
            context.deleteFile(fileName);
        }
    }

    public void copyToClipboard(BlocNotesApplication application) {
        application.setClipboardString(text);
    }

    public boolean findChanges(String newVersion) {
        return (text.equals(newVersion));
    }

    public void share(Context context) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(android.content.Intent.EXTRA_TITLE, context.getString(R.string.shareEntityName));
        share.putExtra(android.content.Intent.EXTRA_TEXT, text);
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(share, context.getString(R.string.sharePromptText)));
    }

    public void saveToFile(Context context) throws IOException {

        if (TextUtils.isEmpty(fileName)) {
            fileName = gestionNotes.generateFilename();
        }

        FileOutputStream file = context.openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] buffer = text.getBytes();
        file.write(buffer);
        file.flush();
        file.close();

    }
}
