package cami.aws.workspace.impl;

import cami.aws.workspace.interfaces.IFile;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.Date;

public class AWSFile implements IFile{

	private Date timeStamp;
	private S3ObjectSummary summary;
	private String key;
	private String name;

	private AWSFile(){
	}
	
	protected AWSFile(S3ObjectSummary lSummary, String lKey){
		this.summary = lSummary;
		this.key = lKey;
		
		String wholeKey = summary.getKey();
		String name = wholeKey.substring(key.length()-1);
		String[] names = name.split("/");
		this.name = names[1];
		this.timeStamp = lSummary.getLastModified();
	}
	
	public String getPath() {
		return key;
	}

	public String getName() {
		return name;
	}

	@Override
	public Date getTimeStamp() {
		return this.timeStamp;
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
		AWSFile other = (AWSFile) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;

		return true;
	}
}