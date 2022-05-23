package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;

import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GroupsClientService {

    /** The gRpc stub to make calls to the server service */
    @GrpcClient("groups-grpc-server")
    private GroupsServiceGrpc.GroupsServiceBlockingStub groupsStub;

    /** For logging the grpc requests related to groups */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * The grpc service to request the deletion of a group from the IdP
     * <br>
     * @param request - The request to delete a group following the DeleteGroupRequest message format
     * @return response - The IdP's response following the DeleteGroupResponse message format
     */
    public DeleteGroupResponse deleteGroup (DeleteGroupRequest request) {
        logger.info("SERVICE - send deleteGroupRequest request to server");
        return groupsStub.deleteGroup(request);
    }


    /**
     * The grpc service to request the creation of a group on the IdP
     * <br>
     * @param request - The request to create a group following the CreateGroupRequest message format
     * @return response - The IdP's response following the CreateGroupResponse message format
     */
    public CreateGroupResponse createGroup (CreateGroupRequest request) {
        logger.info("SERVICE - send createGroupRequest request to server");
        return groupsStub.createGroup(request);
    }


    /**
     * The grpc service to request the adding of a user to a group on the Idp
     *
     * @param request The request to add a user to a group, following the AddGroupMembersRequest message format
     * @return The IdP's response following the AddGroupMembersResponse message format
     */
    public AddGroupMembersResponse addGroupMembers (AddGroupMembersRequest request) {
        logger.info("SERVICE - send deleteGroupRequest request to server");
        return groupsStub.addGroupMembers(request);
    }


    /**
     * The grpc service to request the removal of a user from a group on the Idp
     *
     * @param request The request to remove a user from a group, following the AddGroupMembersRequest message format
     * @return The IdP's response following the RemoveGroupMembersResponse message format
     */
    public RemoveGroupMembersResponse removeGroupMembers (RemoveGroupMembersRequest request) {
        logger.info("SERVICE - send deleteGroupRequest request to server");
        return groupsStub.removeGroupMembers(request);
    }
}
