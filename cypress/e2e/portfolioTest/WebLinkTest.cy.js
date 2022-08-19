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

        // inport weblink 1
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 1')
        cy.get('.addWebLinkButton').click();

        // inport weblink 2
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 2')
        cy.get('.addWebLinkButton').click();

        // inport weblink 3
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 3')
        cy.get('.addWebLinkButton').click();

        // inport weblink 4
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 4')
        cy.get('.addWebLinkButton').click();

        // inport weblink 5
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 5')
        cy.get('.addWebLinkButton').click();

        // inport weblink 6
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 6')
        cy.get('.addWebLinkButton').click();

        // inport weblink 7
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 7')
        cy.get('.addWebLinkButton').click();

        // inport weblink 8
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 8')
        cy.get('.addWebLinkButton').click();

        // inport weblink 9
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 9')
        cy.get('.addWebLinkButton').click();

        // inport weblink 10
        cy.get('.addWebLinkButton').click();
        cy.get('#webLinkUrl')
            .type('https://www.canterbury.ac.nz/')
        cy.get('#webLinkName')
            .type('WebLink 10')
        cy.get('.addWebLinkButton').click();

        // assertion
        cy.get('#webLinkFull').should('have.text', '10 web links have been added');
    })

    }

)