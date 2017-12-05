package com.oxchains.basicService.files.repo;

import com.oxchains.basicService.files.entity.FileAppInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
/**
 * Created by xuqi on 2017/12/5.
 */
@Repository
public interface FileAppInfoRepo extends CrudRepository<FileAppInfo,Integer>{
}
