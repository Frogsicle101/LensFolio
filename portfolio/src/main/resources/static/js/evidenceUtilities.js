/**
 * A JS file that contains utilities for pieces of evidence,
 * that can be used across multiple pages.
 */


/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;

/** A regex only allowing modern English letters */
const regExp = new RegExp('[A-Za-z]');

/** The id of the piece of evidence being displayed. */
let selectedEvidenceId;

let skillsArray = ["ActionScript", "AppleScript", "Asp", "BASIC", "C", "C++", "Clojure", "COBOL", "ColdFusion", "Erlang", "Fortran", "Groovy", "Haskell", "Java", "JavaScript", "Lisp", "Perl", "PHP", "Python", "Ruby", "Scala", "Scheme"]


/**
 * Makes a call to the server and gets all the skills belonging to this user,
 * It then appends those skills to the list
 */
function getUserSkills() {
    $.ajax({
        url: "skills?userId=" + userBeingViewedId, type: "GET", success: function (response) {
            console.log(response)
            for (let skill of response) {
                if (!skillsArray.includes(skill)) {
                    skillsArray.push(skill)
                }
            }
        }, error: function (response) {
            console.log(response)
        }
    })
}