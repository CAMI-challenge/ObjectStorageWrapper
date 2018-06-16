package cami.objectstoragewrapper.swift;

import cami.objectstoragewrapper.core.IFile;
import org.openstack4j.model.storage.object.SwiftObject;

import java.util.Date;

public class SwiftFile implements IFile {
    private final SwiftObject swiftObject;
    private final String name;
    private final String path;

    protected SwiftFile(SwiftObject object) {
        this.swiftObject = object;
        int index = object.getName().lastIndexOf('/');
        this.name = object.getName().substring(index + 1);
        this.path = object.getName();
    }

    @Override
    public String getPath() {
        return path;
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
        return path == null || name == null ? 0 : path.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        SwiftFile other = (SwiftFile) obj;
        if (path == null) {
            return other.path == null;
        }
        return path.equals(other.path);
    }
}
