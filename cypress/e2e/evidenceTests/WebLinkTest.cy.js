context("WebLinkTest", () => {
    beforeEach(() => {
        cy.visit('http://localhost:9000/login')
        cy.get('#username')
            .type('admin')
        cy.get('#password')
            .type('password{enter}')
        cy.get('.navButtonsDiv').click("right");
        cy.viewport(1200, 1024)
        cy.get('.createEvidenceButton').click();
    })

    it('Up to 10 WebLinks', () => {
        for (let i = 1; i < 11; i++) {
            cy.get('.addWebLinkButton').click();
            cy.get('#webLinkUrl').wait(500)
                .type('https://www.canterbury.ac.nz/')
            cy.get('#webLinkName').wait(500)
                .type('WebLink ' + i.toString())
            cy.get('.addWebLinkButton').click();
        }

        cy.get('#webLinkFull').should('be.visible');
    })
})