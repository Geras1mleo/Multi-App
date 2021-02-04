package my.bestapp.multiapp.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import my.bestapp.multiapp.AlertReceiver;
import my.bestapp.multiapp.DatabaseHelper;
import my.bestapp.multiapp.MainActivity;
import com.example.multiapp.R;

import java.util.ArrayList;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MultiApp);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){ onBackPressed(); }
        return true;
    }

    @Override
    public void onBackPressed() {
        //Restarting ToDoFragment
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        ListPreference vibrationList;
        SwitchPreference switchPref;
        Preference deleteFoldersPref;
        SharedPreferences spVibration;
        SharedPreferences.Editor editorVibration;
        int index;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            deleteFoldersPref = findPreference("folders_delete");
            deleteFoldersPref.setOnPreferenceClickListener(deleteFoldersListener);

            switchPref = findPreference("vibration_switch");
            vibrationList = findPreference("list_vibrationtime");

            switchPref.setOnPreferenceChangeListener(switchListener);
            vibrationList.setOnPreferenceChangeListener(vibrationListListener);

            spVibration = getActivity().getSharedPreferences("Reaction_settings", Context.MODE_PRIVATE);
            editorVibration = spVibration.edit();

            vibrationListListener.onPreferenceChange(vibrationList, spVibration.getInt("VibrationTime", 100));
            vibrationList.setValueIndex(index);
            switchPref.setChecked(spVibration.getBoolean("VibrationEnabled", true));
        }

        Preference.OnPreferenceChangeListener vibrationListListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                index = 0;
                for (int i = 0; i < vibrationList.getEntryValues().length; i++) {
                    if(vibrationList.getEntryValues()[i].equals(newValue.toString())){
                        index = i;
                        break; }
                }
                preference.setSummary(vibrationList.getEntries()[index]);
                editorVibration.putInt("VibrationTime", Integer.parseInt(String.valueOf(newValue))).apply();
                return false;
            }
        };

        Preference.OnPreferenceChangeListener switchListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editorVibration.putBoolean("VibrationEnabled", (boolean)newValue).apply();
                return true;
            }
        };

        Preference.OnPreferenceClickListener deleteFoldersListener = new Preference.OnPreferenceClickListener() {
            AlertDialog.Builder mainBuilder;
            AlertDialog mainDialog;

            SharedPreferences spFolders;
            SharedPreferences.Editor editorFolders;

            ArrayList<CheckBox> checkBoxes = new ArrayList<>();
            ArrayList<TextView> textViews = new ArrayList<>();
            ArrayList<View> views = new ArrayList<>();

            ArrayList<String> tableNames = new ArrayList<>();
            ArrayList<String> folderNames = new ArrayList<>();
            LinearLayout layout;
            ScrollView scrollView;
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Clearing old data bc if user deleted folder it keep exist in these fields
                if (mainBuilder != null){
                    mainBuilder = null;
                    mainDialog = null;
                    spFolders = null;
                    editorFolders = null;
                    layout = null;
                    checkBoxes = new ArrayList<>();
                    textViews = new ArrayList<>();
                    views = new ArrayList<>();
                    tableNames = new ArrayList<>();
                    folderNames = new ArrayList<>();
                }

                mainBuilder = new AlertDialog.Builder(getActivity());

                getFoldersAndTables();

                mainBuilder.setTitle("Choose folders to delete");
                mainBuilder.setNegativeButton("Cancel", cancelListener);
                mainBuilder.setPositiveButton("Delete", null);
                setFoldersToDialog();

                mainDialog = mainBuilder.create();
                mainDialog.setOnShowListener(onShowListener);

                mainDialog.show();
                return false;
            }

            private void getFoldersAndTables() {
                spFolders = getActivity().getSharedPreferences("Folders", Context.MODE_PRIVATE);
                editorFolders = spFolders.edit();

                Object[] tables = spFolders.getAll().values().toArray();
                String[] folders = spFolders.getAll().keySet().toArray(new String[0]);
                for (int i = 0; i < tables.length; i++){
                    tableNames.add(String.valueOf(tables[i]));
                    folderNames.add(folders[i]);
                }
            }

            private void setFoldersToDialog() {
                layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                scrollView = new ScrollView(getActivity());
                scrollView.addView(layout);
                mainBuilder.setView(scrollView);
                for (int i = 0; i < tableNames.size(); i++){
                    if (tableNames.get(i).equals("main_table")) continue;

                    View view = getLayoutInflater().inflate(R.layout.folder_delete_button, null);
                    views.add(view);
                    layout.addView(view);
                    checkBoxes.add(view.findViewById(R.id.checkBox_delete));
                    textViews.add(view.findViewById(R.id.folder_delete_textview));
                    textViews.get(views.lastIndexOf(view)).setText(folderNames.get(i));
                }
            }

            private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkBoxes.size() < 1)  return;
                            for (int i = 0; i < checkBoxes.size(); i++){
                                if (checkBoxes.get(i).isChecked())  break;
                                if(i + 1 == checkBoxes.size()) {
                                    Toast.makeText(getActivity(), "Select folders to delete", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            deleteListener.onClick(null, 0);
                        }
                    });
                }
            };

            private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            private DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                    TextView confirmTextView = new TextView(getActivity());

                    confirmTextView.setGravity(Gravity.START);
                    confirmTextView.setTextColor(Color.BLACK);
                    confirmTextView.setPadding(70,20,0,20);
                    confirmTextView.setTextSize(20);
                    String foldersText = "";
                    for (int i = 0; i < checkBoxes.size(); i++){
                        if (checkBoxes.get(i).isChecked()){
                            foldersText += textViews.get(i).getText() + "\n";
                        }
                    }
                    foldersText = foldersText.subSequence(0, foldersText.length() -1).toString();
                    confirmTextView.setText(foldersText);
                    confirmBuilder.setTitle("Are you sure want to delete folders?");
                    confirmBuilder.setView(confirmTextView);
                    confirmBuilder.setPositiveButton("Yes", yesListiner);
                    confirmBuilder.setNegativeButton("No", cancelListener);
                    confirmBuilder.show();
                }
                private DialogInterface.OnClickListener yesListiner = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkBoxes.size(); i++){
                            if (checkBoxes.get(i).isChecked()){
                                String folderToDelete = textViews.get(i).getText().toString();
                                String tableToDelete = tableNames.get(folderNames.indexOf(textViews.get(i).getText()));

                                DatabaseHelper db = new DatabaseHelper(getActivity());
                                Cursor cursor = db.showData(tableToDelete);

                                while (cursor.moveToNext()){
                                    String date = cursor.getString(2);
                                    if (!date.equals("")){
                                        SharedPreferences sp = getActivity().getSharedPreferences("Notifications", Context.MODE_PRIVATE);
                                        int id = sp.getInt(date, -1);

                                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                                        Intent intent = new Intent(getActivity(), AlertReceiver.class);
                                        intent.putExtra("Id", id);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                        sp.edit().remove(date).apply();
                                        alarmManager.cancel(pendingIntent);
                                    }
                                }
                                editorFolders.remove(folderToDelete).apply();
                                db.removeTable(tableToDelete);
                            }
                        }
                        mainDialog.dismiss();
                    }
                };
            };
        };
    }
}