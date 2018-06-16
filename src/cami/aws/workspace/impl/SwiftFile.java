package cami.aws.workspace.impl;

import cami.aws.workspace.interfaces.IFile;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.openstack4j.model.storage.object.SwiftObject;

import java.util.Date;

public class SwiftFile implements IFile{


	private final Date timeStamp;
	private final SwiftObject swiftObject;
	private final String name;
	private final String key;

	protected SwiftFile(SwiftObject object){
//		this.summary = lSummary;
//		this.key = lKey;
//
//		this.object = object;
//		String wholeKey = summary.getKey();
//		String name = wholeKey.substring(key.length()-1);
//		String[] names = name.split("/");
//		this.name = names[1];
		this.swiftObject = object;
		this.name = object.getName();
		this.key = "test";
		this.timeStamp = object.getLastModified();
	}

	@Override
	public String getPath() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Date getTimeStamp() {
		return this.swiftObject.getLastModified();
	}

	@Override
	public int hashCode() {
	  if(key == null || name == null){
		  return 0;
	  }
      return key.hashCode() + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		SwiftFile other = (SwiftFile) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;

		return true;
	}
}