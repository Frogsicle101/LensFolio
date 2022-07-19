package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    private final int MAX_SHORT_NAME_LENGTH = 50;
    private final int MAX_LONG_NAME_LENGTH = 100;
    private final int MIN_LENGTH = 1;

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

        int shortNameLength = request.getShortName().trim().length();
        int longNameLength = request.getLongName().trim().length();

        if (shortNameLength < MIN_LENGTH || shortNameLength > MAX_SHORT_NAME_LENGTH) {
            response.addValidationErrors(ValidationError.newBuilder()
                            .setFieldName("Short name")
                            .setErrorText("Group short name has to be between " + MIN_LENGTH + " and " +
                                    MAX_SHORT_NAME_LENGTH + " characters")
                            .build())
                    .setIsSuccess(false)
                    .setMessage("Error: A group short name has to be between " + MIN_LENGTH + " and " +
                            MAX_SHORT_NAME_LENGTH + " characters");
        }
        if (longNameLength < MIN_LENGTH || longNameLength > MAX_LONG_NAME_LENGTH) {
            response.addValidationErrors(ValidationError.newBuilder()
                            .setFieldName("Long name")
                            .setErrorText("Group long name has to be between " + MIN_LENGTH + " and " +
                                    MAX_LONG_NAME_LENGTH + " characters")
                            .build())
                    .setIsSuccess(false)
                    .setMessage("Error: A group long name has to be between " + MIN_LENGTH + " and " +
                            MAX_LONG_NAME_LENGTH + " characters");
        }
        if (groupRepository.findByShortName(request.getShortName()).isPresent()) {
            response.addValidationErrors(ValidationError.newBuilder()
                            .setFieldName("Short name")
                            .setErrorText("A group exists with the shortName " + request.getShortName())
                            .build())
                    .setIsSuccess(false)
                    .setMessage("Error: A group already exists with the short name " + request.getShortName());
        }
        if (groupRepository.findByLongName(request.getLongName()).isPresent()) {
            response.addValidationErrors(ValidationError.newBuilder()
                            .setFieldName("Long name")
                            .setErrorText("A group exists with the longName " + request.getLongName())
                            .build())
                    .setIsSuccess(false)
                    .setMessage("Error: A group already exists with the long name " + request.getLongName());
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
     * If the group exists and the new names (short and long) don't match existing names, the group
     * is updated and the response isSuccess is try, otherwise it is false.
     *
     * @param request A ModifyGroupDetailsRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - modify group details for group with group id " + request.getGroupId());
        ModifyGroupDetailsResponse.Builder response = ModifyGroupDetailsResponse.newBuilder();
        Optional<Group> optionalGroup = groupRepository.findById(request.getGroupId());
        if (optionalGroup.isPresent()) {
            try {
                Group group = optionalGroup.get();
                logger.info("Group Modify Success - updated group details for group " + request.getGroupId());

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
        GroupDetailsResponse response;
        // Checks that the group exists.
        if (groupRepository.existsById(request.getGroupId())) {
            Group group = groupRepository.getGroupById(request.getGroupId());
            response = group.groupDetailsResponse();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            //If the group doesn't exist
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
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
        PaginatedGroupsResponse.Builder reply = PaginatedGroupsResponse.newBuilder();

        List<Group> allGroups = (List<Group>) groupRepository.findAll();
        String sortMethod = request.getOrderBy();

        switch (sortMethod) {

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

        for (int i = request.getOffset(); ((i - request.getOffset()) < request.getLimit()) && (i < allGroups.size()); i++) {
            Group group = allGroups.get(i);
            reply.addGroups(group.groupDetailsResponse());
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
        GroupDetailsResponse response;
        try {
            Optional<Group> group = groupRepository.findByShortName("Teachers");
            if (group.isPresent()) {
                response = group.get().groupDetailsResponse();
            } else {
                response = GroupDetailsResponse.newBuilder().setGroupId(-1).build();
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception err) {
            logger.error("SERVICE - Getting teaching group: {}", err.getMessage());
            responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
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
            GroupDetailsResponse response;
            try {
                Optional<Group> group = groupRepository.findByShortName("Non-Group");
                if (group.isPresent()) {
                    response = group.get().groupDetailsResponse();
                } else {
                    response = GroupDetailsResponse.newBuilder().setGroupId(-1).build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception err) {
                logger.error("SERVICE - Getting MWAG group: {}", err.getMessage());
                responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
                responseObserver.onCompleted();
            }
        }
    }
}
