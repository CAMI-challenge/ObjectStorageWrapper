package cami.objectstoragewrapper.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cami.objectstoragewrapper.core.ICredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetFederationTokenRequest;
import com.amazonaws.services.securitytoken.model.GetFederationTokenResult;

public class S3CredentialsBuilder {
    public static String DELIMITER = "/";

    private AWSSecurityTokenServiceClient stsClient;
    private List<Statement> statements = new ArrayList<>();
    private final static Logger LOGGER = Logger.getLogger(S3CredentialsBuilder.class.getName());

    public S3CredentialsBuilder(String lCredentialsPath, String lProfile) {
        stsClient = new AWSSecurityTokenServiceClient(
                new ProfileCredentialsProvider(new ProfilesConfigFile(
                        lCredentialsPath), lProfile));
    }

    public S3CredentialsBuilder() {
        stsClient = new AWSSecurityTokenServiceClient(new EnvironmentVariableCredentialsProvider());
    }

    public ICredentials getCredentials(Integer duration, String userName) {
        GetFederationTokenRequest getFederationTokenRequest = new GetFederationTokenRequest();
        getFederationTokenRequest.setDurationSeconds(duration);
        getFederationTokenRequest.setName(userName);

        // Define the policy and add to the request.
        Policy policy = new Policy();
        Statement[] statementsArr = new Statement[statements.size()];
        for (int i = 0; i < statements.size(); i++) {
            statementsArr[i] = statements.get(i);
        }
        policy = policy.withStatements(statementsArr);
        getFederationTokenRequest.setPolicy(policy.toJson());

        // Get the temporary security credentials.
        GetFederationTokenResult federationTokenResult = stsClient
                .getFederationToken(getFederationTokenRequest);
        Credentials sessionCredentials = federationTokenResult.getCredentials();
        statements.clear();
        return new cami.objectstoragewrapper.aws.Credentials(sessionCredentials);
    }

    public S3CredentialsBuilder addResourceAction(ResourceAction action) {
        Statement statement = new Statement(Effect.Allow);
        statement.withActions(action.getAction()).withResources(
                new Resource("arn:aws:s3:::" + action.getBucketName()
                        + action.getPath()));

        if (action.isConditionAvailable()) {
            statement.withConditions(action.getCondition());
        }
        statements.add(statement);

        return this;
    }
}
