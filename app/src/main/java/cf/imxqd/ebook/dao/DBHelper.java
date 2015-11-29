package cf.imxqd.ebook.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Henry on 2015/11/27.
 * 继承了SQLiteOpenHelper，用于创建、升级数据库
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "data.db"; //数据库名称
    public static final int VERSION = 1; //数据库版本
    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE booklist (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " name TEXT(20) NOT NULL, path TEXT(512) NOT NULL, charset TEXT(10) NOT NULL" +
                ", count_byte LONG NOT NULL, page INTEGER NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
