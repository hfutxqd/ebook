package cf.imxqd.ebook.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Henry on 2015/11/27.
 * 封装了数据库的操作
 */
public class Database {
    DBHelper dbh = null;
    static Database database = null;
    Database(Context context)
    {
        dbh = new DBHelper(context);
    }

    public static Database newInstance(Context context) {
        if(database == null)
        {
            database = new Database(context);
        }
        return database;
    }

    public void set(int id, String charset, long count, int page)
    {
        SQLiteDatabase db = dbh.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("charset",charset);
        values.put("count_byte", count);
        values.put("page", page);
        db.update("booklist", values, "id= ? ", new String[]{String.valueOf(id)});
        db.close();
    }

    public void add(String name, String path, String charset)
    {
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("path", path.toString());
        values.put("charset", charset);
        values.put("count_byte", 0);
        values.put("page", 0);
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.insert("booklist", null, values);
        db.close();
    }

    public void remove(int id)
    {
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.execSQL("delete from booklist where id ="+id+";");
        System.out.println("delete");
        db.close();
    }

    public ArrayList<Book> getList()
    {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from booklist",null);
        while (cursor.moveToNext())
        {
            Book book = new Book();
            book.setId(cursor.getInt(cursor.getColumnIndex("id")));
            book.setName(cursor.getString(cursor.getColumnIndex("name")));
            book.setPath(cursor.getString(cursor.getColumnIndex("path")));
            book.setCharset(cursor.getString(cursor.getColumnIndex("charset")));
            book.setCount(cursor.getLong(cursor.getColumnIndex("count_byte")));
            book.setPage(cursor.getInt(cursor.getColumnIndex("page")));
            list.add(book);
        }
        cursor.close();
        db.close();
        return list;
    }

}
