package org.oucho.bloc_notes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.oucho.bloc_notes.ConfirmationDialogFragment.ConfirmationDialogListener;
import org.oucho.bloc_notes.notes.Note;
import org.oucho.bloc_notes.notes.NoteManager;

public class NoteEditActivity extends AppCompatActivity
        implements ConfirmationDialogListener {

    /*Dialog IDs*/
    private final int DIALOG_DELETE = 1;
    private final int DIALOG_RESTORE = 2;
    private Note currentNote;
    private NoteManager noteManager;
    private EditText textEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);

        Context context = getApplicationContext();

        int couleurTitre = ContextCompat.getColor(context, R.color.colorAccent);

        String titre = context.getString(R.string.app_edit);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(Html.fromHtml("<font color='" + couleurTitre + "'>" + titre + "</font>"));

        NotepadApplication application = (NotepadApplication) this.getApplication();
        noteManager = application.getNoteManager();

        int id = getIntent().getExtras().getInt("noteId");
        currentNote = noteManager.getNoteById(id);

        textEdit = (EditText) findViewById(R.id.editText1);


        String s = currentNote.getText();
        textEdit.setText(s);
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
                ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(this, getString(R.string.dialogDeleteNote), DIALOG_DELETE);
                dialog.show(getSupportFragmentManager(), "delete");
                break;
            case R.id.revertChanges:
                ConfirmationDialogFragment d = ConfirmationDialogFragment.newInstance(this, getString(R.string.dialogRevertChanges), DIALOG_RESTORE);
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
        noteManager.deleteNote(currentNote);
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
