package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.authentication.AuthenticationServerInterceptor;
import nz.ac.canterbury.seng302.identityprovider.authentication.JwtTokenUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc.AuthenticationServiceImplBase;
import org.springframework.beans.factory.annotation.Autowired;


@GrpcService
public class AuthenticateServerService extends AuthenticationServiceImplBase{

    private final String ROLE_OF_USER = "student"; // Puce teams may want to change this to "teacher" to test some functionality

    private JwtTokenUtil jwtTokenService = JwtTokenUtil.getInstance();

    @Autowired
    private UserRepository repository;

    /**
     * Attempts to authenticate a user with a given username and password. 
     */
    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {

        AuthenticateResponse.Builder reply = AuthenticateResponse.newBuilder();

        User foundUser = repository.findByUsername(request.getUsername());
        if (foundUser != null && foundUser.getPassword().equals(request.getPassword())) {

            String token = jwtTokenService.generateTokenForUser(
                    foundUser.getUsername(),
                    foundUser.getId(),
                    foundUser.getFirstName() + " " + foundUser.getLastName(),
                    ROLE_OF_USER
            );

            reply
                .setEmail(foundUser.getEmail())
                .setFirstName(foundUser.getFirstName())
                .setLastName(foundUser.getLastName())
                .setMessage("Logged in successfully!")
                .setSuccess(true)
                .setToken(token)
                .setUserId(1)
                .setUsername(foundUser.getUsername());
        } else {
            reply
            .setMessage("Log in attempt failed: username or password incorrect")
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
