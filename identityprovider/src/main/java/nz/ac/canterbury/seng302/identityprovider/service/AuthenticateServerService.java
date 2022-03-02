package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.authentication.AuthenticationServerInterceptor;
import nz.ac.canterbury.seng302.identityprovider.authentication.JwtTokenUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc.AuthenticationServiceImplBase;

@GrpcService
public class AuthenticateServerService extends AuthenticationServiceImplBase{

    //ToDo connect these default values to the database instead

    private final int VALID_USER_ID = 1;
    private final String ROLE_OF_USER = "student"; // Puce teams may want to change this to "teacher" to test some functionality

    private JwtTokenUtil jwtTokenService = JwtTokenUtil.getInstance();

    /**
     * Attempts to authenticate a user with a given username and password. 
     */
    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
        AuthenticateResponse.Builder reply = AuthenticateResponse.newBuilder();

        // Attempt to retrieve user form the database
        try {
            User attemptedUser = DatabaseService.getUserfromDatabase(request.getUsername());
            if (request.getPassword().equals(attemptedUser.getPassword())) {

                String token = jwtTokenService.generateTokenForUser(
                        attemptedUser.getUsername(),
                        VALID_USER_ID,
                        attemptedUser.getFullName(),
                        ROLE_OF_USER);
                reply
                        .setEmail(attemptedUser.getEmail())
                        .setFirstName(attemptedUser.getFirstName())
                        .setLastName(attemptedUser.getLastName())
                        .setMessage("Logged in successfully!")
                        .setSuccess(true)
                        .setToken(token)
                        .setUserId(1)
                        .setUsername(attemptedUser.getUsername());
            } else {
                reply
                        .setMessage("Log in attempt failed: username or password incorrect")
                        .setSuccess(false)
                        .setToken("");
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (NullPointerException exception) {
            reply
                .setMessage(exception.getMessage())
                .setSuccess(false)
                .setToken("");
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * The AuthenticationInterceptor already handles validating the authState for us, so here we just need to
     * retrieve that from the current context and return it in the gRPC body
     */
    @Override
    public void checkAuthState(Empty request, StreamObserver<AuthState> responseObserver) {
        responseObserver.onNext(AuthenticationServerInterceptor.AUTH_STATE.get());
        responseObserver.onCompleted();
    }
}
