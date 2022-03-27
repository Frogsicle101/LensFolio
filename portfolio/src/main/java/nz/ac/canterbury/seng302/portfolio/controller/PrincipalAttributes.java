package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;

public class PrincipalAttributes {

    public static int getId(AuthState principal) {
        return Integer.parseInt(getClaim(principal, "nameid"));
    }

    public static String getClaim(AuthState principal, String claimType) {
        return principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals(claimType))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
    }

    public static UserResponse getUserFromPrincipal(AuthState principal, UserAccountsClientService userAccountsClientService) {
        // Get user from server
        int user_id = Integer.parseInt(PrincipalAttributes.getClaim(principal, "nameid"));
        GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder().setId(user_id).build();
        return userAccountsClientService.getUserAccountById(userRequest);
    }

}
