module migodLinux

// Libs
import List;

// Locals
import Statistics;
import experiments::scm::Scm;
import experiments::scm::git::Git;

str linuxVID = "2.6";
str gitLocation = "/Users/migod/Desktop/linux-" + linuxVID;

void main () {
    Repository repo = git(fs(gitLocation), "", {});
//--     list[ChangeSet] resourceChanges = getChanges(repo);
}

