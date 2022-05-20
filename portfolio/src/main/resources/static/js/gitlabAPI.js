

function getBranches(repoID) {
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/branches", function(data, status){
        alert(data + " " + status);
    });
}

getBranches("seng302-2022/team-600");