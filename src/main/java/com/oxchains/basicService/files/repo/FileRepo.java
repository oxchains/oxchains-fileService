package com.oxchains.basicService.files.repo;

import com.oxchains.basicService.files.entity.FileInfos;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by xuqi on 2017/12/5.
 */
@Repository
public interface FileRepo extends CrudRepository<FileInfos,Long>{
    public void deleteByTfsFilename(String tfsFileName);

}
