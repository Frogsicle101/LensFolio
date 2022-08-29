describe('filtering by categories and skills', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/evidence')
    })

    it("can filter by 'Qualitative' category", () => {
        cy.get('#categoryList').find(".skillName").first().click({force: true})
        cy.get(".evidenceTitle").should("have.text", "Qualitative")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").first().contains("Qualitative")
        })
    })

    it("can filter by 'test' skill", () => {
        cy.get('#skillList').find("#SkillCalledtest").click({force: true})
        cy.get(".evidenceTitle").should("have.text", "test")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").eq(1).contains("debugging")
        })
    })
})
