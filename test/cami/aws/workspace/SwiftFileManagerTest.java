package cami.aws.workspace;

import cami.aws.workspace.impl.SwiftFileManager;
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
public class SwiftFileManagerTest {

    @Test
    public void listFolders() {
        String username = "pbelmann";

        String url = "https://openstack.cebitec.uni-bielefeld.de:5000/v3/";
        String projectId = "2cd65880b6ad45479ac3fc076e623a4a";
        String domain = "Default";

        SwiftFileManager fileManager = new SwiftFileManager("CAMI_TEST", username, password, url, projectId, domain);
        fileManager.createDirs("test");

        String test = "test";
        InputStream is = new ByteArrayInputStream(test.getBytes());
        fileManager.uploadFile("test2/file", is, Long.valueOf(test.length()));
        fileManager.uploadFile("test2/file2", is, Long.valueOf(test.length()), "test_fingerprint");
        //fileManager.delete("test2/file2");
        String md5 = fileManager.getMD5("test2/file2");
        fileManager.getTimeStamp("test2/file2");
        fileManager.generateUrl("https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_TEST/test/tmux-client-14010.log");
        System.out.println(md5);
    }
}