package cami.objectstoragewrapper.aws;

import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import cami.objectstoragewrapper.core.S3Link;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
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
// for test implementation of AWS temporary tokens
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.auth.STSSessionCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
// for test implmentation of ACLs 
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

// Clashing when run through webFramework

// import org.apache.log4j.Logger;
// import org.apache.log4j.LogManager;
// import org.apache.log4j.BasicConfigurator;







public class AWSFileManager implements IFileManager {
    private static final String FOLDER_SUFFIX = "/";
    private static final String MD5_KEY = "fingerprint";
    private static final String HTTPS_HOST = "https.proxyHost";
    private static final String HTTPS_PORT = "https.proxyPort";

    private final AmazonS3 connection;
    private final String bucketName;

    // Clashing when run through webFramework

    // private static org.apache.log4j.Logger log4Jlogger =
    // org.apache.log4j.LogManager.getLogger( AWSFileManager.class );

    public AWSFileManager(String bucketName, String endpoint, String region) {
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
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public AWSFileManager(String bucketName, String credentialsPath, String endpoint, String region) throws Exception {
        this.bucketName = bucketName;
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        // use default signer type - specifying v3 or v4 causes trouble
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

// Test code for AWS temporary sessions

/*
	AWSSecurityTokenServiceClient sts_client = new AWSSecurityTokenServiceClient(credentials);
	sts_client.setEndpoint(endpoint);
	GetSessionTokenRequest session_token_request = new GetSessionTokenRequest();
	session_token_request.setDurationSeconds(7200); // optional.

	GetSessionTokenResult session_token_result = sts_client.getSessionToken(session_token_request);
	Credentials session_creds = session_token_result.getCredentials();

	BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
   	session_creds.getAccessKeyId(),
   	session_creds.getSecretAccessKey(),
   	session_creds.getSessionToken());
*/

        connection = AmazonS3Client.builder()
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .build();

		// Test code for AWS temporary sessions

                // .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))

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

        // Clashing when run through webFramework
        Logger.getAnonymousLogger().setLevel(Level.SEVERE);

        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                // filter out root
                if (!objectSummary.getKey().equals(path)) {
        	   	// Clashing when run through webFramework
		    	// log4Jlogger.debug(path);
	            	// Logger.getAnonymousLogger().log(Level.SEVERE, objectSummary.getKey());
                    	set.add(new AWSFile(objectSummary, objectSummary.getKey()));
                }
            }
            objects = connection.listNextBatchOfObjects(objects);
            // Clashing when run through webFramework
            // Logger.getAnonymousLogger().log(Level.SEVERE, "LOOP");

        } while (objects.isTruncated());
        return new ArrayList<>(set);
    }

    public void uploadFile(String path, InputStream stream, Long length) {
        ObjectMetadata data = new ObjectMetadata();
        data.setContentLength(length);
        connection.putObject(new PutObjectRequest(bucketName, path, stream, data));
    }

    public void setFileReadonlyACL(String bucketName, String keyName) {
        Collection<Grant> grantCollection = new ArrayList<Grant>();
	Grant grant1 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.Read);
	grantCollection.add(grant1);
	Grant grant2 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.ReadAcp);
	grantCollection.add(grant2);

        setFileACL(bucketName, keyName, grantCollection);
    }

    public void setFilePublicACL(String bucketName, String keyName) {
        Collection<Grant> grantCollection = new ArrayList<Grant>();
	Grant grant1 = new Grant(GroupGrantee.AllUsers, Permission.Write);
	grantCollection.add(grant1);
	Grant grant2 = new Grant(GroupGrantee.AllUsers, Permission.WriteAcp);
	grantCollection.add(grant2);

        setFileACL(bucketName, keyName, grantCollection);

    }


    public void setFileACL(String bucketName, String keyName, Collection<Grant> grantCollection) {
        AccessControlList objectAcl = connection.getObjectAcl(bucketName, keyName);
        objectAcl.getGrantsAsList().clear();
        objectAcl.getGrantsAsList().addAll(grantCollection);
        connection.setObjectAcl(bucketName, keyName, objectAcl);
    }

    public void setBucketReadonlyACL(String bucketName) {
        Collection<Grant> grantCollection = new ArrayList<Grant>();
	Grant grant1 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.Read);
	grantCollection.add(grant1);
	Grant grant2 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.ReadAcp);
	grantCollection.add(grant2);

        setBucketACL(bucketName, grantCollection);
    }

    public void setBucketReadwriteACL(String bucketName) {
        Collection<Grant> grantCollection = new ArrayList<Grant>();
	Grant grant1 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.FullControl);
	grantCollection.add(grant1);

        setBucketACL(bucketName, grantCollection);
    }

    public void setBucketPublicACL(String bucketName) {
        Collection<Grant> grantCollection = new ArrayList<Grant>();
	Grant grant1 = new Grant(new CanonicalGrantee(connection.getS3AccountOwner().getId()), Permission.FullControl);
	grantCollection.add(grant1);
	Grant grant2 = new Grant(GroupGrantee.AllUsers, Permission.Write);
	grantCollection.add(grant2);

        setBucketACL(bucketName, grantCollection);

    }

    public void setBucketACL(String bucketName, Collection<Grant> grantCollection) {
        AccessControlList bucketAcl = connection.getBucketAcl(bucketName);

        bucketAcl.getGrantsAsList().clear();
        bucketAcl.getGrantsAsList().addAll(grantCollection);
        connection.setBucketAcl(bucketName, bucketAcl);
    }

    public void uploadFile(String path, InputStream stream, Long length, String fingerprint) {
        ObjectMetadata data = new ObjectMetadata();
        data.addUserMetadata(MD5_KEY, fingerprint);
        data.setContentLength(length);
        connection.putObject(new PutObjectRequest(bucketName, path, stream, data));
    }

    public void createBucket(String bucketName) {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        connection.createBucket(createBucketRequest);
    }

    public void delete(String path) {
        for (S3ObjectSummary file : connection.listObjects(bucketName, path).getObjectSummaries()) {
            connection.deleteObject(bucketName, file.getKey());
        }
    }

    public String getMD5(String path) {
        return connection.getObject(new GetObjectRequest(bucketName, path)).getObjectMetadata().getUserMetaDataOf("fingerprint");
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
        c.add(Calendar.DATE, 1); // 24 hours
        // c.add(Calendar.MINUTE, 5); // 5 mins
        date = c.getTime();

	GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(link.getBucket(), link.getKey());
	generatePresignedUrlRequest.setExpiration(date);
	// Optional
	// generatePresignedUrlRequest.setContentType("multipart/form-data");
	// generatePresignedUrlRequest.setContentType("image/png");
	// generatePresignedUrlRequest.setContentMd5("d3b07384d113edec49eaa6238ad5ff00");
	// generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
	// generatePresignedUrlRequest.setMethod(HttpMethod.POST);

        return connection.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public void copyBucketContents(String sourceBucketName, String targetBucketName) {

	    String nextKey = null;
            CopyObjectRequest copyObjRequest = null;

	    ObjectListing objectListing = connection.listObjects(sourceBucketName);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    nextKey = objIter.next().getKey();
		    copyObjRequest = new CopyObjectRequest(sourceBucketName, nextKey, targetBucketName, nextKey);
                    connection.copyObject(copyObjRequest);
                }
    
                // If the bucket contains many objects, the listObjects() call
                // might not return all of the objects in the first listing. Check to
                // see whether the listing was truncated. If so, retrieve the next page of objects 
                // and delete them.
                if (objectListing.isTruncated()) {
                    objectListing = connection.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
           }
    }

    public void deleteBucket(String bucketName) {
	    ObjectListing objectListing = connection.listObjects(bucketName);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    connection.deleteObject(bucketName, objIter.next().getKey());
                }
    
                // If the bucket contains many objects, the listObjects() call
                // might not return all of the objects in the first listing. Check to
                // see whether the listing was truncated. If so, retrieve the next page of objects 
                // and delete them.
                if (objectListing.isTruncated()) {
                    objectListing = connection.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }

           }

	   connection.deleteBucket(bucketName);

     }


    public void setBucketContentsFullcontrolACL(String bucketName) {

        String nextKey = null;
        CopyObjectRequest copyObjRequest = null;

        Grantee grantee = new CanonicalGrantee(connection.getS3AccountOwner().getId());

        Collection<Grant> grantCollection = new ArrayList<Grant>();
        Grant grant1 = new Grant(grantee, Permission.FullControl);
        grantCollection.add(grant1);

	System.out.println(bucketName+"XXXXXXXXXXXX");
        ObjectListing objectListing = connection.listObjects(bucketName);
        while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    nextKey = objIter.next().getKey();

                    setFileACL(bucketName, nextKey, grantCollection);

                }

                // If the bucket contains many objects, the listObjects() call
                // might not return all of the objects in the first listing. Check to
                // see whether the listing was truncated. If so, retrieve the next page of objects 
                // and delete them.
                if (objectListing.isTruncated()) {
                    objectListing = connection.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }

           }
    }

   public String getConnectionId() {
	return connection.getS3AccountOwner().getId().toString();
   }

}
