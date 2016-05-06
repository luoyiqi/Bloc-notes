package org.oucho.bloc_notes;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.oucho.bloc_notes.ConfirmationDialog.ConfirmationDialogListener;

public class EditNoteActivity extends AppCompatActivity
        implements ConfirmationDialogListener {

    /*Dialog IDs*/
    private final int DIALOG_DELETE = 1;
    private final int DIALOG_RESTORE = 2;
    private Note currentNote;
    private GestionNotes gestionNotes;
    private EditText textEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);

        Context context = getApplicationContext();

        int couleurTitre = ContextCompat.getColor(context, R.color.colorAccent);
        int couleurActionBar = ContextCompat.getColor(context, R.color.selected);


        String titre = context.getString(R.string.app_edit);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(Html.fromHtml("<font color='" + couleurTitre + "'>" + titre + "</font>"));

        ColorDrawable colorDrawable = new ColorDrawable(couleurActionBar);

        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setElevation(0);

        BlocNotesApplication application = (BlocNotesApplication) this.getApplication();
        gestionNotes = application.getGestionNotes();

        int id = getIntent().getExtras().getInt("noteId");
        currentNote = gestionNotes.getNoteById(id);

        String s = currentNote.getText();

        textEdit = (EditText) findViewById(R.id.editText1);
        assert textEdit != null;
        textEdit.setLinksClickable(true);
        textEdit.setAutoLinkMask(Linkify.WEB_URLS);
        textEdit.setMovementMethod(LinkMovementMethod.getInstance());
        //If the edit text contains previous text with potential links
        textEdit.setText(s);
        Linkify.addLinks(textEdit, Linkify.WEB_URLS);

        moveTextCaret();

    }



    @Override
    public void onPause() {
        saveCurrentNote();
        super.onPause();
    }

    private void moveTextCaret() {
        textEdit.setSelection(textEdit.getText().toString().length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_edit_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        saveOrDelete();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.saveNote:
                saveCurrentNote();
                break;
/*            case R.id.shareNote:
                saveCurrentNote();
                currentNote.share(this);
                break;*/
            case R.id.deleteItem:
                ConfirmationDialog dialog = ConfirmationDialog.newInstance(this, getString(R.string.dialogDeleteNote), DIALOG_DELETE);
                dialog.show(getSupportFragmentManager(), "delete");
                break;
            case R.id.revertChanges:
                ConfirmationDialog d = ConfirmationDialog.newInstance(this, getString(R.string.dialogRevertChanges), DIALOG_RESTORE);
                d.show(getSupportFragmentManager(), "restore");
                break;

            default:
                return false;
        }
        return true;
    }

    private void saveOrDelete() {

        if (TextUtils.isEmpty(textEdit.getText())) {
            deleteCurrentNote();
        } else {
            saveCurrentNote();
        }
    }

    private void deleteCurrentNote() {
        gestionNotes.deleteNote(currentNote);
        currentNote = null;
        Toast.makeText(getApplicationContext(), getString(R.string.toastNoteDeleted), Toast.LENGTH_SHORT).show();
    }

    private void refreshNote() {
		/*Reverts any changes*/
        textEdit.setText(currentNote.getText());
        moveTextCaret();
    }

    private void saveCurrentNote() {
        try {
            String s = textEdit.getText().toString();

            if (currentNote.findChanges(s))
                return;

            currentNote.setText(s);
            currentNote.saveToFile(getApplicationContext());
            Toast.makeText(getApplicationContext(), getString(R.string.toastNoteSaved), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onYesClicked(Bundle bundle) {

        switch (bundle.getInt("dialogId")) {
            case DIALOG_DELETE:
                deleteCurrentNote();
                this.setResult(RESULT_OK);
                finish();
                break;

            case DIALOG_RESTORE:
                refreshNote();
                break;
        }
    }

    public void onNoClicked() {
    }
}
