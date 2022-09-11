describe('Test Registration', () => {
  beforeEach('Visits the local web address for registration', () => {
    cy.visit('/register')
  })

  it('Fills in the registration details with correct information', () => {
    cy.get("#firstname").invoke('val', 'Aaron')
    cy.get("#middlename").invoke('val', 'A test')
    cy.get("#lastname").invoke('val',"A")
    cy.get("#username").invoke('val',"User" + Math.floor(Math.random() * 100))
    cy.get("#password").invoke('val',"password")
    cy.get("#email").invoke('val',"test@test.com")
    cy.contains("Submit").click()
    cy.url().should("include", '/account')
  })

  it('Fills in the registration details with bad username', () => {
    cy.get("#firstname").invoke('val', 'test')
    cy.get("#middlename").invoke('val', 'test')
    cy.get("#lastname").invoke('val', 'test')
    cy.get("#password").invoke('val', 'password')
    cy.get("#email").invoke('val', 'test@test.com')
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad firstname', () => {
    cy.get("#middlename").invoke('val',"test")
    cy.get("#lastname").invoke('val',"test")
    cy.get("#password").invoke('val',"password")
    cy.get("#username").invoke('val',"User" + Math.floor(Math.random() * 100))
    cy.get("#email").invoke('val',"test@test.com")
    cy.contains("Submit").click()
    cy.get('#firstname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad lastname', () => {
    cy.get("#firstname").invoke('val',"test")
    cy.get("#middlename").invoke('val',"test")
    cy.get("#password").invoke('val',"password")
    cy.get("#username").invoke('val',"User" + Math.floor(Math.random() * 100))
    cy.get("#email").invoke('val',"test@test.com")
    cy.contains("Submit").click()
    cy.get('#lastname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with invalid username', () => {
    cy.get("#firstname").invoke('val',"test")
    cy.get("#middlename").invoke('val',"test")
    cy.get("#lastname").invoke('val',"test")
    cy.get("#username").invoke('val','\u0000' + Math.floor(Math.random() * 10))
    cy.get("#password").invoke('val',"password")
    cy.get("#email").invoke('val',"test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('exist')
  })

  it('Fills in the registration details with bio emoji', () => {
    cy.get("#firstname").invoke('val',"test")
    cy.get("#middlename").invoke('val',"test")
    cy.get("#lastname").invoke('val',"test")
    cy.get("#username").invoke('val',"test" + Math.floor(Math.random() * 10))
    cy.get("#password").invoke('val',"password")
    cy.get("#email").invoke('val',"test@test.com")
    cy.get("#bio").type("😀")
    cy.get("#alertPopUp").should('be.visible').contains("Invalid character")
  })
})

