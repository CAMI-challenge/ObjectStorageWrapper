package cami.objectstoragewrapper.main;

import cami.objectstoragewrapper.core.IFileManager;

public final class FileManagerFactory {
    public final static String HTTPS_HOST = "https.proxyHost";
    public final static String HTTPS_PORT = "https.proxyPort";
    public final static String DELIMITER = "/";

    private FileManagerFactory() {
    }

    public static IFileManager getAWSManager(String credentialsPath, String profile, String bucketName) {
        /*
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        if (httpsHost != null && httpsPort != null) {
            Logger.getAnonymousLogger().info(httpsHost);
            Logger.getAnonymousLogger().info(httpsPort);
            clientConfiguration.setProxyHost(httpsHost);
            clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
        }
        AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider(new ProfilesConfigFile(credentialsPath), profile),
                clientConfiguration);
        return new AWSFileManager(s3Client, bucketName);
        */
        return null;
    }

    public static IFileManager getAWSManager(String bucketName) {
        /*
        String httpsHost = System.getProperty(HTTPS_HOST);
        String httpsPort = System.getProperty(HTTPS_PORT);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        if (httpsHost != null && httpsPort != null) {
            clientConfiguration.setProxyHost(httpsHost);
            clientConfiguration.setProxyPort(Integer.parseInt(httpsPort));
        }
        AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider(), clientConfiguration);
        return new AWSFileManager(s3Client, bucketName);
        */
        return null;
    }
}
