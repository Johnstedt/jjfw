package jjfw.auth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import jjfw.common.Config;

public class AWSBroker {

    /**
     * Should never be initiated.
     */
    private AWSBroker(){}

    protected static AWSStaticCredentialsProvider getCredentials() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                        Config.getAws("accessKey"),
                        Config.getAws("secretKey"))
        );
    }


}
