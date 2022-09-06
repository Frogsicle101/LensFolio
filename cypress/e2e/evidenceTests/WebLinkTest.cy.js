describe("WebLinkTest", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1200, 1024)
        cy.visit('/evidence')
        cy.get('#createEvidenceButton').click();
    })

    it('Up to 10 WebLinks', () => {
        for (let i = 1; i < 11; i++) {
            cy.get('#addWebLinkButton').click()
            cy.get('#webLinkUrl').wait(500)
                .type('http://www.a.ac.nz')
            cy.get('#webLinkName').wait(500)
                .type('WebLink ' + i.toString())
            cy.get('#addWebLinkButton').click()
                .type('Wl ' + i.toString())
            cy.get('.addWebLinkButton').click()
        }

        cy.get('#webLinkFull').should('be.visible');
    })
})