package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Implements the server side functionality of the services defined by the groups.proto gRpc contracts.
 */
@GrpcService
public class GroupsServerService extends GroupsServiceGrpc.GroupsServiceImplBase {

    /** For logging the requests related to groups. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The group repository for adding, deleting, updating and retrieving groups. */
    @Autowired
    private GroupRepository groupRepository;

    /** Provides helpful services for adding and removing users from groups. */
    @Autowired
    private GroupService groupService;

    /** The user repository for getting users. */
    @Autowired
    private UserRepository userRepository;


    /** GroupShortName Comparator */
    Comparator<Group> compareByShortName = Comparator.comparing(Group::getShortName);

    /** GroupLongName Comparator */
    Comparator<Group> compareByLongName = Comparator.comparing(Group::getLongName);

    /** GroupMemberNumber Comparator */
    Comparator<Group> compareByMemberNumber = Comparator.comparing(Group::getMembersNumber);


    /**
     * Follows the gRPC contract and provides the server side service for creating groups.
     *
     * @param request          A CreateGroupRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
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

    /**
     * Follows the gRPC contract and provides the server side service for adding members to groups.
     *
     * @param request          An AddGroupMembersRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void addGroupMembers(AddGroupMembersRequest request, StreamObserver<AddGroupMembersResponse> responseObserver) {
        logger.info("SERVICE - Adding users {} to group {}", request.getUserIdsList(), request.getGroupId());
        AddGroupMembersResponse.Builder response = AddGroupMembersResponse.newBuilder().setIsSuccess(true);
        try {
            groupService.addGroupMembers(request.getGroupId(), request.getUserIdsList());
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

    /**
     * Follows the gRPC contract and provides the server side service for removing members from groups.
     *
     * @param request          A RemoveGroupMembersRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void removeGroupMembers(RemoveGroupMembersRequest request, StreamObserver<RemoveGroupMembersResponse> responseObserver) {
        logger.info("SERVICE - Removing users {} from group {}", request.getUserIdsList(), request.getGroupId());
        RemoveGroupMembersResponse.Builder response = RemoveGroupMembersResponse.newBuilder().setIsSuccess(true);
        try {
            groupService.removeGroupMembers(request.getGroupId(), request.getUserIdsList());
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

     /**
     * Follows the gRPC contract and provides the server side service for modifying group details.
     *
     * @param request          A ModifyGroupDetailsRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        // log
        logger.info("SERVICE - modify group details for group with group id " + request.getGroupId());
        ModifyGroupDetailsResponse.Builder response = ModifyGroupDetailsResponse.newBuilder();
        // Do logic to populate response
        Optional<Group> optionalGroup = groupRepository.findById(request.getGroupId());
        if (optionalGroup.isPresent()) {
            try {
                logger.info("Group Modify Success - updated group details for group " + request.getGroupId());
                Group group = optionalGroup.get();

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
                    group.setShortName(request.getShortName());
                    group.setLongName(request.getLongName());
                    groupRepository.save(group);
                    response.setIsSuccess(true)
                            .setMessage("Successfully updated details for " + group.getShortName());
                }
            } catch (StatusRuntimeException e) {
                logger.error("An error occurred editing group from request: " + request + "\n See stack trace below \n");
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("Incorrect current password provided");
            }
        } else {
            logger.info("Group Edit Failure - could not find group with id " + request.getGroupId());
            response.setIsSuccess(false)
                    .setMessage("Could not find group to modify");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Follows the gRPC contract and provides the server side service for deleting groups.
     *
     * @param request          A DeleteGroupRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        logger.info("SERVICE - Deleting group {}", request.getGroupId());
        DeleteGroupResponse.Builder response = DeleteGroupResponse.newBuilder();
        if (groupRepository.existsById(request.getGroupId())) {
            logger.info("SERVICE - Successfully deleted the group with Id: {}", request.getGroupId());
            groupRepository.deleteById(request.getGroupId());
            response.setIsSuccess(true)
                    .setMessage("Successfully deleted the group with Id: " + request.getGroupId());
        } else {
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            response.setIsSuccess(false)
                    .setMessage("No group exists with Id: " + request.getGroupId());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Follows the gRPC contract and provides the server side service for getting group details.
     *
     * @param request          A GetGroupDetailRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting group {}", request.getGroupId());
        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder();
        // Checks that the group exists.
        if (groupRepository.existsById(request.getGroupId())) {
            Group group = groupRepository.getGroupById(request.getGroupId());
            List<UserResponse> userResponseList = new ArrayList<>();

            //Checks to see if there are members of the group.
            if (!group.getMemberIds().isEmpty()) {
                List<Integer> groupMembers = group.getMemberIds();
                for (int id : groupMembers) {
                    //For each group member Id that the group has, we want to create a UserResponse.
                    User user = userRepository.findById(id);
                    UserResponse userResponse = UserHelperService.retrieveUser(user);
                    userResponseList.add(userResponse);
                }
                // Iterates over the list of UserResponses and adds them to the response.
                for (UserResponse userResponse : userResponseList) {
                    response.addMembers(userResponse);
                }
            }
            //General setters for the response.
            response.setLongName(group.getLongName())
                    .setShortName(group.getShortName())
                    .setGroupId(group.getId()).build();
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } else {
            //If the group doesn't exist
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            response.setLongName("NOT FOUND");
            response.setShortName("");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }


    /**
     * Follows the gRPC contract for retrieving the paginated groups. Does this by sorting a list of all the groups based
     * on what was requested and then looping through to add the specific page of groups to the response
     *
     * @param request the GetPaginatedGroupsRequest passed through from the client service
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getPaginatedGroups(GetPaginatedGroupsRequest request, StreamObserver<PaginatedGroupsResponse> responseObserver) {
        super.getPaginatedGroups(request, responseObserver);

        PaginatedGroupsResponse.Builder reply = PaginatedGroupsResponse.newBuilder();
        //TODO: check is there a repository.findAll()?
        List<Group> allGroups = (List<Group>) groupRepository.findAll();
        String sortMethod = request.getOrderBy();

        switch (sortMethod) {
            //TODO: creat compareByShortname, compareByLongname, compareByMemberNumber
            case "shortname-increasing" -> allGroups.sort(compareByShortName);
            case "shortname-decreasing" -> {
                allGroups.sort(compareByShortName);
                Collections.reverse(allGroups);
            }
            case "longname-increasing" -> allGroups.sort(compareByLongName);
            case "longname-decreasing" -> {
                allGroups.sort(compareByLongName);
                Collections.reverse(allGroups);
            }
            case "MemberNumber-increasing" -> allGroups.sort(compareByMemberNumber);
            case "MemberNumber-decreasing" -> {
                allGroups.sort(compareByMemberNumber);
                Collections.reverse(allGroups);
            }
            default -> allGroups.sort(compareByShortName);
        }
        //for each group up to the limit or until all the users have been looped through, add to the response
        //TODO: creat GroupHelperService.retrieveGroup
        for (int i = request.getOffset(); ((i - request.getOffset()) < request.getLimit()) && (i < allGroups.size()); i++) {
            reply.addGroups(GroupHelperService.retrieveGroup(allGroups.get(i)));
        }
        reply.setResultSetSize(allGroups.size());
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Follows the gRPC contract and provides the server side service for getting the teaching group details.
     *
     * @param request          An empty request.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getTeachingStaffGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting teaching group");
        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder();
        try {
            Optional<Group> group = groupRepository.findByShortName("Teachers");
            groupResponseHelper(group, responseObserver, response);
        } catch (Exception err) {
            logger.error("SERVICE - Getting teaching group: {}", err.getMessage());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

     /**
     * Follows the gRPC contract and provides the server side service for getting the MWAG group details.
     *
     * @param request          An empty request.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getMembersWithoutAGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        {
            logger.info("SERVICE - Getting MWAG group");
            GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder();
            try {
                Optional<Group> group = groupRepository.findByShortName("Non-Group");
                groupResponseHelper(group, responseObserver, response);
            } catch (Exception err) {
                logger.error("SERVICE - Getting MWAG group: {}", err.getMessage());
                responseObserver.onNext(response.build());
                responseObserver.onCompleted();
            }
        }
    }

    /**
     * A helper method used to get the members of a group and build the list of UserResponse's.
     * Builds the response and returns it to the client.
     *
     * @param group the optional group, for which each member is added to the response.
     * @param responseObserver the client.
     * @param response the response to send to the client.
     */
    private void groupResponseHelper(Optional<Group> group, StreamObserver<GroupDetailsResponse> responseObserver,GroupDetailsResponse.Builder response) {
        List<UserResponse> userResponseList = new ArrayList<>();
        //Checks to see if there are members of the group.
        if (group.isPresent() && !group.get().getMemberIds().isEmpty()) {
            List<Integer> groupMembers = group.get().getMemberIds();
            for (int id : groupMembers) {
                //For each group member ID that the group has, we want to create a UserResponse.
                User user = userRepository.findById(id);
                UserResponse userResponse = UserHelperService.retrieveUser(user);
                userResponseList.add(userResponse);
            }
            // Iterates over the list of UserResponses and adds them to the response.
            for (UserResponse userResponse : userResponseList) {
                response.addMembers(userResponse);
            }
            //General setters for the response.
            response.setLongName(group.get().getLongName())
                    .setShortName(group.get().getShortName())
                    .setGroupId(group.get().getId()).build();
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }
}
