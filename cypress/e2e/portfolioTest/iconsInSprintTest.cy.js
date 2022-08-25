context("iconsInSprintTest", () => {
    beforeEach(() => {
        cy.visit('http://localhost:9000/login')
        cy.get('#username')
            .type('admin')
        cy.get('#password')
            .type('password{enter}')
        cy.get('.navButtonsDiv').click("left");
        cy.viewport(1200, 1024)
    })

    it('Event icons should display in sprint', () => {
        cy.get('[id="eventIconInSprint"]').should('be.visible');
    })

    it('Deadline icons should display in sprint', () => {
        cy.get('[id="deadlineIconInSprint"]').should('be.visible');
    })

    it('Milestone icons should display in sprint', () => {
        cy.get('[id="milestoneIconInSprint"]').should('be.visible');
    })

})