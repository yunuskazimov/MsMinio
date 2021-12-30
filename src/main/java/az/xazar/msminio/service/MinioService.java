package az.xazar.msminio.service;

import az.xazar.msminio.model.MinioFileDto;

import java.io.InputStream;
import java.util.List;

public interface MinioService {

     MinioFileDto uploadFile(MinioFileDto request, Long userId, String folder);

     InputStream getObject(String filename) ;

     List<MinioFileDto> getListObjects() ;

     void deleteFile(String fileName) ;

}
