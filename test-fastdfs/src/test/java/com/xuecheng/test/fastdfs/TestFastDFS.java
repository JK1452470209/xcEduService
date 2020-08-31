package com.xuecheng.test.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {


    /**
     * 测试文件上传
     */
    @Test
    public void testFileUpload(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //连接 Tracker
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取 Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建 Storage Client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //向 Storage 服务器上传文件,拿到文件id
            String filePath = "d:/1.jpg";
            String fileId = storageClient1.upload_file1(filePath, "jpg", null);
            System.out.println("上传成功：" + fileId);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }


    /**
     * 文件信息查询
     */
    @Test
    public void TestFileInfoQuery(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //连接 Tracker
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取 Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建 Storage Client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);

            String fileId = "group1/M00/00/00/wKiShl832bOAJRyKAADSQOOqUko799.jpg";
            FileInfo fileInfo = storageClient1.query_file_info1(fileId);
            System.out.println("文件信息: "+ fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件下载
     */
    @Test
    public void TestFileDownload(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //连接 Tracker
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取 Storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建 Storage Client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);

            String fileId = "group1/M00/00/00/wKiShl832bOAJRyKAADSQOOqUko799.jpg";
            String saveToPath = "d:/1.jpg";
            //下载文件
            byte[] bytes = storageClient1.download_file1(fileId);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(saveToPath));
            fileOutputStream.write(bytes);
            System.out.println("下载成功! " + saveToPath);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
