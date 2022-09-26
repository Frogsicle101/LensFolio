describe('Edit evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })

    it("Edit button show on my own evidence", () => {
        cy.get("#editEvidenceButton").should("be.visible")
    })

    it("Edit page pre-fill with info", () => {
        cy.get("#editEvidenceButton").click()
        cy.get("#evidenceCancelButton").click()
        cy.get("#editEvidenceButton").click()
        cy.get("#addOrEditEvidenceModal").find(".skillChip").should('have.length', 4)
        cy.get("#addOrEditEvidenceModal").find(".webLinkElement").should('have.length', 1)
        cy.get("#addOrEditEvidenceModal").find(".linkedUser").should('have.length', 1)
        cy.get(".evidenceCategoryTickIcon").should('be.visible')
    })
})