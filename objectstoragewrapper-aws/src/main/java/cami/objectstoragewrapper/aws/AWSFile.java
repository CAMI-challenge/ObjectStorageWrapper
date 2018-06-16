package cami.objectstoragewrapper.aws;

import cami.objectstoragewrapper.core.IFile;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.Date;

public class AWSFile implements IFile {
    private Date timestamp;
    private S3ObjectSummary summary;
    private String key;
    private String name;

    protected AWSFile(S3ObjectSummary lSummary, String lKey) {
        this.summary = lSummary;
        this.key = lKey;

        String wholeKey = summary.getKey();
        String name = wholeKey.substring(key.length() - 1);
        String[] names = name.split("/");
        this.name = names[1];
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
