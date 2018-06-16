package cami.objectstoragewrapper.aws;

import cami.objectstoragewrapper.core.ICredentials;

class Credentials implements ICredentials {
    private com.amazonaws.services.securitytoken.model.Credentials credentials;

    private Credentials() {
    }

    Credentials(com.amazonaws.services.securitytoken.model.Credentials lCredentials) {
        this.credentials = lCredentials;
    }

    public String getAccessKey() {
        return this.credentials.getAccessKeyId();
    }

    public String getSecretAccessKey() {
        return this.credentials.getSecretAccessKey();
    }

    public String getSessionToken() {
        return this.credentials.getSessionToken();
    }
}
