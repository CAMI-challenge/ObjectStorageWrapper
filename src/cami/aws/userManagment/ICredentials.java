package cami.aws.userManagment;

public interface ICredentials {

	public String getAccessKey();
	
	public String getSecretAccessKey();
	
	public String getSessionToken();
}
