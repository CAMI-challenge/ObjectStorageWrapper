package cami.aws.workspace.impl;

import cami.aws.util.S3Link;
import cami.aws.workspace.interfaces.IFile;
import cami.aws.workspace.interfaces.IFileManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;
import java.util.stream.Collectors;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.amazonaws.services.s3.internal.Constants.HMAC_SHA1_ALGORITHM;

public class SwiftFileManager implements IFileManager {

	private final static String FOLDER_SUFFIX = "/";
	private final static String MD5_KEY = "Fingerprint";
	private String bucketName;
	private OSClient.OSClientV3 os;

	public SwiftFileManager(String lBucketName, String username, String password, String url,
							String projectId, String domain) {
		bucketName = lBucketName;
		OSFactory.enableHttpLoggingFilter(true);
		os = OSFactory.builderV3()
				.endpoint(url)
				.credentials(username, password,  Identifier.byName(domain))
				.scopeToProject(Identifier.byId(projectId))
				.authenticate();
	}

	public void createDirs(String path) {
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
		os.objectStorage().objects().put(bucketName, path + FOLDER_SUFFIX, Payloads.create(emptyContent));
	}

	public List<IFile> list(String path) {
		return os.objectStorage().objects().list(bucketName).stream().map(obj -> new SwiftFile(obj)).collect(Collectors.toList());
	}

	public void uploadFile(String path, InputStream stream, Long length) {
		os.objectStorage().objects().put(bucketName, path, Payloads.create(stream));
	}

	public void uploadFile(String path, InputStream stream, Long length, String fingerprint) {
		Map<String, String> data = new HashMap<>();
		data.put(MD5_KEY,fingerprint);
		os.objectStorage().objects().put(bucketName, path,
				Payloads.create(stream),
				ObjectPutOptions.create()
						.metadata(data)
		);
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
	public Date getTimeStamp(String path){
		return  os.objectStorage().objects().get(bucketName, path).getLastModified();
	}

	private static String toHexString(byte[] bytes) {
		Formatter formatter = new Formatter();

		for (byte b : bytes) {
			formatter.format("%02x", b);
		}

		return formatter.toString();
	}

	public static String calculateRFC2104HMAC(String data, String key)
			throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}

	@Override
	public URL generateUrl(String s3Link){
		//s3Link = "https://openstack.cebitec.uni-bielefeld.de:8080/swift/v1/CAMI_PRIVATE/userinfo.png";
		Long expires = (System.currentTimeMillis()/1000) + 300;
		String tempURL = null;
		try {
			tempURL = s3Link + "?temp_url_sig=" + calculateRFC2104HMAC("/v1/CAMI_PRIVATE/userinfo.png", "test") + "&temp_url_expires="+expires;
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		try {
			return new URL(tempURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
