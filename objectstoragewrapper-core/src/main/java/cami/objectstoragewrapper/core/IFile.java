package cami.objectstoragewrapper.core;

import java.util.Date;

public interface IFile {
	String getPath();
	
	String getName();

	Date getTimeStamp();
}
