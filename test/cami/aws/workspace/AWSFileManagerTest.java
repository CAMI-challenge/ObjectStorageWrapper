package cami.aws.workspace;

import cami.aws.workspace.interfaces.IFile;
import cami.aws.workspace.interfaces.IFileManager;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AWSFileManagerTest {

    private final String ROOT = "junit/CamiAwsWrapper/AwsFileManager";
    private String bucketName = "cami-test";
    private IFileManager manager;

    @Before
    public void initialize() {
        manager = FileManagerFactory.getAWSManager(bucketName);
    }

    @Test
    public void listFolders() {
        List<IFile> list = manager.list(ROOT + "/list/");
        for (IFile file : list) {
            System.out.println(file.getName());
        }
        assertEquals(list.size(), 3);
    }

    @Test
    public void createDirs() {
        manager.createDirs(ROOT + "/createDelete/test.txt");

        List<IFile> list = manager.list(ROOT + "/createDelete/");
        for (IFile file : list) {
            System.out.println(file.getPath());
        }
        assertEquals(list.size(), 1);
    }

    @Test
    public void delete() {
        manager.delete(ROOT + "/createDelete/test.txt/");
        List<IFile> list = manager.list(ROOT + "/createDelete/");
        for (IFile file : list) {
            System.out.println(file.getPath());
        }
        assertEquals(list.size(), 0);
    }

    @Test
    public void canDeleteDirectory() {
        manager.createDirs(ROOT + "/delete/folder/folder_to_delete");
        String test = "test";
        InputStream is = new ByteArrayInputStream(test.getBytes());
        manager.uploadFile(ROOT + "/delete/folder/folder_to_delete/test.txt", is,
                Long.valueOf(test.length()));
        manager.delete(ROOT + "/delete/folder/folder_to_delete");
        List<IFile> list = manager.list(ROOT + "/delete/folder/folder_to_delete/");
        for (IFile file : list) {
            System.out.println(file.getPath());
        }
        assertEquals(list.size(), 0);
    }

    @Test
    public void stream() {
        String test = "test";
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(test.getBytes());
        manager.uploadFile(ROOT + "/upload/test.txt", is,
                Long.valueOf(test.length()));
        List<IFile> list = manager.list(ROOT + "/upload/");
        int size = list.size();
        manager.delete(ROOT + "/upload/test.txt");
        assertEquals(list.size(), size);
    }

    @Test
    public void getMD5() {
        String md5 = manager.getMD5(ROOT + "/md5/test.txt");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5);
    }

    @Test
    public void getMetadataTest() {
        Map<String, String> metadata = manager.getObjectMetadata(ROOT
                + "/metadata/test.txt");
        assertEquals(metadata.get("test"), "test");
    }

    @Test
    public void getTimeStampTest() {
        Date date1 = manager.getTimeStamp(ROOT + "/timestamp/test1.txt");
        Date date2 = manager.getTimeStamp(ROOT + "/timestamp/test2.txt");
        assertTrue(date2.after(date1));
    }

    @Test
    public void canDownload() throws IOException {
        File file = File.createTempFile("download", "test");
        manager.downloadFile("s3://" + bucketName + "/" + ROOT + "/download/test.txt",
                file);
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void canGenerateS3Link() throws IOException {
        URL url = manager.generateUrl("s3://" + bucketName + "/" + ROOT + "/download/test.txt");
        assertTrue(url.toString().startsWith("https://" + bucketName + ".s3.amazonaws.com/" + ROOT + "/download/test.txt"));
    }
}
