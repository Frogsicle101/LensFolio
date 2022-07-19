package nz.ac.canterbury.seng302.portfolio.authentication;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import java.security.Principal;

public class Authentication implements Principal {


    private AuthState authState;

    public Authentication(AuthState authState) {
        this.authState = authState;
    }

    @Override
    public String getName() {
        System.out.println(authState.getName());
        return authState.getName();
    }

    public AuthState getAuthState() {
        return authState;
    }
}
