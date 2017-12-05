package com.oxchains.basicService.files.tfsService.fileService;
import com.alibaba.dubbo.config.annotation.Service;
import com.oxchains.basicService.files.entity.FileInfos;
import com.oxchains.basicService.files.exception.SaveFileExecption;
import com.oxchains.basicService.files.repo.FileRepo;
import com.oxchains.basicService.files.tfsService.TFSService;
import com.taobao.common.tfs.DefaultTfsManager;
import com.taobao.common.tfs.packet.FileInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
/**
 * Created by xuqi on 2017/12/1.
 */
@Service(version = "1.0.0")
public class TFSServiceImpl implements TFSService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Resource
    private DefaultTfsManager tfsManager;
    @Resource
    private FileRepo fileRepo;
    @Resource
    HashOperations hashOperations;

    @Value("${file.service.redis.hk}")
    private String fileHK;
    @Value("${oxchains.file.size}")
    private Integer fileSize;
    @Value("${oxchains.large.file.size}")
    private Integer larfeFileSize;
    //将文件存入文件服务器 并返回一个新的文件name
    @Override
    public String saveTfsFile(FileInfos fileInfo) throws SaveFileExecption{
        try {
            if(this.checkFileInfos(fileInfo,fileSize)){
                byte[] bytes = fileInfo.getFile();
                String fileName = fileInfo.getFilename();
                String suffix = this.getFileSuffix(fileName);
                String tfsfileName = tfsManager.saveFile(bytes, null, suffix, true);
                if(tfsfileName != null){
                    fileInfo.setTfsFilename(tfsfileName);
                    fileRepo.save(fileInfo);
                    hashOperations.put(fileHK,tfsfileName,fileName);
                    LOG.info("应用 {} ,用户 {} ,存入图片 {}-->{}",fileInfo.getAppKey(),fileInfo.getUserId(),fileInfo.getFilename(),fileInfo.getTfsFilename());
                    return tfsfileName;
                }
            }
        } catch (Exception e) {
            LOG.error("save tfs file faild :{}",e.getMessage(),e);
            throw new SaveFileExecption("save tfs file faild :"+e.getMessage());
        }
        return null;
    }
    @Override
    public String saveTfsLargeFile(FileInfos fileInfos) throws SaveFileExecption{
        try {
            if(this.checkFileInfos(fileInfos,larfeFileSize)){
                byte[] bytes =fileInfos.getFile();
                String fileName = fileInfos.getFilename();
                String suffix = this.getFileSuffix(fileName);
                String tfsfileName = tfsManager.saveLargeFile(bytes, null, suffix,"abc");
                if(tfsfileName != null){
                    fileInfos.setTfsFilename(tfsfileName);
                    FileInfos save = fileRepo.save(fileInfos);
                    hashOperations.put(fileHK,tfsfileName,fileName);
                    LOG.info("应用 {} ,用户 {} ,存入图片 {}-->{}",fileInfos.getAppKey(),fileInfos.getUserId(),fileInfos.getFilename(),fileInfos.getTfsFilename());
                    return tfsfileName;
                }
            }
        } catch (Exception e) {
            LOG.error("save tfs large file faild : {}",e.getMessage(),e);
            throw new SaveFileExecption("save tfs large file faild : "+e.getMessage());
        }
        return null;
    }

    //从文件服务器读取文件
    @Override
    public FileInfos getTfsFile(String tfsFileName) throws SaveFileExecption{
        ByteArrayOutputStream outputStream = null;
        boolean result = false;
        try {
            outputStream = new ByteArrayOutputStream();
            result = tfsManager.fetchFile(tfsFileName, "", outputStream);
            if(result){
                FileInfos fileInfos = new FileInfos();
                fileInfos.setFile(outputStream.toByteArray());
                fileInfos.setTfsFilename(tfsFileName);
                String filename = (String) hashOperations.get(fileHK,tfsFileName);
                fileInfos.setFilename(filename);
                return fileInfos;
            }
        } catch (Exception e) {
            LOG.error("get tfs file {} faild :{}",tfsFileName,e.getMessage(),e);
            throw  e;
        }
        return null;
    }

    //从文件服务器删除文件
    @Override
    public boolean deleteTfsFile(String tfsFileName) throws Exception{
        boolean flag = false;
        try {
            flag = tfsManager.unlinkFile(tfsFileName, null);
            if(flag){
                fileRepo.deleteByTfsFilename(tfsFileName);
                hashOperations.delete(fileHK,tfsFileName);
            }
        } catch (Exception e) {
            LOG.error("delete tfs file error :{}",e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    //临时隐藏一个文件  0 可以展示  1 隐藏
    @Override
    public boolean hideTfsFile(String tfsFileName, int isHidden) throws Exception{
        boolean flag = false;
        try {
            flag = tfsManager.hideFile(tfsFileName, null, isHidden);
        } catch (Exception e) {
            LOG.error("hide tfs file error : {}",e.getMessage(),e);
            throw e;
        }
        return  flag;
    }
    //将文件名的后缀截取下来 例如 .jpg .png
    private String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
    private boolean checkFileInfos(FileInfos fileInfos,Integer fileSize) throws SaveFileExecption {
        if(fileInfos == null){
            LOG.info("save file faild,Causy by fileInfos is null");
            throw new SaveFileExecption("fileInfos is null");
        }
        if(fileInfos.getLength() > fileSize*1024*1024){
            throw new SaveFileExecption("file is too large,file is Should be less than "+fileSize+"M");
        }
        if(!StringUtils.isNotBlank(fileInfos.getFilename())){
            LOG.info("save file faild,Causy by filename is null");
            throw new SaveFileExecption("filename is null");
        }
        if(!StringUtils.isNotBlank(fileInfos.getFileFormat())){
            String suffix = fileInfos.getFilename().substring(fileInfos.getFilename().lastIndexOf("."));
            fileInfos.setFileFormat(suffix);
        }
        if(fileInfos.getAppKey() == null){
            LOG.info("save file faild,Causy by appKey is null");
            throw new SaveFileExecption("Sorry,This application is no permission to access");
        }
        if(fileInfos.getUserId() == null){
            LOG.info("save file faild,Causy by userId is null");
            throw  new SaveFileExecption("userId is null");
        }
        return true;
    }

}
