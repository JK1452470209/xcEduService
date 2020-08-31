package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mr.JK
 * @create 2020-08-15  21:37
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
