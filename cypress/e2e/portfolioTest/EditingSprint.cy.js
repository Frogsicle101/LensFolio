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
})

