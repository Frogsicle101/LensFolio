package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;

public class PrincipalAttributes {


    public static String getClaim(AuthState principal, String claimType) {
        return principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals(claimType))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
    }

    /**
     * Specific use of the getClaim method for returning a userId from their AuthState.
     *
     * @param principal - The AuthState gRPC message.
     * @return userId (int) - a user Id of the principal.
     */
    public static int getIdFromPrincipal(AuthState principal) {
        return Integer.parseInt(PrincipalAttributes.getClaim(principal, "nameid"));
    }

    public static UserResponse getUserFromPrincipal(AuthState principal, UserAccountsClientService userAccountsClientService) {
        // Get user from server
        int user_id = Integer.parseInt(PrincipalAttributes.getClaim(principal, "nameid"));
        GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder().setId(user_id).build();
        UserResponse user = userAccountsClientService.getUserAccountById(userRequest);
        return user;
    }

}
