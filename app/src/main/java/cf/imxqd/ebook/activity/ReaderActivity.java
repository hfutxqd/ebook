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

        new Thread(new Runnable() {
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
        String result;
        try {
            result = readToString(file, encoding);
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putStringArray("text", split(result,PAGE_CHAR_OUNT));
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (UnsupportedEncodingException e) {
            handler.sendEmptyMessage(0);
        }
    }

    /**
     * 把文本文件完整读取成String
     * @param file 文件
     * @param encoding 编码
     * @return 完整的文件内容
     * @throws UnsupportedEncodingException
     */
    public String readToString(File file, String encoding) throws UnsupportedEncodingException {
        System.out.println("loading");
        Long filelength = file.length();     //获取文件长度
        char[] filecontent = new char[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in,encoding);
            reader.read(filecontent);
            reader.close();
            in.close();
        } catch (FileNotFoundException e) {
            loading.setText("文件不存在！");
        } catch (IOException e) {
            loading.setText("文件读取失败！");
        } catch (Exception e) {
            loading.setText("文件编码不支持！");
        }
        System.out.println("loaded");
        return new String(filecontent);//返回文件内容
    }

    @Override
    protected void onDestroy()
    {//退出时保存进度
        System.out.println("save ScrollY to database:" + scrollView.getScrollY());
        Database database = Database.newInstance(getApplicationContext());
        int maxcount = scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getHeight();
        float percent = scrollView.getScrollY() /(float) maxcount;
        System.out.println(percent);
        System.out.println(content.length);
        System.out.println(page);
        database.set(id, charset, scrollView.getScrollY(),page);
        super.onDestroy();
    }

    /**
     *
     * @param str 要分割的字符串
     * @param length 分割后单个小字符串的最大的长度
     * @return 分割后的字符串数组
     */
    public String[] split(String str, int length)
    {
        int count = str.length() / length + 1;
        System.out.println(count);
        String[] arr = new String[count];
        for(int i = 0; i < count; i++)
        {
            arr[i] = str.substring(i * length,
                    (i * length + length) < str.length()?(i * length + length): str.length());
        }
        return arr;
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
