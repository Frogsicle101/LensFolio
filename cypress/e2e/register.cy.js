

context('Window', () => {
  beforeEach(() => {
    cy.visit('http://localhost:9000/anything')
  })

  it('login', () => {
    // 'https://csse-s302g6.canterbury.ac.nz/test/portfolio/login'
    cy.get('#username')
        .type('admin')
    cy.get('#password')
        .type('password{enter}')
    cy.url().should('include', 'account')
  })
})
