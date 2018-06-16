package cami.objectstoragewrapper.main;

import cami.objectstoragewrapper.aws.AWSFileManager;
import cami.objectstoragewrapper.core.IFileManager;
import cami.objectstoragewrapper.swift.SwiftFileManager;

public final class FileManagerFactory {
    private FileManagerFactory() {
    }

    /**
     * Create a new AWS file manager instance with environment variable credentials.
     *
     * @param bucketName The bucket associated with the file manager.
     * @return {@link AWSFileManager} instance
     */
    public static IFileManager getAWSManager(String bucketName) {
        return new AWSFileManager(bucketName);
    }

    /**
     * Create a new AWS file manager instance with file based credentials and profile.
     *
     * @param bucketName      The bucket associated with the file manager.
     * @param credentialsPath AWS credentials file path for authentication.
     * @param profile         The credentials profile.
     * @return {@link AWSFileManager} instance
     */
    public static IFileManager getAWSManager(String bucketName, String credentialsPath, String profile) {
        return new AWSFileManager(bucketName, credentialsPath, profile);
    }

    /**
     * Create a new Swift file manager instance with the provided credentials.
     *
     * @param bucketName The bucket associated with the file manager.
     * @param username   Authentication username.
     * @param password   Authentication password.
     * @param url        Authentication url.
     * @param projectId  Project the bucket was created in.
     * @param domain     Project domain.
     * @return {@link SwiftFileManager} instance
     */
    public static IFileManager getSwiftManager(String bucketName, String username, String password, String url,
                                               String projectId, String domain) {
        return new SwiftFileManager(bucketName, username, password, url, projectId, domain);
    }
}
