package my.bestapp.multiapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ToDoList.db";
    public static final String MAIN_TABLE = "main_table";
    public static final String COL2 = "ITEM";
    public static final String COL3 = "ISCHECKED";
    public static final String COL4 = "DATE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + MAIN_TABLE + " (ITEM TEXT, ISCHECKED TEXT NOT NULL, DATE TEXT DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + MAIN_TABLE);
        onCreate(db);
    }

    public void addData(String table, String text, String isChecked, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, text);
        contentValues.put(COL3, isChecked);
        contentValues.put(COL4, date);

        db.insert(table, null, contentValues);
    }

    public Cursor showData(String table){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            return db.rawQuery("SELECT * FROM " + table, null);
        } catch (Exception a){ return null;}
    }

    public boolean updateData(String table, String id, String text, String isChecked, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, text);
        contentValues.put(COL3, isChecked);
        contentValues.put(COL4, date);
        db.update(table, contentValues, "rowid = ?", new String[] {id});
        return true;
    }

    public void deleteData(String table, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ table + " WHERE rowid = "+ id);
        db.execSQL("VACUUM");
    }

    public void addTable(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE " + table + " (ITEM TEXT, ISCHECKED TEXT NOT NULL, DATE TEXT DEFAULT 0)");
    }
    public void removeTable(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE "+ table);
    }
}
