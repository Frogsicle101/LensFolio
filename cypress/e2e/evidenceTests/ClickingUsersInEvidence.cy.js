describe("Clicking Linked Users", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/evidence')
    })

    it("can view other users evidence through the user list", () => {
        const projectId = 1

        let evidenceDTO = JSON.stringify({
            title: "title",
            date: "01-01-2022",
            description: "description",
            projectId: projectId,
            webLinks: [],
            skills: [],
            categories: [],
            associateIds: []
        })

        cy.request({
            method: 'POST',
            url: '/evidence',
            headers: {contentType: "application/json"},
            body: {evidenceDTO},
        }).then(() => {

        })
        cy.get(".userRoleRow").first().click({force: true})
        cy.get("#nameHolder").contains("Viewing evidence for Aaron A")
        cy.get("#createEvidenceButton").should("not.exist")
    })
})