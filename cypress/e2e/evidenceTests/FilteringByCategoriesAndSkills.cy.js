describe('filtering by categories and skills', () => {
    beforeEach(() => {
     cy.adminLogin()
    })

    it("can filter by 'Qualitative' category", () => {
        cy.visit('/evidence')
        cy.get('#categoryList').find(".categoryChip").first().click({force: true})
        cy.get(".evidenceTitle").should("have.text", "Qualitative")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").first().contains("Qualitative")
        })
    })

    it("can filter by 'debugging' skill", () => {
            cy.visit('/evidence')
            cy.get('#skillList').find("#SkillCalleddebugging").click({force: true})
            cy.get(".evidenceTitle").should("have.text", "debugging")
            cy.get(".evidenceListItem").each(($el) => {
                cy.wrap($el).find(".skillChipDisplay").eq(1).contains("debugging")
            })
        })
})
