package cami.aws.util;

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
        this.key = s3LinkSplit[3];
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }
}
