context("CtrlKeyTest", () => {
    beforeEach(() => {
        cy.visit('http://localhost:9000/login')
        cy.get('#username')
            .type('admin')
        cy.get('#password')
            .type('password{enter}')
        cy.get('.navButtonsDiv').click();
        cy.get('#focusOnGroup').click('top');
    })

    it('pressing ctrl select, press again to deselect', () => {

    })

})