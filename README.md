# PDS3 Product Tools Library 
Java library of software classes for manipulating 
PDS product labels. The software is packaged in a JAR file.

# Documentation
The documentation including release notes, installation and operation of the
software should be online at
https://pds-engineering.jpl.nasa.gov/development/pds4/current/preparation/validate/. If it is not
accessible, you can execute the "mvn site:run" command and view the
documentation locally at http://localhost:8080.

# Build
The software can be compiled and built with the "mvn compile" command but in order
to create the JAR file, you must execute the "mvn compile jar:jar" command.

In order to create a complete distribution package, execute the
following commands:

% mvn site
% mvn package

# Release
Here is the procedure for releasing the software both in Github and pushing the JARs to the public Maven Central repo:

1. Checkout the dev branch.

2. Version the software:
```
mvn versions:set -DnewVersion=1.2.0
```

3. Deploy the new software to Sonatype Maven repo:
```
mvn clean deploy -P release
```

4. Create pull request from dev -> master and merge.

5. Tag release in Github

6. Update version to next snapshot:
```
mvn versions:set -DnewVersion=1.3.0-SNAPSHOT
```