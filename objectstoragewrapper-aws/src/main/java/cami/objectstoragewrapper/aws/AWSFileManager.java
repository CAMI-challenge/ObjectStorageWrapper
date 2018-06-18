package cami.objectstoragewrapper.aws;

import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import cami.objectstoragewrapper.core.S3Link;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class AWSFileManager implements IFileManager {
    private static final String FOLDER_SUFFIX = "/";
    private static final String MD5_KEY = "fingerprint";
    private static final String HTTPS_HOST = "https.proxyHost";
    private static final String HTTPS_PORT = "https.proxyPort";

    private final AmazonS3 connection;
    private final String bucketName;

    public AWSFileManager(String bucketName) {
        this.bucketName = bucketName;
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        if (httpsHost != null && httpsPort != null) {
            Logger.getAnonymousLogger().info(httpsHost);
            Logger.getAnonymousLogger().info(httpsPort);
            clientConfiguration.setProxyHost(httpsHost);
            clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
        }
        connection = AmazonS3Client.builder()
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    public AWSFileManager(String bucketName, String credentialsPath) throws Exception {
        this.bucketName = bucketName;
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        if (httpsHost != null && httpsPort != null) {
            Logger.getAnonymousLogger().info(httpsHost);
            Logger.getAnonymousLogger().info(httpsPort);
            clientConfiguration.setProxyHost(httpsHost);
            clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
        }
        AWSCredentials credentials;
        try {
            credentials = new PropertiesCredentials(Paths.get(credentialsPath).toFile());
        } catch (IOException | IllegalArgumentException e) {
            throw new Exception("AWS credentials file could not be loaded.", e);
        }
        connection = AmazonS3Client.builder()
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "https://openstack.cebitec.uni-bielefeld.de:5000/v3/", "Bielefeld"))
                .build();
    }

    public AWSFileManager(String bucketName, String credentialsPath, String profile) {
        this.bucketName = bucketName;
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        if (httpsHost != null && httpsPort != null) {
            Logger.getAnonymousLogger().info(httpsHost);
            Logger.getAnonymousLogger().info(httpsPort);
            clientConfiguration.setProxyHost(httpsHost);
            clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
        }
        connection = AmazonS3Client.builder()
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new ProfileCredentialsProvider(new ProfilesConfigFile(credentialsPath), profile))
                .build();
    }

    public void createDirs(String path) {
        // Create metadata for your folder & set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        // Create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        // Create a PutObjectRequest passing the folder name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path + FOLDER_SUFFIX,
                emptyContent, metadata);
        // Send request to S3 to create folder
        connection.putObject(putObjectRequest);
    }

    public List<IFile> list(String path) {
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(bucketName);
        request.withPrefix(path).withDelimiter(path);
        ObjectListing objects = connection.listObjects(request);
        Set<IFile> set = new HashSet<>();
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                // filter out root
                if (!objectSummary.getKey().equals(path)) {
                    set.add(new AWSFile(objectSummary, path));
                }
            }
            objects = connection.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
        return new ArrayList<>(set);
    }

    public void uploadFile(String path, InputStream stream, Long length) {
        ObjectMetadata data = new ObjectMetadata();
        data.setContentLength(length);
        connection.putObject(new PutObjectRequest(bucketName, path, stream, data));
    }

    public void uploadFile(String path, InputStream stream, Long length, String fingerprint) {
        ObjectMetadata data = new ObjectMetadata();
        data.addUserMetadata(MD5_KEY, fingerprint);
        data.setContentLength(length);
        connection.putObject(new PutObjectRequest(bucketName, path, stream, data));
    }

    public void delete(String path) {
        for (S3ObjectSummary file : connection.listObjects(bucketName, path).getObjectSummaries()) {
            connection.deleteObject(bucketName, file.getKey());
        }
    }

    public String getMD5(String path) {
        return connection.getObject(new GetObjectRequest(bucketName, path)).getObjectMetadata().getETag();
    }

    public Map<String, String> getObjectMetadata(String path) {
        return connection.getObjectMetadata(bucketName, path).getUserMetadata();
    }

    public void downloadFile(String s3Link, File file) {
        TransferManager tm = new TransferManager(connection);
        S3Link link = new S3Link(s3Link);
        Download download = tm.download(link.getBucket(), link.getKey(), file);
        try {
            download.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("");
    }

    @Override
    public Date getTimeStamp(String path) {
        return connection.getObjectMetadata(bucketName, path).getLastModified();
    }

    @Override
    public URL generateUrl(String s3Link) {
        S3Link link = new S3Link(s3Link);
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        date = c.getTime();
        return connection.generatePresignedUrl(link.getBucket(), link.getKey(), date);
    }
}
