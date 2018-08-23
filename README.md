# Requirements
* MongoDB >= 3.4
* Maven
* Create data folder at /data/WIPP-plugins

# Compiling
```shell
mvn clean install
```
# Running
```shell
cd wipp-backend-application
mvn spring-boot:run
```

# WIPP Development flow
We are following the [Gitflow branching model](https://nvie.com/posts/a-successful-git-branching-model/) for the WIPP development.  
To accommodate the specificities of the Maven version management, we are using the [JGitFlow plugin](https://bitbucket.org/atlassian/jgit-flow/wiki/Home).

## Branching model
* Main branches: master (for releases only), develop (development branch, in SNAPSHOT version)
* One branch per feature/bug fix, linked to a Gitlab issue
* One branch per release/hotfix

## Start a new feature/bug fix
* The process should start with a Gitlab issue, we should be part of a milestone (target release version). Assign the issue to yourself.
* From the develop branch

```shell
git pull --prune # you can also enable auto-prune in your git config
mvn jgitflow:feature-start
```
The plugin will ask for a name, which should mention the issue number and a short description, ie issue123-my-feature.  

Work on the feature, commit and push as needed.   

Once feature is done, create a Merge request (choosing to merge into the develop branch instead of master) from Gitlab and tag another developer in the comments for code review.  
After the code review and optional changes are made, accept and close the merge request (removing the release remote branch). Tag the issue in the merge commit message, for example "closes #12345" and close the issue.