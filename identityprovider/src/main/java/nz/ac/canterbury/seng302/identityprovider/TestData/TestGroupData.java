package nz.ac.canterbury.seng302.identityprovider.TestData;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestGroupData {

    /** The repository containing all users */
    @Autowired
    UserRepository userRepository;

    /** The repository containing all groups */
    @Autowired
    GroupRepository groupRepository;

    /** to log the adding of test data */
    Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Creates the two default groups, members without groups and teaching staff.
     * Loops through a list that contains every user and filters them into either nonGroupUsers or Teachers.
     * Saves both the groups to the repository.
     */
    public void addDefaultGroups() {
        logger.info("Creating default groups");
        Group teachingGroup = new Group(0, "Teachers", "Teaching Staff");
        Group nonGroupGroup = new Group(1, "Non-Group", "Members Without A Group");

        groupRepository.save(teachingGroup);
        groupRepository.save(nonGroupGroup);
        logger.info("Finished creating default groups");
    }


    /**
     * Creates six test groups, and saves them to the repository
     */
    public void addTestGroups() {
        logger.info("Creating test groups");
        Group groupOne = new Group(3, "Team 100", "Seng 302 Team 100");
        Group groupTwo = new Group(4, "Team 200", "Seng 302 Team 200");
        Group groupThree = new Group(5, "Team 300", "Seng 302 Team 300");
        Group groupFour = new Group(6, "Team 400", "Seng 302 Team 400");
        Group groupFive = new Group(7, "Team 500", "Seng 302 Team 500");
        Group groupSix = new Group(8, "Team 600", "The Best SENG 302 Team");

        groupRepository.save(groupOne);
        groupRepository.save(groupTwo);
        groupRepository.save(groupThree);
        groupRepository.save(groupFour);
        groupRepository.save(groupFive);
        groupRepository.save(groupSix);
        logger.info("Finished creating test groups");
    }


    /**
     * Adds 3 users to test group 3, and 2 users to test group 4.
     */
    public void addUsersToTestGroups() {
        ArrayList<User> groupThreeMembers = new ArrayList<>();
        groupThreeMembers.add(userRepository.findById(1));
        groupThreeMembers.add(userRepository.findById(2));
        groupThreeMembers.add(userRepository.findById(3));

        ArrayList<User> groupFourMembers = new ArrayList<>();
        groupFourMembers.add(userRepository.findById(3));
        groupFourMembers.add(userRepository.findById(4));

        Group group3 = groupRepository.getGroupById(3);
        group3.addGroupMembers(groupThreeMembers);
        groupRepository.save(group3);

        Group group4 = groupRepository.getGroupById(4);
        group4.addGroupMembers(groupFourMembers);
        groupRepository.save(group4);
    }

    public void setInitialTeachersAndMWAGGroupMembers() {
        logger.info("Adding Teacher and Members without a group to default groups");
        Group teachingGroup = groupRepository.getGroupById(0);
        Group nonGroupGroup = groupRepository.getGroupById(1);

        List<User> everyUserList = (List<User>) userRepository.findAll();
        List<User> teachers = new ArrayList<>();
        List<User> nonGroupUsers = new ArrayList<>();
        for (User user: everyUserList) {
            if (user.getRoles().contains(UserRole.TEACHER)) {
                teachers.add(user);
            } else {
                nonGroupUsers.add(user);
            }
        }

        teachingGroup.addGroupMembers(teachers);
        nonGroupGroup.addGroupMembers(nonGroupUsers);

        groupRepository.save(teachingGroup);
        groupRepository.save(nonGroupGroup);
        logger.info("Finished adding teacher and MWAG to default groups");
    }
}
