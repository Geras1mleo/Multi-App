package my.bestapp.multiapp.ui.todo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import my.bestapp.multiapp.AlertReceiver;
import my.bestapp.multiapp.DatabaseHelper;
import com.example.multiapp.R;
import my.bestapp.multiapp.ui.FoldersDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ToDoFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener {
    public Activity activity;
    public DatabaseHelper db;
    private View mainView;
    private LinearLayout mainLayout;
    private Button addButton, deleteButton, chooseFolderButton;

    //Variables for folder names and their table names in database
    public SharedPreferences spFolders, spFoldersCount, spNotificationsIds;
    public SharedPreferences.Editor editor, countEditor, notificationsEditor;
    public ArrayList<String> tableNames = new ArrayList<>();
    public ArrayList<String> folderNames = new ArrayList<>();
    public int tableIndex, foldersCount;

    private Calendar calendar;
    private String textDateTime;

    //This is for TextWatcher to know which editText has been changed
    private int globalFocusIndex;

    //I need this when I change some views programmatically to avoid recursion or other bugs
    private boolean toReturn = true;

    //Storing all links to all items in ArrayList
    public ArrayList<View> views = new ArrayList<>(); //public to read size(amount) of existing items in FoldersDialog
    private ArrayList<ConstraintLayout> layouts = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private ArrayList<Switch> switches = new ArrayList<>();
    private ArrayList<TextView> textViews = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_todo, container, false);

        /*getActivity().deleteDatabase(DatabaseHelper.DATABASE_NAME);
        getActivity().getSharedPreferences("Folders", Context.MODE_PRIVATE).edit().clear().commit();
        getActivity().getSharedPreferences("FoldersCount", Context.MODE_PRIVATE).edit().clear().commit();
        getActivity().getSharedPreferences("Notifications", Context.MODE_PRIVATE).edit().clear().commit();*/

        initComponents();
        setNormalUIFlags();

        getFolders(); //From Shared Preferences
        //Always opening Main Folder at the start
        OpenFolder(folderNames.indexOf("Main Folder"));

        return mainView;
    }

    private void initComponents(){
        activity = getActivity();
        db = new DatabaseHelper(activity);
        calendar = Calendar.getInstance();

        spFolders = activity.getSharedPreferences("Folders", Context.MODE_PRIVATE);
        editor = spFolders.edit();
        editor.putString("Main Folder","main_table").apply();
        spFoldersCount = activity.getSharedPreferences("FoldersCounter", Context.MODE_PRIVATE);
        countEditor = spFoldersCount.edit();
        foldersCount = spFoldersCount.getInt("FoldersCount", 1);

        mainLayout = mainView.findViewById(R.id.Linear_V_List);
        addButton = mainView.findViewById(R.id.btn_AddItem);
        deleteButton = mainView.findViewById(R.id.btn_Delete);
        chooseFolderButton = mainView.findViewById(R.id.btn_Folder);
        textDateTime = "";

        deleteButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        chooseFolderButton.setOnClickListener(this);
    }

    private void setNormalUIFlags() {
        ((AppCompatActivity)activity).getSupportActionBar().show();
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void getFolders() {
        Object[] tables = spFolders.getAll().values().toArray();
        String[] folders = spFolders.getAll().keySet().toArray(new String[0]);
        for (int i = 0; i < tables.length; i++){
            tableNames.add(String.valueOf(tables[i]));
            folderNames.add(folders[i]);
        }
    }

    public void OpenFolder(int folderIndex) {
        toReturn = true;
        chooseFolderButton.setText(folderNames.get(folderIndex));
        tableIndex = folderIndex;
        addFolderList();
        toReturn = false;
        countDownTimer.start();
    }

    private void addFolderList(){
        Cursor data = db.showData(tableNames.get(tableIndex));

        if (data == null || data.getCount() <= 0){
            addNewItem("Enter items here!", false, "");
            db.addData(tableNames.get(tableIndex),"Enter items here!", "0", "");
        }
        else {
            while (data.moveToNext())
                addNewItem(data.getString(0), data.getString(1).equals("1"), data.getString(2));

        }
    }

    private void addNewItem(String text, boolean isChecked, String date ) {
        toReturn = true;
        View view = getLayoutInflater().inflate(R.layout.add_new_item, null, false);
        mainLayout.addView(view);

        views.add(view);
        layouts.add(view.findViewById(R.id.ItemLayout));
        textViews.add(view.findViewById(R.id.ItemTextView));

        checkBoxes.add(view.findViewById(R.id.ItemCheckBox));
        checkBoxes.get(views.indexOf(view)).setOnCheckedChangeListener(this);

        editTexts.add(view.findViewById(R.id.ItemEditText));
        editTexts.get(views.lastIndexOf(view)).setOnFocusChangeListener(this);
        editTexts.get(views.lastIndexOf(view)).addTextChangedListener(textWatcher);

        switches.add(view.findViewById(R.id.ItemSwitch));
        switches.get(views.lastIndexOf(view)).setOnCheckedChangeListener(this);

        if (date.equals("")){
            switches.get(views.lastIndexOf(view)).setChecked(false);
            textViews.get(views.lastIndexOf(view)).setText("");
        }
        else {
            textViews.get(views.lastIndexOf(view)).setText(date);
            switches.get(views.lastIndexOf(view)).setChecked(true);
        }

        if (isChecked)
            editTexts.get(views.lastIndexOf(view)).setPaintFlags(editTexts.get(views.lastIndexOf(view)).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        editTexts.get(views.lastIndexOf(view)).setText(text);
        checkBoxes.get(views.lastIndexOf(view)).setChecked(isChecked);
        toReturn = false;
    }

    public void deleteItem(int index) {
        globalFocusIndex--;
        mainLayout.removeView(views.get(index));
        views.remove(index);
        layouts.remove(index);
        checkBoxes.remove(index);
        editTexts.remove(index);
        switches.remove(index);
        textViews.remove(index);
    }

    private void switchBoxSwitched(int index, boolean isChecked) {
        if (isChecked){
            openDateTimeDialogsAsync(index);
        }
        else {
            deleteAlarm(index);
            updateDataBase(index);
        }
    }

    private void openDateTimeDialogsAsync(int index) {
        //We set alarm only when OK button on TIMER picker is clicked
        TimePickerDialog timePicker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
                    calendar = Calendar.getInstance();
                }

                setAlarm(index);
                boolean result = updateDataBase(index);
                if (!result)
                    Toast.makeText(activity, "Something went wrong :(", Toast.LENGTH_LONG).show();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePicker.setCanceledOnTouchOutside(true);
        timePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                textDateTime = "";
                switches.get(index).setChecked(false);
            }
        });
        timePicker.show();

        DatePickerDialog datePicker = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis() + 31556952000L);
        datePicker.show();
    }

    private void setAlarm(int index) {
        textDateTime = new SimpleDateFormat("dd.MM.yy HH:mm").format(calendar.getTime());

        spNotificationsIds = activity.getSharedPreferences("Notifications", Context.MODE_PRIVATE);
        notificationsEditor = spNotificationsIds.edit();
        int notificationLastId = spNotificationsIds.getInt("LastId", 1);

        int id = spNotificationsIds.getInt(textDateTime, -1);

        for(;id != -1;){ //The notification with same time already exist so we will add one minute
            calendar.add(Calendar.MINUTE, 1);
            textDateTime = new SimpleDateFormat("dd.MM.yy HH:mm").format(calendar.getTime());
            id = spNotificationsIds.getInt(textDateTime, -1);
        }
        notificationLastId++;
        notificationsEditor.putInt("LastId", notificationLastId).apply();
        notificationsEditor.putInt(textDateTime, notificationLastId).apply();

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(activity, AlertReceiver.class);
        intent.putExtra("Id", notificationLastId);
        intent.putExtra("Key", textDateTime);
        intent.putExtra("Text", editTexts.get(index).getText().toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, notificationLastId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(activity, "Alarm has been added", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(activity, "Sorry, alarm cannot be added due your android version :(", Toast.LENGTH_SHORT).show();
            switches.get(index).setChecked(false);
            return;
        }

        textViews.get(index).setText(textDateTime);
        toReturn = true;
        switches.get(index).setChecked(true);
        toReturn = false;
    }

    private void deleteAlarm(int index) {
        textDateTime = textViews.get(index).getText().toString();
        textViews.get(index).setText("");

        spNotificationsIds = activity.getSharedPreferences("Notifications", Context.MODE_PRIVATE);
        notificationsEditor = spNotificationsIds.edit();
        int id = spNotificationsIds.getInt(textDateTime, -1);
        notificationsEditor.remove(textDateTime).apply();

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(activity, AlertReceiver.class);
        intent.putExtra("Id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private CountDownTimer countDownTimer = new CountDownTimer(3600000, 20000) {
        @Override
        public void onTick(long millisUntilFinished) { checkAlarm(); }
        @Override
        public void onFinish() {}
    };

    private void checkAlarm(){
        for (int i = 0; i < textViews.size(); i++){
            if (!textViews.get(i).getText().equals("")){
                String text = textViews.get(i).getText().toString();
                long milliseconds = 0;
                try {
                    Date d = new SimpleDateFormat("dd.MM.yy hh:mm").parse(text);
                    milliseconds = d.getTime();
                }catch (Exception a){}

                if (milliseconds+30000 < Calendar.getInstance().getTimeInMillis()){
                    //Don't saving this to Database bc if we delete table in settings it trows exception
                    toReturn = true;
                    switches.get(i).setChecked(false);
                    toReturn = false;
                    deleteAlarm(i);
                }
            }
        }
    }

    private boolean updateDataBase(int index){
        return db.updateData(tableNames.get(tableIndex),String.valueOf(index + 1),
        String.valueOf(editTexts.get(index).getText()),
        checkBoxes.get(index).isChecked()? "1": "0",
        String.valueOf(textViews.get(index).getText()));
    }

    @Override
    public void onClick(View v) {
        if (v == addButton && !toReturn){
            addNewItem("", false, "");
            db.addData(tableNames.get(tableIndex),"", "0", "");

            //So, we will wait for 100 milliseconds bc of database, it makes to much rows in table if we click nonstop
            toReturn = true;
            new CountDownTimer(100, 100){
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() { toReturn = false;}
            }.start();
        }
        else if(v == deleteButton){
            for (int i = 0; i < checkBoxes.size(); i++){
                if(checkBoxes.get(i).isChecked()){
                    if (switches.get(i).isChecked()) deleteAlarm(i);
                    db.deleteData(tableNames.get(tableIndex), String.valueOf(i + 1));
                    deleteItem(i);
                    i--;
                }
            }
        }
        else if(v == chooseFolderButton){
            FoldersDialog dialog = new FoldersDialog(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(toReturn){return;}

        int index = checkBoxes.indexOf(buttonView);

        if (index < 0){
            index = switches.indexOf(buttonView);
            switchBoxSwitched(index, isChecked);
        }
        else {
            updateDataBase(index);
            if(isChecked)
                editTexts.get(index).setPaintFlags(editTexts.get(index).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                editTexts.get(index).setPaintFlags(editTexts.get(index).getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            globalFocusIndex = editTexts.lastIndexOf(v);
            toReturn = false;
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (toReturn){return;}
            updateDataBase(globalFocusIndex);
        }
    };
}