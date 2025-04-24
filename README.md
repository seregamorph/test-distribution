# test-distribution
Dynamic Test Distribution allows to split test suite of unit or integration tests to separate parts for individual
execution. There can be different split criteria like simple split to N groups, split with weights (TODO),
split by spring context configuration for integration test (TODO) or custom.

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
                <executions>
                    <execution>
                        <id>unit-test-split</id>
                        <goals>
                            <goal>split</goal>
                        </goals>
                        <configuration>
                            <includes>
                                <include>**/*Test.java</include>
                            </includes>
                            <numGroups>4</numGroups>
                            <testGroupName>unit-test</testGroupName>
                            <!-- distributionProvider parameter allows to specify the custom Provider -->
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
                <executions>
                    <execution>
                        <id>unit-test-filter</id>
                        <goals>
                            <goal>filter-test</goal>
                        </goals>
                        <configuration>
                            <testGroupName>unit-test</testGroupName>
                            <groupNumber>${unit-test.group}</groupNumber>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```
