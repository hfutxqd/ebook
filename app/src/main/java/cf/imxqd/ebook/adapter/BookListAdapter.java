package cf.imxqd.ebook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import cf.imxqd.ebook.dao.Book;
import cf.imxqd.ebook.dao.Database;
import cf.imxqd.ebook.R;

/**
 * Created by Henry on 2015/11/27.
 * 书目列表的数据适配器
 */
public class BookListAdapter extends BaseAdapter {
    Database database = null;
    ArrayList<Book> booklist = null;

    @SuppressWarnings("deprecation")
    public BookListAdapter(Context context)
    {
        database = Database.newInstance(context);
        assert database != null:"断言失败：获取数据库对象失败";
        booklist = database.getList();
        assert booklist != null:"断言失败：从数据库获取失败";
    }
    @Override
    public int getCount() {
        return booklist.size();
    }

    @Override
    public Object getItem(int position) {
        return booklist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return booklist.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder itemHolder;
        Book book = booklist.get(position);
        if(convertView == null)
        {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.book_item, parent, false);
            itemHolder = new ItemHolder(convertView);
            convertView.setTag(itemHolder);
        }
        itemHolder = (ItemHolder) convertView.getTag();
        itemHolder.bookname.setText(book.getName());
        if(book.getCount() == 0)
        {
            itemHolder.image.setImageResource(R.drawable.circle_red);
        }else if(book.getCount() > 0 && book.getCount() < 5000){
            itemHolder.image.setImageResource(R.drawable.circle_orange);
        }else{
            itemHolder.image.setImageResource(R.drawable.circle_green);
        }
        File file = new File(book.getPath());
        if(file.exists() && file.isFile())
        {
            itemHolder.filesize.setText(computeFileSize(file.length()));
        }else{
            itemHolder.filesize.setText("已删除");
        }
        return convertView;
    }

    public static String computeFileSize(long size)
    {
        if(size < 1000)
        {
            return size+"B";
        }else if(size < 1024 * 1024)
        {

            return numFormat(size/1024.0)+"KB";
        }else if(size < 1024 * 1024 * 1024)
        {
            return numFormat(size/(1024.0*1024))+"MB";
        }else{
            return numFormat(size/(1024.0*1024))+"MB";
        }
    }

    public static String numFormat(double num)
    {
        return String.format("%.2f",num);
    }

    @Override
    public void notifyDataSetChanged() {
        booklist.clear();
        booklist.addAll(database.getList());
        super.notifyDataSetChanged();
    }

    static class ItemHolder{
        TextView bookname, filesize;
        ImageView image;
        ItemHolder(View root)
        {
            bookname = (TextView) root.findViewById(R.id.bookname);
            image = (ImageView) root.findViewById(R.id.imgage);
            filesize = (TextView) root.findViewById(R.id.file_size);
            assert bookname != null && image != null
                    && filesize != null:"ItemHolder断言失败：无法获取View";
        }
    }
}
