package org.oucho.bloc_notes.note_edit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.oucho.bloc_notes.ConfirmationDialogFragment;
import org.oucho.bloc_notes.NotepadApplication;
import org.oucho.bloc_notes.R;
import org.oucho.bloc_notes.notes.Note;
import org.oucho.bloc_notes.notes.NoteManager;

public class NoteEditActivity extends AppCompatActivity
		implements
		ConfirmationDialogFragment.ConfirmationDialogListener
{
	private Note currentNote;
	private NoteManager noteManager;
	private EditText textEdit;
	
	/*Dialog IDs*/
	private final int DIALOG_DELETE		= 1;
	private final int DIALOG_RESTORE	= 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		NotepadApplication application = (NotepadApplication) this.getApplication();
		noteManager = application.getNoteManager();
		
		int id = getIntent().getExtras().getInt("noteId");
		currentNote = noteManager.getNoteById(id);

		textEdit = (EditText) findViewById(R.id.editText1);		
		if (currentNote == null)
		{
			finish();
			return;
		}
		String s = currentNote.getText();
		textEdit.setText(s);
		moveTextCaret();
		
		try
		{
			setupActionBar();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void setupActionBar()
	{
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public void onPause()
	{
		saveCurrentNote();
		super.onPause();		
	}

    private void moveTextCaret()
	{
		textEdit.setSelection(textEdit.getText().toString().length());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.note_edit_menu, menu);
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		saveOrDelete();
		super.onBackPressed();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
					onBackPressed();
				break;
			case R.id.saveNote:
				saveCurrentNote();
				break;
			case R.id.deleteItem:
				ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(this, getString(R.string.dialogDeleteNote), DIALOG_DELETE);
				dialog.show(getSupportFragmentManager(), "delete");
				break;
			case R.id.revertChanges:
				ConfirmationDialogFragment d = ConfirmationDialogFragment.newInstance(this, getString(R.string.dialogRevertChanges), DIALOG_RESTORE);
				d.show(getSupportFragmentManager(), "restore");
				break;
			case R.id.shareNote:
				saveCurrentNote();
				currentNote.share(this);
				break;
			default:
				return false;
		}
		return true;	
	}
	
	private void saveOrDelete()
	{
		
		if (TextUtils.isEmpty(textEdit.getText())) 
		{
			deleteCurrentNote();
		}
		else saveCurrentNote();
	}
	
	private void deleteCurrentNote()
	{
		noteManager.deleteNote(currentNote);
		currentNote = null;
		Toast.makeText(getApplicationContext(), getString(R.string.toastNoteDeleted), Toast.LENGTH_SHORT).show();
	}
	
	private void refreshNote()
	{
		/*Reverts any changes*/
		textEdit.setText(currentNote.getText());
		moveTextCaret();
	}

	private void saveCurrentNote()
	{
		try
		{
			String s = textEdit.getText().toString();		
			if (currentNote.findChanges(s))
			{
				return;
			}
			currentNote.setText(s);
			currentNote.saveToFile(getApplicationContext());
			Toast.makeText(getApplicationContext(), getString(R.string.toastNoteSaved), Toast.LENGTH_SHORT).show();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public void onYesClicked(Bundle bundle)
	{
		switch (bundle.getInt("dialogId"))
		{
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

	public void onNoClicked()
	{
	}
}
