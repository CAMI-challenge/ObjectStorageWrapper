package cami.objectstoragewrapper.swift;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import cami.objectstoragewrapper.core.IFile;
import cami.objectstoragewrapper.core.IFileManager;
import cami.objectstoragewrapper.core.S3Link;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectListOptions;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SwiftFileManager implements IFileManager {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String FOLDER_SUFFIX = "/";
    private static final String MD5_KEY = "Fingerprint";

    private final String bucketName;
    private final OSClient.OSClientV3 os;

    public SwiftFileManager(String bucketName, String username, String password, String url,
                            String projectId, String domain) {
        this(bucketName, username, password, url, projectId, domain, false);
    }

    public SwiftFileManager(String bucketName, String username, String password, String url,
                            String projectId, String domain, boolean logging) {
        this.bucketName = bucketName;
        OSFactory.enableHttpLoggingFilter(logging);
        os = OSFactory.builderV3()
                .endpoint(url)
                .credentials(username, password, Identifier.byName(domain))
                .scopeToProject(Identifier.byId(projectId))
                .authenticate();
    }

    public void createDirs(String path) {
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        os.objectStorage().objects().put(bucketName, path + FOLDER_SUFFIX, Payloads.create(emptyContent));
    }

    public List<IFile> list(String path) {
        ObjectListOptions options = ObjectListOptions.create();
        if (path != null && path.length() > 0) {
            options = options.startsWith(path);
        }
        return os.objectStorage().objects().list(bucketName, options).stream().map(SwiftFile::new)
                .collect(Collectors.toList());
    }

    public void uploadFile(String path, InputStream stream, Long length) {
        os.objectStorage().objects().put(bucketName, path, Payloads.create(stream));
    }

    public void uploadFile(String path, InputStream stream, Long length, String fingerprint) {
        Map<String, String> data = new HashMap<>();
        data.put(MD5_KEY, fingerprint);
        os.objectStorage().objects().put(bucketName, path, Payloads.create(stream),
                ObjectPutOptions.create().metadata(data));
    }

    public void delete(String path) {
        os.objectStorage().objects().delete(bucketName, path);
    }

    public String getMD5(String path) {
        Map<String, String> map = os.objectStorage().objects().getMetadata(bucketName, path);
        return map.get(MD5_KEY);
    }

    public Map<String, String> getObjectMetadata(String path) {
        return os.objectStorage().objects().getMetadata(bucketName, path);
    }

    public void downloadFile(String s3Link, File file) {
        S3Link link = new S3Link(s3Link);
        try {
            os.objectStorage().objects().download(link.getBucket(), link.getKey()).writeToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Date getTimeStamp(String path) {
        return os.objectStorage().objects().get(bucketName, path).getLastModified();
    }

    @Override
    public URL generateUrl(String s3Link) {
        //s3Link = "https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_PRIVATE/userinfo.png";
        Long expires = (System.currentTimeMillis() / 1000) + 300;
        String tempURL = null;
        try {
            tempURL = s3Link + "?temp_url_sig=" + calculateRFC2104HMAC("/v1/CAMI_PRIVATE/userinfo.png", "test") +
                    "&temp_url_expires=" + expires;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            return new URL(tempURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String calculateRFC2104HMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
