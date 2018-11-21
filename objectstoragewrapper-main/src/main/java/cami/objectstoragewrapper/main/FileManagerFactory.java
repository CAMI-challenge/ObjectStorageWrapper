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
    public static IFileManager getAWSManager(String bucketName, String endpoint, String region) {
        return new AWSFileManager(bucketName, endpoint, region);
    }

    /**
     * Create a new AWS file manager instance with file based credentials.
     *
     * @param bucketName      The bucket associated with the file manager.
     * @param credentialsPath AWS credentials file path for authentication.
     * @param endpoint        S3 endpoint to use.
     * @param region          Region to use.
     * @return {@link AWSFileManager} instance
     */
    public static IFileManager getAWSManager(String bucketName, String credentialsPath, String endpoint, String region)
            throws Exception {
        return new AWSFileManager(bucketName, credentialsPath, endpoint, region);
    }

    /**
     * Create a new AWS file manager instance with file based credentials and profile.
     *
     * @param bucketName      The bucket associated with the file manager.
     * @param credentialsPath AWS credentials file path for authentication.
     * @param profile         The credentials profile.
     * @return {@link AWSFileManager} instance
    public static IFileManager getAWSManager(String bucketName, String credentialsPath, String profile) {
        return new AWSFileManager(bucketName, credentialsPath, profile);
    }
     */

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

    /**
     * Create a new Swift file manager instance with the provided credentials.
     *
     * @param bucketName The bucket associated with the file manager.
     * @param username   Authentication username.
     * @param password   Authentication password.
     * @param url        Authentication url.
     * @param projectId  Project the bucket was created in.
     * @param domain     Project domain.
     * @param logging    Enable or disable logging of HTTP requests.
     * @return {@link SwiftFileManager} instance
     */
    public static IFileManager getSwiftManager(String bucketName, String username, String password, String url,
                                               String projectId, String domain, boolean logging) {
        return new SwiftFileManager(bucketName, username, password, url, projectId, domain, logging);
    }
}
