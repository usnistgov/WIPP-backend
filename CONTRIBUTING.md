# Contributing to WIPP

## Development flow

### Fork & Pull Request Workflow

To submit a change:
* Fork this repository to your personal account
* Clone the forked repository
* In most cases, the changes should be made in a new branch branched of the *develop* branch (see the [Gitflow Workflow section](#gitflow-workflow) below)
* Submit a Pull Request (PR) to merge your changes into the *develop* branch of this repository
* One of the repository maintainers will review the PR, usually within 48 hours

### Gitflow Workflow

The WIPP repositories are managed following the [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow):
* The *master* branch is for releases only
* All new development occurs on their own branches, the name of the new branch will usually start with 'feature/', 'fix/', 'chore/' or 'doc/' to indicate the type of change
* Branches start from the *develop* branch base

## Styleguides

### Git Commit Messages

* Follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0-beta.4/) convention to format commit messages
* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Squash commits before submitting the PR if needed

### Java Coding Conventions

* Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) conventions