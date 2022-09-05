describe('Test Editing Project', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')

    })

    it('Colours valid elements green', () => {
        cy.get("#editProject").click();
        cy.get("#projectName").should("have.css", "border-color", 'rgb(25, 135, 84)');
    })

    it('Colours invalid elements red', () => {
        cy.get("#editProject").click();
        cy.get("#projectName").clear();
        cy.get("#projectName").should("have.css", "border-color", 'rgb(220, 53, 69)');
    })
})

