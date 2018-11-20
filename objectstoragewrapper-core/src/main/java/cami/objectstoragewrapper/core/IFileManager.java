package cami.objectstoragewrapper.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;


public interface IFileManager {
    void createDirs(String path);

    List<IFile> list(String path);

    List<String> listFileNames(String path);

    void delete(String path);

    void uploadFile(String path, InputStream stream, Long length);

    void uploadFile(String path, InputStream stream, Long length, String fingerprint);

    String getMD5(String path);

    Map<String, String> getObjectMetadata(String path);

    Date getTimeStamp(String path);

    void downloadFile(String s3Link, File file);

    URL generateUrl(String s3Link);

    void setFileReadonlyACL(String bucketName, String keyName);

    void setFilePublicACL(String bucketName, String keyName);

    void setBucketReadonlyACL(String bucketName);

    void setBucketReadwriteACL(String bucketName);

    void setBucketPublicACL(String bucketName);

    void createBucket(String bucketName);

    void copyBucketContents(String sourceBucketName, String targetBucketName);

    void deleteBucket(String bucketName);

    void setBucketContentsFullcontrolACL(String bucketName);

    String getConnectionId();

    String generateSwiftURL(String link_method, long link_expiry, String link_key, String delim, String protocol, String host, String port, String link) throws Exception;

}
