package cf.imxqd.ebook.activity;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import cf.imxqd.ebook.R;
import cf.imxqd.ebook.dao.Database;
import cf.imxqd.ebook.reader.TxtReader;

public class ReaderActivity extends AppCompatActivity implements View.OnLongClickListener
,View.OnClickListener{
    File file;
    long count;
    int id;
    TextView text, loading;
    ScrollView scrollView;
    String charset;
    String[] content;
    int page;
    Button next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }
        text = (TextView) findViewById(R.id.reader_text);
        loading = (TextView) findViewById(R.id.loading);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        next = (Button) findViewById(R.id.reader_next);
        next.setOnClickListener(this);
        text.setOnLongClickListener(this);
        text.setOnClickListener(this);
        file = new File(getIntent().getStringExtra("path"));
        count = getIntent().getLongExtra("count", 0);
        id = getIntent().getIntExtra("id", 0);
        charset = getIntent().getStringExtra("charset");
        page = getIntent().getIntExtra("page",0);

        assert charset != null:"ReaderActivity onCreate断言失败:charset为空";

        new Thread(new Runnable() {//开启新线程来加载文件，以防止阻塞UI线程
            @Override
            public void run() {
                loading(charset);
            }
        }).start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final Bundle data = msg.getData();
            if(data != null)
            {
                content = data.getStringArray("text");
                if (content != null) {
                    if(content.length > page)
                    {
                        text.setText(content[page]);
                        loading.setVisibility(View.GONE);

                        if(content.length > 1 && page != content.length - 1)
                        {
                            next.setVisibility(View.VISIBLE);
                        }else{
                            next.setVisibility(View.GONE);
                        }
                        handler.post(new Runnable() {//新增UI线程来滑动ScollView,否则scrollTo无效？
                            @Override
                            public void run() {
                                scrollView.scrollTo(0, (int) count);
                            }
                        });
                    }
                }

                return true;
            }
            Toast.makeText(getApplicationContext(), "文件加载出错！", Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    final int PAGE_CHAR_OUNT = 100*1024;//单节的最大字符数量

    /**
     * 加载并分割字符串
     * @param encoding 文本文件编码
     */
    void loading(String encoding){
        try {
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putStringArray("text", TxtReader.getStrings(file,charset,PAGE_CHAR_OUNT));
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (UnsupportedEncodingException e) {
            handler.sendEmptyMessage(0);
        } catch (Exception e) {
            loading.setText(e.getMessage());
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onDestroy()
    {//退出时保存进度
        System.out.println("save ScrollY to database:" + scrollView.getScrollY());
        Database database = Database.newInstance(getApplicationContext());
        database.set(id, charset, scrollView.getScrollY(),page);
        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View v)
    {//切换文件编码
        new AlertDialog.Builder(this)
                .setTitle("切换编码")
                .setItems(new String[]{"GBK", "UTF-8"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            charset = "GBK";
                        } else if (which == 1) {
                            charset = "UTF-8";
                        }
                        loading.setVisibility(View.VISIBLE);
                        loading(charset);
                    }
                })
                .show();
        return true;
    }

    void lastPage()
    {
        if(page <= content.length - 1 && page > 0)
        {
            page--;
            text.setText(content[page]);
            next.setVisibility(View.VISIBLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_UP);
                }
            });
        }else if(page == 0)
        {
            Toast.makeText(this,"已经是最前面了！",Toast.LENGTH_SHORT).show();
        }
    }

    void nextPage()
    {
        if(page == content.length - 1)
        {
            Toast.makeText(this,"已经没有了！",Toast.LENGTH_SHORT).show();
        }else if(page < content.length - 1 && page >= 0)
        {
            page++;
            text.setText(content[page]);
            next.setVisibility(View.VISIBLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_UP);
                }
            });
        }
    }

    void menu()
    {
        new AlertDialog.Builder(this)
                .setTitle("当前"+(page+1)+"/"+(content.length)+"节")
                .setItems(new String[]{"上一节", "节首", "节尾", "下一节"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                                lastPage();
                                break;
                            case 1:
                                scrollView.fullScroll(View.FOCUS_UP);
                                break;
                            case 2:
                                scrollView.fullScroll(View.FOCUS_DOWN);
                                break;
                            case 3:
                                nextPage();
                                break;
                            default:
                        }
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.reader_next:
                nextPage();
                break;
            case R.id.reader_text:
                menu();
                break;
            default:
        }
    }
}
