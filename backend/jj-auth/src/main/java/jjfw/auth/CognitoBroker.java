package jjfw.auth;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import jjfw.common.Config;

public class CognitoBroker {
    private static final String USER_POOL_ID = Config.getCognito("userPoolId");
    private static final String INTEGRATION_CLIENT_ID = Config.getCognito("clientId");
    /**
     * Should never be initiated.
     */
    private CognitoBroker(){}

    private static AWSCognitoIdentityProvider getCognitoClient() {

        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(AWSBroker.getCredentials())
                .withRegion(Regions.EU_CENTRAL_1) // Frankfurt
                .build();
    }

    public static Object verifyUser(String accessToken) {
        try {
            AWSCognitoIdentityProvider cognitoClient = CognitoBroker.getCognitoClient();

            GetUserRequest authRequest = new GetUserRequest().withAccessToken(accessToken);
            return cognitoClient.getUser(authRequest);

        } catch (NotAuthorizedException ex) {
            if (ex.getErrorMessage().equals("Access Token has expired")) {
                return StatusResponse.Builder.newInstance()
                        .withStatus("Access Token has expired")
                        .withError(true)
                        .build();
            }
            else {
                return StatusResponse.Builder.newInstance()
                        .withStatus("exception during validation: {}" + ex.getMessage())
                        .withError(true)
                        .build();
            }
        } catch (TooManyRequestsException ex) {
            return StatusResponse.Builder.newInstance()
                    .withStatus("caught TooManyRequestsException, delaying then retrying")
                    .withError(true)
                    .build();
        }
    }

    public static void addUser(String user){
        AdminInitiateAuthResult result = getCognitoClient().adminInitiateAuth(new AdminInitiateAuthRequest()
                .addAuthParametersEntry("USERNAME", user)
                .addAuthParametersEntry("PASSWORD", "Development!9")
                .withUserPoolId(USER_POOL_ID)
                .withClientId(INTEGRATION_CLIENT_ID)
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        );

        RespondToAuthChallengeResult result1 = getCognitoClient().respondToAuthChallenge(new RespondToAuthChallengeRequest()
                .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .withClientId(INTEGRATION_CLIENT_ID)
                .addChallengeResponsesEntry("USERNAME", user)
                .addChallengeResponsesEntry("NEW_PASSWORD", "Development!8")
                .withSession(result.getSession())
        );
        System.out.println(result1);
    }

    public static String login(String user) {
        AdminInitiateAuthResult result = getCognitoClient().adminInitiateAuth(new AdminInitiateAuthRequest()
                .addAuthParametersEntry("USERNAME", user)
                .addAuthParametersEntry("PASSWORD", "Development!8")
                .withUserPoolId(USER_POOL_ID)
                .withClientId(INTEGRATION_CLIENT_ID)
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        );
        return result.getAuthenticationResult().getAccessToken();
    }

    public static void listUsers() {
        ListUsersResult s = CognitoBroker.getCognitoClient().listUsers(
                new ListUsersRequest().withUserPoolId(USER_POOL_ID));

        for (UserType i : s.getUsers()){
            System.out.println("user");
            for (AttributeType j : i.getAttributes()){
                System.out.println(j.getName() + " " + j.getValue());
            }
        }
    }
}
