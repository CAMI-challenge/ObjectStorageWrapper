package cami.objectstoragewrapper.swift;

import cami.objectstoragewrapper.core.IFile;
import org.openstack4j.model.storage.object.SwiftObject;

import java.util.Date;

public class SwiftFile implements IFile {
    private final SwiftObject swiftObject;
    private final String name;
    private final String key;

    protected SwiftFile(SwiftObject object) {
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
        return key == null || name == null ? 0 : key.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        SwiftFile other = (SwiftFile) obj;
        if (key == null) {
            return other.key == null;
        }
        return key.equals(other.key);
    }
}
