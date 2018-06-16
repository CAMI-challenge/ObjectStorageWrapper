package cami.aws.workspace.interfaces;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IFileManager {

	public void createDirs(String path);
	
	public List<IFile> list(String path);

	public void delete(String path);
	
	public void uploadFile(String path, InputStream stream, Long length);

	public void uploadFile(String path, InputStream stream, Long length, String fingerprint);

	public String getMD5(String path);

	public Map<String, String> getObjectMetadata(String path);

	public Date getTimeStamp(String path);

    public void downloadFile(String s3Link, File file);

	public URL generateUrl(String s3Link);
}
