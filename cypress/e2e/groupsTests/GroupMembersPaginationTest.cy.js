context("Test group pagination", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.viewport(1200, 1024)
        cy.scrollTo('bottom')
        cy.get('#1').click()
    })

})