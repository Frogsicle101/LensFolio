context("Repo settings page", () => {
    beforeEach(() => {
        cy.viewport(1920, 1024)
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#3').click()
    })

    it('Counts alias characters correctly', () => {
        cy.get("#pillsSettingsTab").click();
        cy.get(".editRepo").click();
        cy.get("#repoName").clear();
        cy.get(".countChar").should("contain.text", "100");

        for (let i = 1; i < 5; i++) {
            cy.get("#repoName").type("a");
            cy.get(".countChar").should("contain.text", (100 - i).toString(10));
        }
    })

})

