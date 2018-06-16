package cami.aws.userManagment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cami.aws.workspace.FileManagerFactory;
import cami.aws.workspace.interfaces.IFileManager;
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

public class CredentialsSimulationTest {

	public static final String VALID_UPLOAD = "junit/CamiAwsWrapper/CredentialSimulation/simulationTest/test";
	private static IFileManager manager;
	private S3CredentialsBuilder builder;

	private static String bucketName = "cami-test";
	private AmazonS3Client s3;

	@BeforeClass
	public static void getFileManager(){
		manager = FileManagerFactory.getAWSManager(bucketName);
	}

	@Before
	public void initialize() {
		builder = new S3CredentialsBuilder();
		
		StringCondition listCondition = new StringCondition(StringComparisonType.StringLike,"s3:prefix","junit/CamiAwsWrapper/CredentialSimulation/simulationTest/*");
		Condition[] conditions = new Condition[1];
		conditions[0] = listCondition;
		
		List<ResourceAction> list = new ArrayList<ResourceAction>();
		builder.addResourceAction(new ResourceAction(bucketName, "", S3Actions.ListObjects,conditions));
		builder.addResourceAction(new ResourceAction(bucketName, "/junit/CamiAwsWrapper/CredentialSimulation/simulationTest/*", S3Actions.AllS3Actions));
		ICredentials credentials = builder.getCredentials(129600, "b_ob");
		
		BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
				credentials.getAccessKey(), credentials.getSecretAccessKey(),
				credentials.getSessionToken());
		s3 = new AmazonS3Client(basicSessionCredentials);
	}
	
	@Test(expected=com.amazonaws.services.s3.model.AmazonS3Exception.class)
	public void illegalUploadTest() {
		s3.putObject(bucketName, "junit/CamiAwsWrapper/CredentialSimulation/illegalUpload/test", new File("test/resources/text.txt"));
	}
	
	@Test
	public void validUploadTest() {
		s3.putObject(bucketName, VALID_UPLOAD, new File("test/resources/text.txt"));
		int count = manager.list("junit/CamiAwsWrapper/CredentialSimulation/simulationTest/").size();
		assertEquals(1,count);
	}
	
	@Test
	public void listOwnBucketObjectsTest() {
		int count = manager.list("junit/CamiAwsWrapper/CredentialSimulation/listTest/").size();
		assertEquals(1, count);
	}
	
	@Test(expected=com.amazonaws.services.s3.model.AmazonS3Exception.class)
	public void listOtherBucketObjectsTest() {
		s3.listObjects(bucketName, "junit/CamiAwsWrapper/CredentialSimulation/illegalListSimulation/");
	}
	
	@Test(expected=com.amazonaws.services.s3.model.AmazonS3Exception.class)
	public void illegalDownloadTest() {
		s3.getObject(bucketName, "junit/CamiAwsWrapper/CredentialSimulation/illegalDownload/test");
	}

	@AfterClass
	public static void deleteLegalUpload(){
		manager.delete(VALID_UPLOAD);
	}

}
