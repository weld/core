# Contributing Guide

**First of all, thank you for taking the time to contribute to Weld!**

Below are some basics such as building the project, pull request standards and issue tracking.  
Please read them before making your contribution and try to adhere to them - it will make it a lot easier for us maintainers to accept your contribution.

### Building and Testing

Weld projects use Java and Maven; building them is as easy as running:

```
mvn clean install
```

The above command will build the whole project on the branch you checked out as well as execute tests for each module.   
Note that this will not execute all the tests; in-container tests - for example tests against actual WildFly server - require some extra setup. However, creating a pull request will trigger a CI run that includes all the tests applicable for given branch.

Alternatively, if you want a faster build, you can skip the testing part by running the following command:

```
mvn clean install -DskipTests
```

### Code Formatting

All Weld projects that inherit from [Weld Parent](https://github.com/weld/parent/blob/56/pom.xml#L610-L650) will also bring in a Maven formatter plugin and import statement sorter.  
Triggering a build of the project should automatically format the code as well as imports in Java classes.  
The set of rules is borrowed from Quarkus IDE config artifact, which can be found [here](https://github.com/quarkusio/quarkus/tree/3.16.1/independent-projects/ide-config/src/main/resources).

### Tracking Your Contributions

Every enhancement or fix should have a tracking issue.  
Not only does it help create release notes, keep track of what has been done or while searching for similar issues, but it also allows you to explain what/how you want to contribute and ask questions.

### Need to Start a Discussion?

If you are unsure and want to first reach out and ask about how to approach certain fix/enhancement, you are welcome to use GitHub discussions which should be enabled for all Weld repositories.  
Alternatively, you can reach out to us via the [weld-dev mailing list](https://lists.jboss.org/archives/list/weld-dev@lists.jboss.org/).

### Pull Request Standards:

* Pull requests are typically sent from your fork of this repository
* Each pull request should link to a tracking issue, ee the [section below](#issues) for more information
* If possible and applicable, try to write an automated test
  * In case you are uncertain how to do that, create a pull request without it and ask for help
* While working on a pull request, you can use any amount of commits but once done, it is preferable to squash commits into a single one
* Commit message(s) should be meaningful and if the project uses JIRA, it should start with the JIRA issue number
  * For instance `WELD-1234 Fix the answer to universe, life and everything`

### Issues

Weld API uses JIRA to manage issues. All issues can be found [here](https://issues.redhat.com/projects/WELD/issues/).

To create a new issue, comment on an existing issue, or assign an issue to yourself, you'll need to first [create a JIRA account](https://issues.redhat.com/).

Lastly, this project is an open source project. Please act responsibly, be nice, polite and enjoy!
