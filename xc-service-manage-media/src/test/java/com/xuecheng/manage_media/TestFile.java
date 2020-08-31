package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @author Mr.JK
 * @create 2020-08-20  20:07
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

    //测试文件分块
    @Test
    public void testChunk() throws Exception {
        File sourceFile = new File("D:\\Mr_JK\\19 微服务项目【学成在线】\\day13 在线学习 HLS\\资料\\lucene.avi");
        String chunkFileFolder = "D:\\Mr_JK\\19 微服务项目【学成在线】\\day13 在线学习 HLS\\资料\\chunk\\";
        long chunkFileSize = 1 * 1024 * 1024;
        //Math.ceil向上取整,例如 12.1=13,12.8=13
        long chunkNum = (long) Math.ceil((sourceFile.length() * 1.0) / chunkFileSize);

        //使用RandomAccessFile访问文件
        RandomAccessFile rafRead = new RandomAccessFile(sourceFile, "r");
        //缓冲区大小
        byte[] byte_cache = new byte[1024];
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File chunkFile = new File(chunkFileFolder + i);
            boolean newFile = chunkFile.createNewFile();
            if (newFile){
                //向分块文件中写入数据
                RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
                int len = -1;
                //读取到-1则表示读取完成
                len = rafRead.read(byte_cache);
                while (len != -1){
                    raf_write.write(byte_cache,0, len);
                    //读取到预期块大小时结束
                    if (chunkFile.length() >= chunkFileSize){
                        break;
                    }
                }
                raf_write.close();
            }
        }
        rafRead.close();
    }


    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("D:\\Mr_JK\\19 微服务项目【学成在线】\\day13 在线学习 HLS\\资料\\chunk\\");
        //合并文件
        File mergeFile = new File("D:\\Mr_JK\\19 微服务项目【学成在线】\\day13 在线学习 HLS\\资料\\lucene1.avi");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();
        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //分块列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
        // 从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        //合并文件
        for(File chunkFile:fileList){
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

}
