# test-distribution
Dynamic Test Distribution allows to split test suite of unit or integration tests to separate parts for individual
execution. There can be different split criteria like simple split to N groups, split with weights (TODO),
split by spring context configuration for integration test or custom.

## Defining custom split rule
Implement [DistributionProvider](test-distribution-provider/src/main/java/com/github/seregamorph/testdistribution/DistributionProvider.java).
The implemented method receives the initial list of test classes in the suite, the number of groups and should return parts.

## How it works
After test compilation the specialized Maven goal `split` generates `target/test-distribution-{test-groups}.json` files (there can be
several target files like this e.g. for unit and integration tests). These files contain lists of test class names separated
by test groups.

Another maven goal `filter` having an input the Number of current job reads such files and passes requested list of tests to surefire
or failsafe plugin.

## Strong contracts
The test distribution is reproducible (unless it's intentionally implemented the other way in custom `DistributionProvider`).
This means it should be recalculated to the exactly same list on re-evaluation.

The test distribution is build cache compatible, because calculated test groups are per-module (not global).

## How to config Maven project
```xml
<profile>
    <id>tests-distribution-split</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.seregamorph</groupId>
                <artifactId>test-distribution-maven-plugin</artifactId>
                <version>${test-distribution.version}</version>
                <executions>
                    <execution>
                        <id>unit-test-split</id>
                        <goals>
                            <goal>split</goal>
                        </goals>
                        <configuration>
                            <includes>
                                <!--Note: includes *.kt-->
                                <include>**/*Test.java</include>
                                <include>**/*Tests.java</include>
                            </includes>
                            <numGroups>${unitTestGroups}</numGroups>
                            <testGroupName>unit-test</testGroupName>
                            <minGroupSize>64</minGroupSize>
                        </configuration>
                    </execution>
                    <execution>
                        <id>integration-test-split</id>
                        <goals>
                            <goal>split</goal>
                        </goals>
                        <configuration>
                            <includes>
                                <!--Note: includes *.kt-->
                                <include>**/*IT.java</include>
                            </includes>
                            <numGroups>${integrationTestGroups}</numGroups>
                            <testGroupName>integration-test</testGroupName>
                            <minGroupSize>16</minGroupSize>
                            <!--
                            In case if SpringDistributionProvider used, don't forget to add
                            "com.github.seregamorph:test-distribution-spring-provider:${test-distribution.version}"
                            dependency to the test classpath of your module
                            -->
                            <distributionProvider>com.github.seregamorph.testdistribution.spring.SpringDistributionProvider</distributionProvider>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
<profile>
    <id>tests-distribution-filter</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.seregamorph</groupId>
                <artifactId>test-distribution-maven-plugin</artifactId>
                <version>${test-distribution.version}</version>
                <executions>
                    <execution>
                        <id>unit-test-filter</id>
                        <goals>
                            <goal>filter-test</goal>
                        </goals>
                        <configuration>
                            <testGroupName>unit-test</testGroupName>
                            <groupNumber>${unitTestGroup}</groupNumber>
                        </configuration>
                    </execution>
                    <execution>
                        <id>integration-test-filter</id>
                        <goals>
                            <goal>filter-integration-test</goal>
                        </goals>
                        <configuration>
                            <testGroupName>integration-test</testGroupName>
                            <groupNumber>${integrationTestGroup}</groupNumber>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

To generate distributions locally:
```shell
./mvnw clean install -DskipTests=true -P tests-distribution-split -DintegrationTestGroups=10 -DunitTestGroups=10
```
Then you can run partial unit tests:
```shell
./mvnw test-distribution:filter-test@unit-test-filter \
    surefire:test \
    -P tests-distribution-filter \
    -DunitTestGroup=1
```
or integration tests
```shell
./mvnw test-distribution:filter-integration-test@integration-test-filter \
    failsafe:integration-test failsafe:verify \
    -P tests-distribution-filter \
    -DintegrationTestGroup=1
```
