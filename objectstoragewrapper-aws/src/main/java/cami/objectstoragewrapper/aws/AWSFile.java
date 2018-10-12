package cami.objectstoragewrapper.aws;

import cami.objectstoragewrapper.core.IFile;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.Date;

public class AWSFile implements IFile {
    private final Date timestamp;
    private final S3ObjectSummary summary;
    private final String key;
    private final String name;

    protected AWSFile(S3ObjectSummary lSummary, String lKey) {
        this.summary = lSummary;
        this.key = lKey;

	/* In test cases, lkey is always passed in as null string, therefore substring below always fails
	Need to handle case where key is a directory */

        String wholeKey = summary.getKey();
	String name = wholeKey;
	if (wholeKey.substring(key.length() - 1) == "/") {
        	name = wholeKey.substring(0, key.length() - 1);
	}
        String[] names = name.split("/");
        this.name = names[names.length - 1];

        this.timestamp = lSummary.getLastModified();
    }

    public String getPath() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Date getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        return key == null || name == null ? 0 : key.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        AWSFile other = (AWSFile) obj;
        if (key == null) {
            return other.key == null;
        }
        return key.equals(other.key);
    }
}
