package cami.aws.workspace;

import cami.aws.workspace.impl.AWSFileManager;
import cami.aws.workspace.interfaces.IFileManager;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.util.logging.Logger;

public class FileManagerFactory {

	public final static String HTTPS_HOST = "https.proxyHost";

	public final static String HTTPS_PORT = "https.proxyPort";

	public final static String DELIMITER = "/";
	
	public static IFileManager getAWSManager(String credentialsPath, String profile, String bucketName){
		String httpsHost = System.getProperty(HTTPS_HOST);
		String httpsPort = System.getProperty(HTTPS_PORT);

		ClientConfiguration clientConfiguration = new ClientConfiguration();
		if (httpsHost != null && httpsPort != null) {
			Logger.getAnonymousLogger().info(httpsHost);
			Logger.getAnonymousLogger().info(httpsPort);
			clientConfiguration.setProxyHost(httpsHost);
			clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
		}
		AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider(new ProfilesConfigFile(credentialsPath),profile),
				clientConfiguration);
		return new AWSFileManager(s3Client,bucketName);
	}

	public static IFileManager getAWSManager(String bucketName){
		String httpsHost = System.getProperty(HTTPS_HOST);
		String httpsPort = System.getProperty(HTTPS_PORT);

		ClientConfiguration clientConfiguration = new ClientConfiguration();
		if (httpsHost != null && httpsPort != null) {
			clientConfiguration.setProxyHost(httpsHost);
			clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
		}
        AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider(), clientConfiguration);
		return new AWSFileManager(s3Client,bucketName);
	}
}
