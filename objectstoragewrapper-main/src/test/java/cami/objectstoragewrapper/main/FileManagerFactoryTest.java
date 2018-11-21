package cami.objectstoragewrapper.main;

import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URI;

import static org.junit.Assert.*;

// clashing when run with webFramework

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.apache.log4j.Logger;
// import org.apache.log4j.LogManager;
// import org.apache.log4j.BasicConfigurator;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

public class FileManagerFactoryTest {
    private static final String TEST_FOLDER = "temp";
    private static final String TEST_FILE1 = TEST_FOLDER + "/test1.txt";
    private static final String TEST_FILE2 = TEST_FOLDER + "/test2.txt";
    private static final String TEST_SIGNATURE = "test-signature";

    // clashing when run with webFramework

    // private static final Logger LOG = LogManager.getLogger(FileManagerFactoryTest.class);
    // private static Logger logger = 
    // LogManager.getLogger( FileManagerFactoryTest.class );

    @Test
    public void testSwiftFileManager() throws Exception {
        final String bucket = "CAMI_TEST";
        final String bucket2 = "NEWPRIVATETEST";
        final String bucket3 = "NEWTEST";

        final String username = "";
        final String password = "";

        IFileManager fileManager = null;
        IFileManager fileManager2 = null;
        IFileManager fileManager3 = null;
        IFileManager fileManager4 = null;
        IFileManager fileManager5 = null;

        fileManager = FileManagerFactory.getAWSManager(bucket, "/home/gary/git/openstack.s3.properties",
                "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");
        fileManager2 = FileManagerFactory.getAWSManager(bucket2, "/home/gary/git/openstack.s3.properties",
                "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");
        fileManager3 = FileManagerFactory.getAWSManager(bucket3, "/home/gary/git/openstack.s3.properties",
                "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");
        fileManager4 = FileManagerFactory.getAWSManager("newbucket", "/home/gary/git/openstack.s3.properties", "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");
        fileManager5 = FileManagerFactory.getAWSManager("newbucket", "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");

        URL url = fileManager.generateUrl("s3://" + bucket + "/file.txt");
	URI uri = new URI(url.toString());

        System.out.println(fileManager.generateUrl("s3://" + bucket2 + "/"));
        System.out.println(fileManager.generateUrl("s3://" + bucket3 + "/"));

        fileManager.createDirs(TEST_FOLDER);
        assertTrue(pathExists(fileManager, TEST_FOLDER));

        // Test files upload with and without signature
        ClassLoader classLoader = getClass().getClassLoader();

	fileManager4.createBucket("newbucket");

	// S3 ACLs don't work properly for Swift
	// fileManager4.setBucketPublicACL("newbucket");

        fileManager5.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
        fileManager5.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
	// fileManager4.setBucketReadwriteACL("newbucket");
        fileManager4.setBucketContentsFullcontrolACL("newbucket");
        // expected to fail
        // fileManager5.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);

	fileManager4.createBucket("newbucket2");
	fileManager4.setBucketReadwriteACL("newbucket2");
	fileManager4.copyBucketContents("newbucket", "newbucket2");
	fileManager4.setBucketReadonlyACL("newbucket2");
	fileManager4.deleteBucket("newbucket");


	// fileManager4.createBucket("NEWBUCKET2");
	// fileManager4.setBucketReadonlyACL("NEWBUCKET2");
        // fileManager5.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);

        fileManager.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
        fileManager.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
	fileManager.setFileReadonlyACL(bucket, TEST_FILE1);
        fileManager.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
	fileManager.setFilePublicACL(bucket, TEST_FILE1);
        fileManager.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);


        assertTrue(pathExists(fileManager, TEST_FILE1));
        // assertNull(fileManager.getMD5(TEST_FILE1));
        fileManager.uploadFile(TEST_FILE2, classLoader.getResource("text.txt").openStream(), 0L, TEST_SIGNATURE);
        assertTrue(pathExists(fileManager, TEST_FILE2));
        assertEquals(TEST_SIGNATURE, fileManager.getMD5(TEST_FILE2));
        // Test file delete
        fileManager.delete(TEST_FILE1);
        assertFalse(pathExists(fileManager, TEST_FILE1));
        fileManager.delete(TEST_FILE2);
        assertFalse(pathExists(fileManager, TEST_FILE2));


	File file = new File("/home/gary/filein.txt");
	HttpPost post = new HttpPost(uri);
        post.setHeader("Accept", "application/json");
        // _addAuthHeader(post);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // fileParamName should be replaced with parameter name your REST API expect.
        builder.addPart("fileParamName", new FileBody(file));
        //builder.addPart("optionalParam", new StringBody("true", ContentType.create("text/plain", Consts.ASCII)));
        post.setEntity(builder.build());
        HttpResponse response = HttpClients.createDefault().execute(post);

        // Test folder delete
        fileManager.delete(TEST_FOLDER + "/");
        assertFalse(pathExists(fileManager, TEST_FOLDER));
    }

    private static boolean pathExists(final IFileManager fileManager, final String path) {
        boolean found = false;
        for (IFile file : fileManager.list("")) {
            if (file.getPath().equalsIgnoreCase(path) || file.getPath().equalsIgnoreCase(path + "/")) {
                found = true;
                break;
            }
        }
        return found;
    }
}
