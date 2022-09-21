describe('Skill creation', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })


    it("Should have skill chips display", () => {
        cy.get('#createEvidenceButton').click()
        cy.get("#evidenceName").invoke("val", "Writing Cypress Tests")
        cy.get("#evidenceDescription").invoke("val", "This is all automatically written and should pass!")
        cy.get("#skillsInput").type("test test1 test2")
        cy.get("#skillChipDisplay").find(".skillChip").should("have.length", 3)
        cy.get("#evidenceSaveButton").click()
    })


    it("Should delete a skill on delete press", () => {
        cy.get('#createEvidenceButton').click()
        cy.get("#evidenceName").invoke("val", "Writing Cypress Tests")
        cy.get("#evidenceDescription").invoke("val", "This is all automatically written and should pass!")
        cy.get("#skillsInput").type("test test1 test2")
        cy.get("#skillChipDisplay").find(".skillChip").should("have.length", 3)
        cy.get("#skillsInput").type("{del}")
        cy.get("#skillChipDisplay").find(".skillChip").should("have.length", 2)
        cy.get("#evidenceSaveButton").click()
    })


    it("Should be able to select a skill from the dropdown", () => {
        cy.get('#createEvidenceButton').click()
        cy.get("#evidenceName").invoke("val", "Writing Cypress Tests")
        cy.get("#evidenceDescription").invoke("val", "This is all automatically written and should pass!")
        cy.get("#skillsInput").type("T")
        cy.get(".ui-menu-item-wrapper").should("contain.text", "Test1")
    })
})


