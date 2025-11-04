package jjfw.security;

import org.jooq.DSLContext;

public interface AccountCreator {

    public String createAccountFromCognitoUser(UserAuthContext uac, DSLContext dsl);
}
