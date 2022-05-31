package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * The main IdP application class using springboot.
 */
@SpringBootApplication
public class IdentityProviderApplication {

    /** Enables us to directly inject test users into the database*/
    @Autowired
    UserRepository repository;

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Turn on (true) to create the default admin account */
    private boolean includeAdminAccount = true;

    /** Turn on (true) to create the 1000 test accounts */
    private boolean includeTestData = true;

    /**
     * Initialises test data when the boolean variables are true
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        if (includeAdminAccount)
            addAdminAccount();
        if (includeTestData)
            addTestUsers();
    }


    /**
     * Main method see class documentation.
     * @param args - default main params
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }

    // ----------------------------------------- Test data ---------------------------------------------------

    /**
     * Adds the default admin user
     */
    private void addAdminAccount() {
        logger.info("Initialising Admin user");
        User admin = new User(
                "admin",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@gmail.com",
                TimeService.getTimeStamp()
        );
        admin.addRole(UserRole.COURSE_ADMINISTRATOR);
        repository.save(admin);
    }


    /**
     * Adds the 30 default test users
     */
    private void addTestUsers() {
        logger.info("Initialising test users");
        User tempUser = new User(
                "steve",
                "password",
                "Steve",
                "",
                "Steveson",
                "Stev",
                "My name is Steve. I am a teacher",
                "He/Him",
                "steve@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "Robert.abe1989",
                "thib2eCuTh",
                "Robert",
                "Martin",
                "Lawrence",
                "Rob",
                "Musicaholic. Proud problem solver. Travel practitioner. Writer. Internet trailblazer.",
                "He/Him",
                "kale.kovace6@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "finagle",
                "password",
                "Pamela",
                "Johnathon",
                "North",
                "Pam",
                "Wannabe internet fanatic. Entrepreneur. Evil troublemaker. Coffee guru. Freelance communicator. Total beer fan.",
                "She/Her",
                "johnathon2006@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Walter.harber",
                "doopoo2Ah",
                "Walter",
                "Dave",
                "Nightingale",
                "Walt",
                "Social media specialist. Amateur creator. Avid twitter fan. Friendly coffee buff. Proud explorer.",
                "He/Him",
                "clark1996@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "RonnieNick",
                "mobahz4Nae",
                "Ronnie",
                "Liam",
                "Hughes",
                "Ron",
                "Alcohol geek. Total communicator. Problem solver. Analyst. Incurable zombie fanatic.",
                "He/Him",
                "Ronnie1972@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Shirley.reilly",
                "mobahz4Nae",
                "Shirley",
                "Jade",
                "Snyder",
                "Shir",
                "Student. Hipster-friendly food buff. Incurable music nerd. Internet practitioner. Tv scholar.",
                "She/Her",
                "arch2001@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Katrina.Crawford",
                "gieheQui2",
                "Katrina",
                "Betty",
                "Crawford",
                "Kat",
                "Food ninja. Typical explorer. Award-winning coffee maven. Social media trailblazer. Freelance zombie scholar. Beer nerd. Introvert.",
                "She/Her",
                "keyon.moscis@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Eric.feest",
                "ruYahf0Oo",
                "Eric",
                "Matthew",
                "Brown",
                "Matt",
                "Food expert. Extreme internet aficionado. Typical problem solver. Web guru.",
                "He/Him",
                "robin_lin8@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "elia_Shirley",
                "joogh0Eyei",
                "Shirley",
                "Betty",
                "Swanson",
                "Shir",
                "Beer fanatic. Twitter enthusiast. Internet expert. Unapologetic web evangelist. Tv practitioner. Food fan.",
                "She/Her",
                "jarred1996@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Blackgod47",
                "joogh0Eyei",
                "Casey",
                "Dale",
                "Arroyo",
                "Case",
                "Introvert. Internet junkie. Hardcore food maven. Problem solver. Typical thinker.",
                "He/Him",
                "marianna2009@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "adrienne_m1987",
                "joogh0Eyei",
                "Irvin",
                "John",
                "Stuart",
                "Irv",
                "Avid writer. Social media guru. Web geek. Pop culture fan. Problem solver. Wannabe twitter junkie. Student.",
                "He/Him",
                "christ.bosc6@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "gileadite",
                "vee4eiTaeph",
                "Joseph",
                "Noah",
                "Haywood",
                "Jo",
                "Total zombieaholic. Lifelong beer lover. Food fan. Travel enthusiast. Alcohol evangelist. Incurable tv scholar. Amateur social media nerd.",
                "He/Him",
                "kenyon.volkm@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "maximo.bec1970",
                "kiboMoh6doo",
                "Debra",
                "Olivia",
                "Jones",
                "Deb",
                "Creator. Tv evangelist. Hardcore alcohol enthusiast. Avid web advocate. Entrepreneur. Award-winning twitter fanatic.",
                "She/Her",
                "alexys1979@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "marjory_kl1981",
                "Gah4leech8",
                "Lucille",
                "Emma",
                "Hurt",
                "Lucy",
                "Prone to fits of apathy. Certified internet maven. Zombie fanatic. Typical creator. Troublemaker. Travel lover.",
                "She/Her",
                "dolly.vander@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "giles",
                "po6ohth8Xi0",
                "Amy",
                "Charlotte",
                "Smith",
                "Ames",
                "Introvert. Friendly tv lover. Music enthusiast. Communicator. Incurable problem solver.",
                "She/Her",
                "gunner_croo@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "wendy_mohr",
                "pij7Eegahshei",
                "Stephen",
                "Oliver",
                "Acosta",
                "Steve",
                "Subtly charming troublemaker. Devoted student. Certified web enthusiast. Avid reader.",
                "He/Him",
                "baby_osinsk4@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "verda",
                "aFeivee2ae",
                "Curtis",
                "Elijah",
                "Cheney",
                "Curt",
                "Web specialist. Infuriatingly humble beer buff. Entrepreneur. Bacon maven. Food junkie. Certified organizer",
                "He/Him",
                "gwen_klock2@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "jamarcus",
                "eeKie1ooP",
                "Anthony",
                "James",
                "Look",
                "Tony",
                "Food scholar. Internet aficionado. Typical twitter enthusiast. Devoted student. Beer advocate.",
                "He/Him",
                "fern_kutc6@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "fidel",
                "ro1Hei2aet",
                "Michelle",
                "Amelia",
                "Mahaney",
                "Micky",
                "Subtly charming pop culture junkie. Certified twitter ninja. Student. Web fanatic.",
                "She/Her",
                "maynard.gaylo@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "abdullah",
                "Iengoh0bu",
                "Kenneth",
                "William",
                "Tillman",
                "Ken",
                "Pop culture junkie. Tv fanatic. Award-winning music lover. Problem solver. Coffee practitioner.",
                "He/Him",
                "kaden1973@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "immanuel.z1983",
                "ip0Aefai",
                "James",
                "Benjamin",
                "Rosa",
                "JJ",
                "Coffee fanatic. Incurable explorer. Future teen idol. Troublemaker. Tv evangelist. Proud beer maven.",
                "He/Him",
                "lewis1973@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "godfrey",
                "ungeeD6foch5",
                "John",
                "Lucas",
                "Fletcher",
                "Johnny",
                "Prone to fits of apathy. Passionate student. Professional beer buff. Unapologetic internet fanatic.",
                "He/Him",
                "kristian2014@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "tyler88",
                "jo4airie8Ie",
                "Michael",
                "Henry",
                "Caldwell",
                "Mike",
                "Thinker. Freelance zombie fanatic. Tv trailblazer. Writer. Infuriatingly humble troublemaker.",
                "He/Him",
                "julian2013@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "rubie.ocon2003",
                "lohBeRio3",
                "William",
                "Theodore",
                "Meier",
                "Will",
                "Travel maven. Music fanatic. Hardcore writer. Analyst. Friendly coffee junkie. Food guru.",
                "He/Him",
                "ernest_mill0@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Josette",
                "eth2faaHief",
                "Josette",
                "Amelia",
                "Schrum",
                "Jo",
                "Organizer. Incurable troublemaker. Typical internetaholic. Explorer. Introvert. Social media trailblazer.",
                "She/Her",
                "candace.herz@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "milan_stol1996",
                "waiKai8u",
                "Ray",
                "Liam",
                "Anderson",
                "Andy",
                "Introvert. Beer enthusiast. Falls down a lot. Pop culture scholar. Hipster-friendly music advocate.",
                "He/Him",
                "madonna.pri@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "murl",
                "aiFahxei9gah",
                "Diane",
                "Isabella",
                "Bishop",
                "Dee",
                "Proud beeraholic. Unapologetic pop culture advocate. Tv lover. Hardcore zombie enthusiast. Problem solver. Creator.",
                "She/Her",
                "antwan.herm@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "sagedaniel",
                "iPeeW7iemae",
                "Matthew",
                "Noah",
                "Richard",
                "Matt",
                "Bacon specialist. Coffee ninja. Internet guru. Friendly tv fan. Twitter fanatic. Subtly charming social media advocate. Pop culture geek.",
                "He/Him",
                "roberto.boe@hotmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "singlehandedly",
                "eigie3Tue",
                "Dorothy",
                "Sophia",
                "Smallwood",
                "Sophie",
                "Infuriatingly humble music evangelist. Evil web trailblazer. Explorer. Social media nerd.",
                "She/Her",
                "king1987@yahoo.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "lavonne.do1975",
                "uNe7naing",
                "Alexander",
                "Oliver",
                "Fuller",
                "Alex",
                "Friendly food junkie. Lifelong introvert. Student. Avid coffee scholar. Unapologetic travel specialist. Zombie buff.",
                "He/Him",
                "rosalia1975@gmail.com",
                TimeService.getTimeStamp()
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);


    }
}
