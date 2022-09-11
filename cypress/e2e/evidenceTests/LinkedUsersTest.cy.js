describe('Link users to evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
        cy.get("#createEvidenceButton").click()
    })

    it("can display linking users input", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").should("be.visible")
    })

    it("can link users", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkedUsersTitle").should("be.visible")
        cy.get("#linkedUserId31").should("exist")
    })

    it("can't link same user twice", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkedUsersTitle").should("be.visible")
        cy.get("#linkedUserId31").should("exist")
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkedUsersTitle").should("be.visible")
        cy.get("#linkedUserId31").should('have.length', 1)
    })
})