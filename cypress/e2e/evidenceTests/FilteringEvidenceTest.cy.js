describe('filtering by categories and skills', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/evidence')
    })

    it("can filter by 'Qualitative' category", () => {
        cy.get('#categoryList').find(".chip").first().click({force: true}).wait(100) //wait for page to load
        cy.get(".evidenceTitle").should("have.text", "Qualitative")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").contains("Qualitative")
        })
    })

    it("can filter by 'making data' skill", () => {
        cy.get('#skillList').find(".chip").last().click({force: true}).wait(100) // wait for  page to load
        cy.get(".evidenceTitle").should("have.text", "making data")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").contains("making data")
        })
    })
})
