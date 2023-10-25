package thunder;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @program: Learning
 * @description:  下载任务类
 * @author: 作者
 * @create: 2023-07-19 19:16
 */
public class DownLoadTask implements Runnable{
    private Logger logger = Logger.getLogger(DownLoadTask.class);
    //第几个线程
    private final int i;
    //文件总大小
    private final long fileSize;
    //启动的线程总数
    private final long threadSize;
    //每个线程要下载的文件大小
    private final long sizePerThread;
    //要下载的文件url地址
    private final String url;
    //文件在本地的地址;
    private final String newFileAddress;
    //通知对象
    private thunder.Xunlei.Notify notify;


    public DownLoadTask(int i, long fileSize, long threadSize, long sizePerThread, String url, String newFileAddress, thunder.Xunlei.LengthNotify notify) {

        this.i = i;
        this.fileSize = fileSize;
        this.threadSize = threadSize;
        this.sizePerThread = sizePerThread;
        this.url = url;
        this.newFileAddress = newFileAddress;
        this.notify = notify;
    }


    @Override
    public void run() {
        //计算起始位置
        long start = i*sizePerThread;
        long end = (i+1)*sizePerThread-1;   //0:0-20   1:21-41  2:42-62
        RandomAccessFile raf = null;
        InputStream iis = null;
        try {
             raf = new RandomAccessFile(newFileAddress, "rw");
            raf.seek(start);
            //开始下载
            URL urlobj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10 * 1000);
            con.setRequestProperty("Range", "bytes=" + start + "-" + end);

            //要用到输入流
             iis = new BufferedInputStream(con.getInputStream());
            byte[] bs = new byte[10*1024];
            int length = -1;
            while ((length = iis.read(bs, 0, bs.length)) != -1) {
                raf.write(bs, 0, length);
                if(this.notify!=null){
                    this.notify.notifyResult(length);
                }
            }
            logger.info("线程"+i+"下载完毕");
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }finally {
            if(raf!=null){
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(iis!=null){
                try {
                    iis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
