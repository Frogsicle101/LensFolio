package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;


import java.util.ArrayList;

public class GroupHelperService {

    /**
     * Helper function to grab all the info from a specific Group and add it to a GroupResponse
     *
     * @param group Group passed through from the getPaginatedGroups method
     * @return GroupResponse - a response with all the info about the Group passed through
     */
    public static GroupDetailsResponse retrieveGroup(Group group) {
        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder();
        response.setShortName(group.getShortName())
                .setLongName(group.getLongName());
        // Todo add the members to the response

        return response.build();
    }
}
