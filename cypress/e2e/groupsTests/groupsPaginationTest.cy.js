context("WebLinkTest", () => {
        beforeEach(() => {
            cy.visit('http://localhost:9000/login')
            cy.get('#username')
                .type('admin')
            cy.get('#password')
                .type('password{enter}')
            cy.get('.navButtonsDiv').click();
            cy.viewport(1200, 1024)
        })

        it('option and button display for gourps pagination', () => {
            cy.get('.scrollableGroupOverview').first().click()
            cy.get('#lang').should('be.visible');
        })
    }

)