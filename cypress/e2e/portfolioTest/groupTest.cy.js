context("CtrlKeyTest", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.get('.navButtonsDiv').click();
        cy.get('#2').click('top');
    })

    it('pressing ctrl select 2 times adjacent', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.selected)').first()
            .click({ctrlKey: true})
        cy.get('.selected').should('have.length', 2)
    })

    it('pressing ctrl select 2 times non-adjacent', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.selected)').last()
            .click({ctrlKey: true})
        cy.get('.selected').should('have.length', 2)
    })

    it('pressing ctrl select 2 times adjacent, press again to deselect', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.selected)').first()
            .click({ctrlKey: true})
        cy.get('.selected').first().click({ctrlKey: true})
        cy.get('.selected').first().click({ctrlKey: true})
        cy.get('.selected').should('have.length', 0)
    })

    it('pressing ctrl select 2 times non-adjacent, press again to deselect', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.selected)').last()
            .click({ctrlKey: true})
        cy.get('.selected').first().click({ctrlKey: true})
        cy.get('.selected').first().click({ctrlKey: true})
        cy.get('.selected').should('have.length', 0)
    })
})


context("ShiftKeyTest", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.get('.navButtonsDiv').click();
        cy.get('#2').click('top');
    })

    it('shift selecting the entire group members list', () => {
        const numRows = Cypress.$('.userRow').length

        cy.get('.userRow').first()
            .click({shiftKey: true})
        cy.get('body')
            .type('{shift}', {release: false})
        cy.get('.userRow').last()
            .click()

        cy.get('.selected').should('have.length', (numRows))
    })


    it('shift selecting and deselecting the whole group members list', () => {
        cy.get('.userRow').first()
            .click({shiftKey: true})
        cy.get('.userRow').last()
            .click({shiftKey: true})
        cy.get("tr [key='17']").click()
        cy.get('.selected').should('have.length', 1)
    })
})