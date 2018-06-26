package cami.objectstoragewrapper.aws;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cami.objectstoragewrapper.core.ICredentials;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetFederationTokenRequest;
import com.amazonaws.services.securitytoken.model.GetFederationTokenResult;

public class S3CredentialsBuilder {
    private AWSSecurityTokenService client;
    private List<Statement> statements = new ArrayList<>();

    public S3CredentialsBuilder() {
        client = AWSSecurityTokenServiceClient.builder()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    public S3CredentialsBuilder(String credentialsPath, String profile) {
        client = AWSSecurityTokenServiceClient.builder()
                .withCredentials(new ProfileCredentialsProvider(new ProfilesConfigFile(credentialsPath), profile))
                .build();
    }

    public S3CredentialsBuilder(String credentialsPath, String endpoint, String region) throws Exception {
        AWSCredentials credentials;
        try {
            credentials = new PropertiesCredentials(Paths.get(credentialsPath).toFile());
        } catch (IOException | IllegalArgumentException e) {
            throw new Exception("AWS credentials file could not be loaded.", e);
        }
        client = AWSSecurityTokenServiceClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
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
        GetFederationTokenResult federationTokenResult = client.getFederationToken(getFederationTokenRequest);
        Credentials sessionCredentials = federationTokenResult.getCredentials();
        statements.clear();
        return new cami.objectstoragewrapper.aws.Credentials(sessionCredentials);
    }

    public S3CredentialsBuilder addResourceAction(ResourceAction action) {
        Statement statement = new Statement(Effect.Allow);
        statement.withActions(action.getAction()).withResources(
                new Resource("arn:aws:s3:::" + action.getBucketName() + action.getPath()));
        if (action.isConditionAvailable()) {
            statement.withConditions(action.getCondition());
        }
        statements.add(statement);
        return this;
    }
}
