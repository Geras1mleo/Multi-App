package my.bestapp.multiapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.multiapp.R;
import my.bestapp.multiapp.ui.todo.ToDoFragment;

import java.util.ArrayList;

public class FoldersDialog{

    AlertDialog.Builder builder, addFolderBuilder;
    AlertDialog chooseFolderDialog, addFolderDialog;
    ToDoFragment main;
    View dialogView;
    LinearLayout foldersLayout;
    EditText folderInput;
    Button addFolderButton;

    ArrayList<View> folderViews = new ArrayList<>();
    ArrayList<Button> folderButtons = new ArrayList<>();

    public FoldersDialog(ToDoFragment toDoFragment){
        main = toDoFragment;
        showDialog();
    }

    private void showDialog() {

        builder = new AlertDialog.Builder(main.activity);
        builder.setTitle("Choose folder");
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", cancelListener);

        chooseFolderDialog = builder.create();
        dialogView = main.getLayoutInflater().inflate(R.layout.folders_dialog, null);
        foldersLayout = dialogView.findViewById(R.id.folders_layout);

        for (int i = 0; i < main.folderNames.size(); i++){
            folderViews.add(main.getLayoutInflater().inflate(R.layout.folders_button, null));
            foldersLayout.addView(folderViews.get(i));
            folderButtons.add(folderViews.get(i).findViewById(R.id.folder_button));
            folderButtons.get(i).setText(main.folderNames.get(i));
            folderButtons.get(i).setOnClickListener(folderPickedListener);
        }

        addFolderButton = dialogView.findViewById(R.id.btn_add_folder);
        addFolderButton.setOnClickListener(addListener);
        chooseFolderDialog.setView(dialogView);
        chooseFolderDialog.show();
    }

    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(folderViews.size() >= 10){
                Toast.makeText(main.activity, "Max. amount of folders reached", Toast.LENGTH_SHORT).show();
                return;
            }
            addFolderBuilder = new AlertDialog.Builder(main.activity);
            View editView = main.getLayoutInflater().inflate(R.layout.folder_new_name_edittext, null);

            folderInput = editView.findViewById(R.id.folder_name);
            folderInput.addTextChangedListener(textWatcher);

            addFolderBuilder.setView(editView);
            addFolderBuilder.setTitle("Enter folder name");
            addFolderBuilder.setNegativeButton("Cancel", cancelListener);
            addFolderBuilder.setPositiveButton("OK", OKListener);
            addFolderDialog = addFolderBuilder.create();
            addFolderDialog.show();
            addFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    };

    DialogInterface.OnClickListener OKListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newFolderName = folderInput.getText().toString();

            main.editor.putString(newFolderName, "table" + main.foldersCount).apply();

            main.db.addTable("table" + main.foldersCount);
            main.tableNames.add("table" + main.foldersCount);
            main.foldersCount++;
            main.countEditor.putInt("FoldersCount", main.foldersCount).apply();
            main.folderNames.add(newFolderName);

            for (int i = 0; i < main.views.size(); ) main.deleteItem(0);
            main.openFolder(main.folderNames.indexOf(newFolderName));

            dialog.dismiss();
            chooseFolderDialog.cancel();
        }
    };

    View.OnClickListener folderPickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (main.tableIndex != folderButtons.indexOf(v)){

                for (int i = 0; i < main.views.size();) main.deleteItem(0);
                main.openFolder(folderButtons.indexOf(v));

                chooseFolderDialog.dismiss();
            }
            else chooseFolderDialog.dismiss();
        }
    };
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            String newFolderName = folderInput.getText().toString();
            for (int i = 0; i < main.folderNames.size(); i++){
                if (newFolderName.equals(main.folderNames.get(i))){
                    folderInput.setError("This folder already exist!");
                    addFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    return;
                }
            }

            if (newFolderName.isEmpty()){
                folderInput.setError("Folder name cannot be empty!");
                addFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
            else if (newFolderName.length() > 18){
                folderInput.setError("Max. amount of characters is 18!");
                addFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
            else {
                addFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }
    };
}
