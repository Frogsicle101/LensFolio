package nz.ac.canterbury.seng302.identityprovider.TestData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initialises the User and Group data used by the application.
 */
@Service
public class DataInitialisationManager {

    /** Turn on (true) to create the default admin account */
    private final boolean includeAdminAccount = true;

    /** Turn on (true) to create the 1000 test accounts */
    private final boolean includeTestUsers = true;

    /** Turn on (true) to create the test groups */
    private final boolean includeTestGroups = true;

    /** To add test users. */
    @Autowired
    TestUserData testUserData;

    /** To add test groups and the default groups. */
    @Autowired
    TestGroupData testGroupData;


    /**
     * Delegates the adding of test data, where data is required.
     */
    public void initialiseData() {
        testGroupData.addDefaultGroups();
        if (includeAdminAccount)
            testUserData.addAdminAccount();
        if (includeTestUsers)
            testUserData.addTestUsers();
        if (includeTestGroups)
            testGroupData.addTestGroups();
        if (includeTestUsers && includeTestGroups)
            testGroupData.addUsersToTestGroups();
    }
}
