package cf.imxqd.ebook.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文本读取和处理的过程与逻辑
 * Created by Henry on 2015/11/29.
 */
public class TxtReader {
    public static String[] getStrings(File file,String encoding, int length)
            throws IOException, OutOfMemoryError {
        String text = readToString(file,encoding);
        return split(text, length);
    }
    final static long MAX_MEMORY = 50 * 1024 * 1024l;
    /**
     * 把文本文件完整读取成String
     *
     * @param file 文件
     * @param encoding 编码
     * @return 完整的文件内容
     * @throws IOException
     */
    static String readToString(File file, String encoding)
            throws IOException, OutOfMemoryError{
        assert  file.exists() && file.isFile():"TxtReader断言失败：文件异常！";
        System.out.println("loading");
        Long filelength = file.length();     //获取文件长度

        if(filelength > MAX_MEMORY)
        {
            throw new IOException("文件过大！");
        }
        char[] filecontent = new char[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in,encoding);
            reader.read(filecontent);
            reader.close();
            in.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("文件不存在！");
        } catch (IOException e) {
            throw new IOException("文件读取失败！");
        }
        System.out.println("loaded");

        return new String(filecontent);//返回文件内容
    }

    /**
     *
     * @param str 要分割的字符串
     * @param length 分割后单个小字符串的最大的长度
     * @return 分割后的字符串数组
     */
    static String[] split(String str, int length)
    {
        assert str != null;
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
}
