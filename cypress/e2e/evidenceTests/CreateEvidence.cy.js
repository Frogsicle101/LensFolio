describe('Create new piece of evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
    })

    it("Modal should display when button is clicked", () => {
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
        cy.get("#addEvidenceModal").should("not.be.visible")
        cy.get('.createEvidenceButton').click()
        cy.get("#addEvidenceModal").should("be.visible")
    })

    it("Should submit with just name and description", () => {
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
        cy.get('.createEvidenceButton').click()
        cy.get("#evidenceSaveButton").should("be.disabled")
        cy.get("#evidenceName").type("Writing Cypress Tests")
        cy.get("#evidenceSaveButton").should("be.disabled")
        cy.get("#evidenceDescription").type("This is all automatically written and should pass!")
        cy.get("#evidenceSaveButton").should("not.be.disabled")
        cy.get("#evidenceSaveButton").click()
        cy.get("#addEvidenceModal").should("not.be.visible")
    })
})
