package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class GroupsControllerTest {

    private AuthState teacher;
    private AuthState student;


    @BeforeEach
    public void beforeEach() {
        student = AuthState.newBuilder()
                .addClaims(ClaimDTO.newBuilder()
                        .setType("nameid")
                        .setValue("1")
                        .build())
                .addClaims(ClaimDTO.newBuilder()
                        .setType("role")
                        .setValue("student")
                        .build())
                .build();

        teacher = AuthState.newBuilder()
                .addClaims(ClaimDTO.newBuilder()
                        .setType("nameid")
                        .setValue("1")
                        .build())
                .addClaims(ClaimDTO.newBuilder()
                        .setType("role")
                        .setValue("course_administrator")
                        .build())
                .build();
    }



}
