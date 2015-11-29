package cf.imxqd.ebook.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

import cf.imxqd.ebook.R;
import cf.imxqd.ebook.adapter.BookListAdapter;
import cf.imxqd.ebook.dao.Book;
import cf.imxqd.ebook.dao.Database;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    Database database;
    ListView booklist_v;
    BookListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        booklist_v = (ListView) findViewById(R.id.booklist_v);
        database = Database.newInstance(getApplicationContext());
        adapter = new BookListAdapter(getApplicationContext());
        booklist_v.setAdapter(adapter);
        booklist_v.setOnItemClickListener(this);
        booklist_v.setOnItemLongClickListener(this);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                /* Type设定为text */
                intent.setType("plain/text");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得文件后返回本画面 */
                startActivityForResult(intent, 1);
            }
        });
        System.out.println("OnCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this,cf.imxqd.ebook.activity.Reader2Activity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            if(uri.toString().endsWith(".txt") || uri.toString().endsWith(".TXT")) {
                File file = new File(uri.getPath());
                System.out.println(file.getName());
                System.out.println(file.getAbsolutePath());
                database.add(file.getName(), file.getAbsolutePath(), "GBK");
                Toast.makeText(this,"文件导入成功！",Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(this,"不支持的文件格式！",Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.notifyDataSetChanged();
        Book book = (Book) adapter.getItem(position);
        book.print();
        Intent intent = new Intent(this,cf.imxqd.ebook.activity.ReaderActivity.class);
        intent.putExtra("path", book.getPath());
        intent.putExtra("page", book.getPage());
        intent.putExtra("count", book.getCount());
        intent.putExtra("charset", book.getCharset());
        intent.putExtra("id", book.getId());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Book book = (Book) adapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("移除书目")
                .setMessage("确定移除"+book.getName()+"?")
                .setNegativeButton("取消",null)
                .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.remove(book.getId());
                        try {
                            File file = new File(book.getPath());
                            file.delete();
                        }catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(),"文件删除失败!",Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.remove(book.getId());
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
        return true;
    }
}
