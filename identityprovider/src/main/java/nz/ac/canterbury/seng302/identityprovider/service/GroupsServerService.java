package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The GroupsServerService implements the server side functionality of the services defined by the
 * groups.proto gRpc contracts.
 */
@GrpcService
public class GroupsServerService extends GroupsServiceGrpc.GroupsServiceImplBase {

    /** For logging the requests related to groups */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The groups repository for adding, deleting, updating and retrieving groups */
    @Autowired
    private GroupRepository groupRepository;

    /** Provides helpful services for adding and removing users from groups */
    @Autowired
    private GroupService groupService;

    /** The user repository for getting users */
    @Autowired
    private UserRepository userRepository;

    private UserAccountsServerService userAccountsServerService;


    /**
     * Follows the gRPC contract and provides the server side service for creating groups
     * <br>
     * @param request - A CreateGroupRequest formatted to satisfy the groups.proto contract
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        logger.info("SERVICE - Creating group {}", request.getShortName());
        CreateGroupResponse.Builder response = CreateGroupResponse.newBuilder().setIsSuccess(true);
        if (groupRepository.findByShortName(request.getShortName()).isPresent()) {
            response.addValidationErrors(ValidationError.newBuilder()
                    .setFieldName("Short name")
                    .setErrorText("A group exists with the shortName " + request.getShortName())
                    .build())
                    .setIsSuccess(false);
        }
        if (groupRepository.findByLongName(request.getLongName()).isPresent()) {
            response.addValidationErrors(ValidationError.newBuilder()
                    .setFieldName("Long name")
                    .setErrorText("A group exists with the longName " + request.getLongName())
                    .build())
                    .setIsSuccess(false);
        }
        if (response.getIsSuccess()) {
            Group group = groupRepository.save(new Group(request.getShortName(), request.getLongName()));
            response.setNewGroupId(group.getId())
                    .setMessage("Created");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    @Override
    public void addGroupMembers(AddGroupMembersRequest request, StreamObserver<AddGroupMembersResponse> responseObserver) {

        AddGroupMembersResponse.Builder response = AddGroupMembersResponse.newBuilder().setIsSuccess(true);

        try {
            groupService.addUsersToGroup(request.getGroupId(), request.getUserIdsList());
            response.setIsSuccess(true)
                    .setMessage("Successfully added users to group")
                    .build();
        } catch (Exception e) {
            response.setIsSuccess(false)
                    .setMessage(e.getMessage())
                    .build();
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    @Override
    public void removeGroupMembers(RemoveGroupMembersRequest request, StreamObserver<RemoveGroupMembersResponse> responseObserver) {
        RemoveGroupMembersResponse.Builder response = RemoveGroupMembersResponse.newBuilder().setIsSuccess(true);

        try {
            groupService.removeUsersFromGroup(request.getGroupId(), request.getUserIdsList());
            response.setIsSuccess(true)
                    .setMessage("Successfully removed users from group")
                    .build();
        } catch (Exception e) {
            response.setIsSuccess(false)
                    .setMessage(e.getMessage())
                    .build();
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        // log
        ModifyGroupDetailsResponse.Builder response = ModifyGroupDetailsResponse.newBuilder();
        // Do logic to populate response
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Follows the gRPC contract and provides the server side service for deleting groups
     * <br>
     * @param request - A DeleteGroupRequest formatted to satisfy the groups.proto contract
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        logger.info("SERVICE - Deleting group {}", request.getGroupId());

        DeleteGroupResponse.Builder response = DeleteGroupResponse.newBuilder();
        if (groupRepository.existsById(request.getGroupId())) {
            logger.info("SERVICE - Successfully deleted the group with Id: " + request.getGroupId());
            groupRepository.deleteById(request.getGroupId());
            response.setIsSuccess(true)
                    .setMessage("Successfully deleted the group with Id: " + request.getGroupId());

        } else {
            logger.info("SERVICE - No group exists with Id: " + request.getGroupId());
            response.setIsSuccess(false)
                    .setMessage("No group exists with Id: " + request.getGroupId());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting group {}", request.getGroupId());

        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder();

        // Checks that the group exists.
        if (groupRepository.existsById(request.getGroupId())) {
            Group group = groupRepository.getGroupById(request.getGroupId());
            List<UserResponse> userResponseList = new ArrayList<>();


            if (!group.getMemberIds().isEmpty()){
                List<Integer> groupMembers = group.getMemberIds();
                for (int id: groupMembers) {

                    User user = userRepository.findById(id);
                    UserResponse userResponse = userAccountsServerService.retrieveUser(user);
                    userResponseList.add(userResponse);

                }

                for (UserResponse userResponse: userResponseList) {
                    response.addMembers(userResponse);
                }
            }


            response.setLongName(group.getLongName())
                    .setShortName(group.getShortName())
                    .setGroupId(group.getId()).build();
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } else {
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            response.setLongName("NOT FOUND");
            response.setShortName("");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPaginatedGroups(GetPaginatedGroupsRequest request, StreamObserver<PaginatedGroupsResponse> responseObserver) {
        super.getPaginatedGroups(request, responseObserver);
    }

    @Override
    public void getTeachingStaffGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        super.getTeachingStaffGroup(request, responseObserver);
    }

    @Override
    public void getMembersWithoutAGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        super.getMembersWithoutAGroup(request, responseObserver);
    }
}
