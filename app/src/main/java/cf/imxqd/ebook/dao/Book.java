package cf.imxqd.ebook.dao;

/**
 * Created by Henry on 2015/11/27.
 * 书的数据模型
 */
public class Book {
    int id;
    String name;
    String path;
    String charset;
    int page;
    long count;
    public Book(String name, String path, String charset)
    {
        this.name = name;
        this.path = path;
        this.charset = charset;
        count= 0;
    }
    public Book()
    {
        
    }
    public void print()
    {
        System.out.println("Book Information:");
        System.out.println(name);
        System.out.println(charset);
        System.out.println(count);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

}
