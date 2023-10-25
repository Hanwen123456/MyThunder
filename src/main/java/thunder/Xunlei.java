package thunder;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: Learning
 * @description: 多线程下载文件
 * @author: 作者
 * @create: 2023-07-19 10:21
 */
public class Xunlei {

    private static final Logger logger = Logger.getLogger(thunder.Xunlei.class);  //日志对象
    private static long fileSize = 0;

//    volatile static int total = 0;
    static AtomicLong total = new AtomicLong(0L);//AtomicLong

    static class LengthNotify implements Notify{

        @Override
        public void notifyResult(long length) {
            total.addAndGet(length);
            System.out.println("下载了:"+total.get()+"字节");
        }
    }

    public static void main(String []args) throws IOException {
        String url = "http://pm.myapp.com/invc/xfspeed/qqpcmgr/download/QQPCDownload1530.exe";

        fileSize = getDownloadFileSize(url);
        logger.info("待下载的文件:"+url+"的大小为:"+fileSize+"字节");
        String newFileName = getFileName(url);

        String newFileAddress = getNewFileAddress(newFileName);
        //创建RandomAccessFile对象，设置文件大小
        RandomAccessFile raf = new RandomAccessFile(newFileAddress,"rw");
        raf.setLength(fileSize);
        raf.close();
        logger.info("文件已经创建,"+raf.getFD());

        int threadSize = Runtime.getRuntime().availableProcessors();
        long sizePerThread = getSizePerThread(fileSize,threadSize);  //每个线程下载多少字节

        LengthNotify lnt = new LengthNotify();
        for(int i=0;i<threadSize;i++){
            DownLoadTask task = new DownLoadTask(i,fileSize,threadSize,sizePerThread,url,newFileAddress,lnt);
            Thread t = new Thread(task);
            t.start();
        }

    }

    /**
     * 获得每个线程下载的字节数
     * @param fileSize
     * @param threadSize
     * @return
     */
    private static long getSizePerThread(Long fileSize,int threadSize){
        //多下了连接就会超时关闭
        return fileSize%threadSize==0?fileSize/threadSize:fileSize/threadSize+1;
    }


    /**
     * 返回文件存放地址
     * @param newFileName
     * @return
     */
    private static String getNewFileAddress(String newFileName) {
        String userhome = System.getProperty("user.home");
        return userhome+ File.separator + newFileName;
    }

    /**
     *根据url地址生成新的保存后的文件名
     * @param url
     * @return
     */
    private static String getFileName(String url) {
        //文件名
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = sdf.format(d);
        //后缀名
        String suffix = url.substring(url.lastIndexOf("."));
        String newFileName = fileName + suffix;
        return newFileName;
    }

    /**
     * 获取文件大小
     * @param url
     * @return
     */
    private static long getDownloadFileSize(String url) {
        long fileSize = 0;
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            //关键:  请求的方法:HEAD
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(10000);//超时
            con.connect();

            fileSize = con.getContentLength();
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return fileSize;

    }

    interface Notify{
        public void notifyResult(long length);
    }



}
