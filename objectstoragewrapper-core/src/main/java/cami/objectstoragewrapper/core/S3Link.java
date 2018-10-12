package cami.objectstoragewrapper.core;

public class S3Link {
    private String bucket;
    private String key;

    public S3Link(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    public S3Link(String s3Link) {
        String[] s3LinkSplit = s3Link.split("/", 4);
        this.bucket = s3LinkSplit[2];
	if (s3LinkSplit.length == 4) {
          this.key = s3LinkSplit[3]; 
	} else {
          this.key = ""; 
	}
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }
}
