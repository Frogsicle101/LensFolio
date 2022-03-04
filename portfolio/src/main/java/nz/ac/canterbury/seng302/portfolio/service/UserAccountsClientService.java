package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.stereotype.Service;

/**
 * The UserAccountsClientServices class implements the functionality of the services outlined
 * by the user_accounts.proto gRPC contacts. This allows the client to make requests to the server
 * regarding their account.
 *
 * @author Sam Clark
 */
@Service
public class UserAccountsClientService {

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    /**
     * Sends a request to the UserAccountsServerService containing the id of a user, requesting the users account details.
     *
     * @param request - The request to send to the server, uses the GetUserByIDRequest message type defined by user_accounts.proto
     * @return response - The servers response to the request, which follows the UserResponse message format.
     */
    public UserResponse getUserAccountById(GetUserByIdRequest request) {
        return userAccountStub.getUserAccountById(request);
    }

}
