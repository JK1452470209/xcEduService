package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import jdk.management.resource.ResourceRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @author Mr.JK
 * @create 2020-08-20  21:24
 */
@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 检查文件块是否存在
     * @param fileMd5 文件md5
     * @param fileExt 文件扩展名
     * @param fileSize 文件大小
     * @return CheckChunkResult
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //1.检查文件在磁盘上是否存在
        //文件所属目录的路径
        String fileFloderPath = this.getFileFloderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //文件是否存在
        boolean exists = file.exists();

        //2.检查文件信息在MongoDB中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()){
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在时做一些准备工作，检查文件所在目录是否存在，如果不存在就创建
        File fileFloder = new File(fileFloderPath);
        if (!fileFloder.exists()){
            fileFloder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }



    /**
     * 分块检查
     * @param fileMd5 文件md5
     * @param chunk 块编号
     * @param chunkSize 块大小
     * @return CheckChunkResult
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件的所在目录
        String fileFloderPath = this.getFileFloderPath(fileMd5);
        //块文件
        File chunkFile = new File(fileFloderPath + chunk);
        if (chunkFile.exists()){
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,false);
        }

    }

    /**
     * 上传分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //检查分块目录，如果不存在则就要自动创建
        //得到分块目录
        String chunkFloderPath = this.getChunkFloderPath(fileMd5);


        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkFloderPath + chunk);
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取文件块路径
     * @param fileMd5
     * @return
     */
    private String getChunkFloderPath(String fileMd5) {
        //获取分块文件所属目录
        String fileFloderPath = this.getFileFloderPath(fileMd5);
        String chunkFloder = fileFloderPath + "chunk/";
        File fileChunkFloder = new File(chunkFloder);
        //如果分块所属目录不存在则创建
        if(!fileChunkFloder.exists()){
            fileChunkFloder.mkdirs();
        }
        return chunkFloder;
    }

    /**
     * 合并文件块
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //1.合并所有分块
        //得到分块文件所属目录
        String chunkFloderPath = this.getChunkFloderPath(fileMd5);
        File chunkFileFolder = new File(chunkFloderPath);

        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //创建一个合并文件
        String fileFullPath = this.getFileFullPath(fileMd5, fileExt);
        File mergeFile = new File(fileFullPath);

        //创建合并文件,如果存在则先删除再创建
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //执行合并
       mergeFile = this.mergeFile(fileList, mergeFile);
       if (mergeFile == null){
           //合并文件失败
           ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
       }

        //2.校验文件的md5值是否和前端传入的md5一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3.将文件的信息写入到MongoDB
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
//        mediaFile.setFileUrl(filePath + fileName + "." + fileExt);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);

        //向MQ发送视频处理消息
        this.sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送视频消息
     * @param mediaId 文件id
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //查询数据库mediaFIle
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //构造消息内容
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String jsonString = JSON.toJSONString(map);
        //想MQ发搜视频处理消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }


        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 校验文件
     * @param mergeFile
     * @param md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            //创建文件输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5DigestAsHex(inputStream);
            //和传入的md5比较
            if (md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 合并文件
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> chunkFileList,File mergeFile){
        try {
            //如果合并的文件已存在则删除，否则创建新文件
            if (mergeFile.exists()){
                mergeFile.delete();
            }else {
                //创建一个新文件
                mergeFile.createNewFile();
            }
            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;

                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            byte[] bytes = new byte[1024];
            for (File chunkFile:chunkFileList){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len = raf_read.read(bytes))!= -1){
                    raf_write.write(bytes,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return mergeFile;
    }



    /**
     * 根据文件md5得到文件的所属目录
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     */
    private String getFileFloderPath(String fileMd5){
        String floderPath = upload_location  + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5  + "/";
        return floderPath;
    }

    /**
     * 获取文件路径
     * 文件名：md5+文件扩展名
     */
    private String getFilePath(String fileMd5, String fileExt){
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "." + fileExt;

    }

    /**
     * 获取全文件路径
     * 文件名：md5+文件扩展名
     */
    private String getFileFullPath(String fileMd5, String fileExt){
        String floderPath = this.getFileFloderPath(fileMd5);
        String filePath = floderPath + fileMd5 + "." + fileExt;
        return filePath;
    }
}
