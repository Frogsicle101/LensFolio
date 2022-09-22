describe('Test Editing Project', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1').wait(500)
        cy.get("#editProject").click({force: true});
    })

    it('Colours valid elements green', () => {
        cy.get("#projectName").should("have.css", "border-color", 'rgb(25, 135, 84)');
    })

    it('Colours invalid elements red', () => {
        cy.get("#projectName").clear();
        cy.get("#projectName").should("have.css", "border-color", 'rgb(220, 53, 69)');
    })

    it('Does not steal focus', () => {
        cy.get("#projectEndDate").type("1999-12-01").trigger("change")
        cy.focused().should('have.id', 'projectEndDate')
    })

    it('Displays multiple errors without submission', () => {
        cy.get("#projectName").invoke('val', 'An ℥ (ounce) of caution');
        cy.get("#projectDescription").invoke('val', 'Ⅵ').trigger("input");

        cy.get("#projectName").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#projectDescription").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#nameError").should("be.visible");
        cy.get("#descriptionError").should("be.visible");
    })

    it('displays date errors', () => {
        cy.get("#projectStartDate")
            .clear().invoke('val', '2003-04-03'); //Year is early
        cy.get("#projectEndDate").invoke('removeAttr','type')
            .clear().invoke('val', '2022-03-03').trigger("change"); //Ends inside of a sprint

        cy.get("#projectStartDate").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#projectEndDate").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#projectStartDateFeedback").should("be.visible");
        cy.get("#projectEndDateFeedback").should("be.visible");
    })

    it('displays date errors when the end date is before the start date', () => {
        cy.get("#projectStartDate")
            .clear().invoke('val', '2022-09-03').trigger("change");
        cy.get("#projectEndDate")
            .clear().invoke('val', '2022-03-25').trigger("change");

        cy.wait(500);
        cy.get("#projectStartDate").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#projectEndDate").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#projectStartDateFeedback").should("be.visible");
        cy.get("#projectEndDateFeedback").should("be.visible");
    })

    it('Allows you to leave the description blank', () => {
        cy.get("#projectDescription").clear();
        cy.get("#projectDescription").should("have.css", "border-color", 'rgb(25, 135, 84)');
        cy.get("#editProjectSubmitButton").click();
        cy.url().should('include', 'portfolio')
    })
})

