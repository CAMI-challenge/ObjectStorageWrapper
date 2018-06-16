package cami.aws.userManagment;

import java.io.File;
import java.util.List;

import cami.aws.workspace.FileManagerFactory;
import cami.aws.workspace.interfaces.IFileManager;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.conditions.StringCondition;
import com.amazonaws.auth.policy.conditions.StringCondition.StringComparisonType;
import com.amazonaws.services.s3.AmazonS3Client;

public class CredentialsFactoryTest {

    private S3CredentialsBuilder builder;
    private static String uploadedFile = "junit/CamiAwsWrapper/CredentialsFactory/s3CredentialsTest/text.txt";
    private static String bucketName = "cami-test";
    private static IFileManager manager;

    @Before
    public void initialize() {
        builder = new S3CredentialsBuilder();
    }

    @BeforeClass
    public static void getFileManager(){
        manager = FileManagerFactory.getAWSManager(bucketName);
    }

    @Test
    public void getS3CredentialTest() {
        builder.addResourceAction(new ResourceAction(bucketName, "/junit/CamiAwsWrapper/CredentialsFactory/s3CredentialsTest/*", S3Actions.PutObject));
        ICredentials credentials = builder.getCredentials(3600, "bob");

        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                credentials.getAccessKey(), credentials.getSecretAccessKey(),
                credentials.getSessionToken());
        AmazonS3Client s3 = new AmazonS3Client(basicSessionCredentials);

        s3.putObject(bucketName, uploadedFile, new File("test/resources/text.txt"));

        int count = manager.list("junit/CamiAwsWrapper/CredentialsFactory/s3CredentialsTest/").size();
        assertEquals(count, 1);
    }

    @Test(expected = com.amazonaws.services.s3.model.AmazonS3Exception.class)
    public void getIllegalUploadTest() {
        builder.addResourceAction(new ResourceAction(bucketName, "/junit/CamiAwsWrapper/CredentialsFactory/illegalUploadTest/*", S3Actions.AllS3Actions));
        ICredentials credentials = builder.getCredentials(3600, "bob");

        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                credentials.getAccessKey(), credentials.getSecretAccessKey(),
                credentials.getSessionToken());
        AmazonS3Client s3 = new AmazonS3Client(basicSessionCredentials);



        s3.putObject(bucketName, "junit/CamiAwsWrapper/CredentialsFactory/s3CredentialsTest/text.txt", new File("test/resources/text.txt"));
    }

    @Test
    public void listOwnBucketObjectsTest() {

        StringCondition stringCondition = new StringCondition(StringComparisonType.StringLike, "s3:prefix", "junit/CamiAwsWrapper/CredentialsFactory/listOwnBucket/*");
        Condition[] conditions = new Condition[1];
        conditions[0] = stringCondition;

        builder.addResourceAction(new ResourceAction(bucketName, "", S3Actions.ListObjects,conditions));
        builder.addResourceAction(new ResourceAction(bucketName, "/junit/CamiAwsWrapper/CredentialsFactory/listOwnBucket/*", S3Actions.AllS3Actions));
        ICredentials credentials = builder.getCredentials(3600, "bob");

        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                credentials.getAccessKey(), credentials.getSecretAccessKey(),
                credentials.getSessionToken());
        AmazonS3Client s3 = new AmazonS3Client(basicSessionCredentials);

        ObjectListing list = s3.listObjects(bucketName, "junit/CamiAwsWrapper/CredentialsFactory/listOwnBucket/");

        List<S3ObjectSummary> summaryList = list.getObjectSummaries();

        int count = 0;
        for (S3ObjectSummary summary : summaryList) {
            count++;
        }
        assertEquals(count, 2);
    }

    @AfterClass
    public static void deleteUploadedFile() {
        manager.delete(uploadedFile);
    }
}
