let projectInput;
let tokenInput
let shaInput;

document.addEventListener('DOMContentLoaded', function() {
    projectInput = document.getElementById("apiProjectID");
    tokenInput = document.getElementById("apiAccessToken");
    shaInput = document.getElementById("apiCommitSha");
}, false);



function getBranches() {
    let repoID = projectInput.value;
    let accessToken = tokenInput.value;
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/branches?access_token=" + accessToken, function(data, status){
        return data;
    });
}

function getCommits() {
    let repoID = projectInput.value;
    let accessToken = tokenInput.value;
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/commits?access_token=" + accessToken, function(data, status){
        return data;
    });
}

function getCommit() {
    let repoID = projectInput.value;
    let accessToken = tokenInput.value;
    let commitSha = shaInput.value;
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/commits/" + commitSha + "?access_token=" + accessToken, function(data, status){
        return data;
    });
}

function getMembers() {
    let repoID = projectInput.value;
    let accessToken = tokenInput.value;
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/members?access_token=" + accessToken, function(data, status){
        return data;
    });
}