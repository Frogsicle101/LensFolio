context("The browser sanitises sprints effectively", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
        cy.get(".sprintsContainer").find(".editSprint").first().click({force: true})
    })

    it('Sanitises sprint names', () => {
        cy.get("#sprintName").clear().type("<h1>Test</h1>")
        cy.get(".submitButton").click()
        cy.get(".sprint").first().find(".name").should("have.text", "<h1>")
    })

})
