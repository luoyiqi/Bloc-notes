package org.oucho.bloc_notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

import org.oucho.bloc_notes.ConfirmationDialogFragment.ConfirmationDialogListener;
import org.oucho.bloc_notes.notes.Note;
import org.oucho.bloc_notes.notes.NoteManager;
import org.oucho.bloc_notes.update.AppUpdater;
import org.oucho.bloc_notes.update.enums.Ecran;
import org.oucho.bloc_notes.update.enums.Duration;
import org.oucho.bloc_notes.update.enums.UpdateFrom;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        ConfirmationDialogListener,
        OnGestureListener,
        NavigationView.OnNavigationItemSelectedListener {

    private final HashMap<Note, View> noteTiles = new HashMap<>();
    private final int DIALOG_DELETE = 1;
    private final int NOTE_EDIT = 2;
    private NoteManager noteManager = null;
    private NotepadApplication application;
    private Note selectedNote = null;
    private DrawerLayout mDrawerLayout;

    private final String updateURL = "http://oucho.free.fr/app_android/Bloc-notes/update_blocnotes.xml";


    /* *********************************************************************************************
     * Création de l'activité
     * ********************************************************************************************/
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int couleurTitre = ContextCompat.getColor(context, R.color.colorAccent);

        String titre = context.getString(R.string.app_name);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setTitle(Html.fromHtml("<font color='" + couleurTitre + "'>" + titre + "</font>"));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);


        application = (NotepadApplication) this.getApplication();
        noteManager = application.getNoteManager();

        noteManager.loadNotes();
        loadNotes();

		/* Handling incoming intent */
        Intent intent = getIntent();
        String type = intent.getType(), action = intent.getAction();

        if (type != null && Intent.ACTION_SEND.equals(action)) {
            /* Intent received */
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    openNote(new Note(noteManager, sharedText));
                }
            }
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNote(new Note(noteManager));
            }
        });

        updateOnStart();
    }



    /* *********************************************************************************************
     * Navigation Drawer
     * ********************************************************************************************/

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {

            case R.id.nav_update:
                checkUpdate();
                break;

            case R.id.nav_help:
                about();
                break;

            case R.id.nav_exit:
                exit();
                break;

            default: //do nothing
                break;
        }
        return true;
    }

    private void exit() {

        finish();
    }



    /* *********************************************************************************************
     * Menu
     * ********************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notepad_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*            case R.id.addNote:
                openNote(new Note(noteManager));
                break;*/

            case R.id.pasteNote:
                Note note = noteManager.newFromClipboard(application);
                if (note == null) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toastClipboardEmpty), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    addTile(note);
                }
                break;

            case android.R.id.home:
                break;

            default:
                return false;
        }
        return true;
    }


    private void selectNote(Note note) {
        // Unknown note?
        if (!noteTiles.containsKey(note)) return;

        if (selectedNote != null) {
            //collapseNote(selectedNote);
            selectedNote = null;
        }
        //expandNoteTile(note);
        selectedNote = note;
    }

    private void addTile(Note note) {
        Animation tileAnimation =
                new TranslateAnimation(1000, 0, 0, 0);
        tileAnimation.setDuration(300);
        tileAnimation.setFillAfter(true);

        //noinspection ConstantConditions
        findViewById(R.id.emptyNotifier).setVisibility(View.GONE);
        addTile(note, (ViewGroup) findViewById(R.id.tile_container), (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), tileAnimation);
        selectNote(note);
    }

    private void removeTile(Note note) {
        if (!noteTiles.containsKey(note)) return;
        if (selectedNote == note) selectedNote = null;

        Animation tileAnimation =
                new TranslateAnimation(0, 1000, 0, 0);
        tileAnimation.setDuration(300);
        tileAnimation.setFillAfter(true);

        final View tile = noteTiles.get(note);
        tile.setAnimation(tileAnimation);

        tile.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(new Runnable() {

                    public void run() {
                        ViewGroup parent = (ViewGroup) findViewById(R.id.tile_container);
                        //noinspection ConstantConditions
                        parent.removeView(tile);
                        if (parent.getChildCount() == 0) {
                            //noinspection ConstantConditions
                            findViewById(R.id.emptyNotifier).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }, 500);
    }


    private void addTile(Note note, ViewGroup parent, LayoutInflater inflater, Animation inAnimation) {
        final ViewGroup child = (ViewGroup) inflater.inflate(
                R.layout.note_list_tile, parent, false);
        noteTiles.put(note, child);
        TextView tv = (TextView) child.findViewById(R.id.noteTitle);
        tv.setText(note.getText());
        final Note n = note;


        child.findViewById(R.id.tile_clickable).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        openNote(n);
                    }
                });

        child.findViewById(R.id.tile_clickable).setOnLongClickListener(
                new OnLongClickListener() {

                    public boolean onLongClick(View v) {
                        selectedNote = n;
                        registerForContextMenu(v);
                        openContextMenu(v);
                        unregisterForContextMenu(v);
                        return true;
                    }
                });


        child.setAnimation(inAnimation);
        parent.addView(child);
    }



    /* *********************************************************************************************
     * Menu contextuel
     * ********************************************************************************************/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.contextDelete:
                ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(this, getString(R.string.dialogDeleteSelected), DIALOG_DELETE);

                Bundle b = new Bundle();
                b.putInt("noteId", selectedNote.getID());
                dialog.setArguments(b);
                dialog.show(getSupportFragmentManager(), "contextDelete");
                break;

            case R.id.contextEdit:
                openNote((int) info.id);
                break;

            case R.id.contextDuplicate:
                noteManager.addNote(new Note(noteManager, selectedNote.getText()));
                loadNotes();
                break;

            case R.id.contextShare:
                selectedNote.share(this);
                break;
        }

        return super.onContextItemSelected(item);
    }



    private void populateNoteTiles() {
        TextView tvEmpty = (TextView) findViewById(R.id.emptyNotifier);
        ViewGroup parent = (ViewGroup) findViewById(R.id.tile_container);
        noteTiles.clear();
        //noinspection ConstantConditions
        parent.removeAllViews();
        selectedNote = null;
        if (noteManager.isEmpty()) {
            //noinspection ConstantConditions
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        //noinspection ConstantConditions
        tvEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<Note> notes = noteManager.getAllNotes();

        for (Note note : notes) {
            addTile(note, parent, inflater, null);
        }
    }


    private void loadNotes() {
        populateNoteTiles();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case NOTE_EDIT:
                loadNotes();
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void openNote(int noteId) {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("noteId", noteId);
        intent.putExtra("noteText", noteManager.getNoteById(noteId).getText());
        startActivityForResult(intent, NOTE_EDIT);
    }

    /**
     * Opens note in editor. Adds note to the note manager.
     */
    private void openNote(Note note) {
        noteManager.addNote(note);
        openNote(note.getID());
    }

    public void onYesClicked(Bundle bundle) {
        switch (bundle.getInt("dialogId")) {
            case DIALOG_DELETE:
                if (bundle.containsKey("noteId")) {
                    int noteId = bundle.getInt("noteId");
                    removeTile(noteManager.getNoteById(noteId));
                    noteManager.deleteNote(noteId);

                    Toast.makeText(getApplicationContext(), getString(R.string.toastNoteDeleted), Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public void onNoClicked() {

    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {

    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {

    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }



   /* **********************************************************************************************
    * Mise à jour
    * *********************************************************************************************/

    private void updateOnStart(){

        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML(updateURL)
                .showEvery(5)
                .setDisplay(Ecran.SNACKBAR)
                .setDuration(Duration.NORMAL)
                .start();
    }

    private void checkUpdate() {
        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML(updateURL)
                .setDisplay(Ecran.DIALOG)
                .showAppUpdated(true)
                .start();
    }



    /***********************************************************************************************
     * About dialog
     **********************************************************************************************/

    private void about() {

        String title = getString(R.string.about);
        AlertDialog.Builder about = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams")
        View dialoglayout = inflater.inflate(R.layout.alertdialog_main_noshadow, null);
        Toolbar toolbar = (Toolbar) dialoglayout.findViewById(R.id.dialog_toolbar_noshadow);
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(0xffffffff);

        final TextView text = (TextView) dialoglayout.findViewById(R.id.showrules_dialog);
        text.setText(getString(R.string.about_message));

        about.setView(dialoglayout);

        AlertDialog dialog = about.create();
        dialog.show();
    }
}
