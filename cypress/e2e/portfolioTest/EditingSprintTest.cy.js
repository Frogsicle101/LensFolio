describe('Test Editing Sprint', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
        cy.get(".sprintsContainer").find(".editSprint").first().click({force: true});
    })

    it('Colours valid elements green', () => {
        cy.get("#sprintName").should("have.css", "border-color", 'rgb(25, 135, 84)');
    })

    it('Colours invalid elements red', () => {
        cy.get("#sprintName").clear();
        cy.get("#sprintName").should("have.css", "border-color", 'rgb(220, 53, 69)');
    })

    it('Does not steal focus', () => {
        cy.get("#sprintEndDate").type("1999-12-01").trigger("change")
        cy.focused().should('have.id', 'sprintEndDate')
    })

    it('Allows you to leave the description blank', () => {
        cy.get("#sprintDescription").clear();
        cy.get("#sprintDescription").should("have.css", "border-color", 'rgb(25, 135, 84)');
        cy.get("#editSprintSubmitButton").click();
        cy.url().should('include', 'portfolio')
    })
})

