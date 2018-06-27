package cami.objectstoragewrapper.main;

import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class FileManagerFactoryTest {
    private static final String TEST_FOLDER = "temp";
    private static final String TEST_FILE1 = TEST_FOLDER + "/test1.txt";
    private static final String TEST_FILE2 = TEST_FOLDER + "/test2.txt";
    private static final String TEST_SIGNATURE = "test-signature";

    @Test
    public void testSwiftFileManager() throws Exception {
        final String bucket = "ceph_api_test";
        final String username = "";
        final String password = "";
        IFileManager fileManager = null;
        //fileManager = FileManagerFactory.getSwiftManager(bucket, username, password,
        //        "https://openstack.cebitec.uni-bielefeld.de:5000/v3/", "3727bc448db74d748e72a03c5cdbcd72",
        //        "Default");
        fileManager = FileManagerFactory.getAWSManager(bucket, "D:\\.openstack.s3.properties",
                "https://openstack.cebitec.uni-bielefeld.de:8080", "Bielefeld");

        URL url = fileManager.generateUrl("s3://" + bucket + "/file.txt");
        System.out.println(url);

        fileManager.createDirs(TEST_FOLDER);
        assertTrue(pathExists(fileManager, TEST_FOLDER));
        // Test files upload with and without signature
        ClassLoader classLoader = getClass().getClassLoader();
        fileManager.uploadFile(TEST_FILE1, classLoader.getResource("text.txt").openStream(), 0L);
        assertTrue(pathExists(fileManager, TEST_FILE1));
        assertNull(fileManager.getMD5(TEST_FILE1));
        fileManager.uploadFile(TEST_FILE2, classLoader.getResource("text.txt").openStream(), 0L, TEST_SIGNATURE);
        assertTrue(pathExists(fileManager, TEST_FILE2));
        assertEquals(TEST_SIGNATURE, fileManager.getMD5(TEST_FILE2));
        // Test file delete
        fileManager.delete(TEST_FILE1);
        assertFalse(pathExists(fileManager, TEST_FILE1));
        fileManager.delete(TEST_FILE2);
        assertFalse(pathExists(fileManager, TEST_FILE2));
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
