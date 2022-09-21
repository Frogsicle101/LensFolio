describe('Test Editing Project', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1').wait(500)
        cy.get("#editProject").click({force: true});
    })

    it('Colours valid elements green', () => {
        cy.get("#projectName").should("have.css", "border-color", 'rgb(25, 135, 84)');
    })

    it('Colours invalid elements red', () => {
        cy.get("#projectName").clear();
        cy.get("#projectName").should("have.css", "border-color", 'rgb(220, 53, 69)');
    })

    it('Does not steal focus', () => {
        cy.get("#projectEndDate").type("1999-12-01").trigger("change")
        cy.focused().should('have.id', 'projectEndDate')
    })

    it('Allows you to leave the description blank', () => {
        cy.get("#projectDescription").clear();
        cy.get("#projectDescription").should("have.css", "border-color", 'rgb(25, 135, 84)');
        cy.get("#editProjectSubmitButton").click();
        cy.url().should('include', 'portfolio')
    })
})

