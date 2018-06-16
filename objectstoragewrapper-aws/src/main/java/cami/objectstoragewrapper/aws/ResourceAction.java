package cami.objectstoragewrapper.aws;

import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.actions.S3Actions;

public class ResourceAction {
    private String bucketName;
    private String path;
    private S3Actions action;
    private Condition[] conditions;
    private Boolean isConditionAvailable;

    /**
     * @param bucketName
     * @param path       path to folder (Note: folder1/folder is different to folder1/folder/*)
     * @param action
     * @param condition
     */
    public ResourceAction(String bucketName, String path, S3Actions action, Condition[] condition) {
        this.bucketName = bucketName;
        this.path = path;
        this.action = action;
        this.conditions = condition;
        isConditionAvailable = condition != null;
    }

    /**
     * @param bucketName
     * @param path       path to folder (Note: folder1/folder is different to folder1/folder/*)
     * @param action
     */
    public ResourceAction(String bucketName, String path, S3Actions action) {
        this(bucketName, path, action, null);
    }

    public Condition[] getCondition() {
        return conditions;
    }

    public Boolean isConditionAvailable() {
        return isConditionAvailable;
    }

    public void setCondition(Condition[] conditions) {
        this.conditions = conditions;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public S3Actions getAction() {
        return action;
    }

    public void setAction(S3Actions action) {
        this.action = action;
    }
}
