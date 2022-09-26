describe('Edit evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })

    it("Edit button show on my own evidence", () => {
        cy.get("#editEvidenceButton").should("be.visible")
    })
})