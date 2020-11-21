![nukkit](https://raw.githubusercontent.com/PowerNukkit/PowerNukkit/master/.github/images/banner.png)

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE) [![Discord](https://img.shields.io/discord/728280425255927879)](https://powernukkit.org/discord)

# PowerNukkit JUnit 5 Testing Framework 
**[PowerNukkit](https://powernukkit.org)** is a modified version of Nukkit that adds support to a huge amount of features like water-logging, all new blocks, more plugin events, offhand slot, bug fixes and many more.

**[JUnit](https://junit.org)** is a very popular Java test unit framework.

**PowerNukkit JUnit 5 Testing Framework** is an extension to JUnit 5 that allows developers to create
easier PowerNukkit testing codes when developing plugins, tools, and PowerNukkit itself.

## Adding as Dependency
### Maven
```xml
    <repositories>
        <repository>
            <id>powernukkit-bintray</id>
            <name>powernukkit-bintray</name>
            <url>https://dl.bintray.com/powernukkit/powernukkit</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>false</enabled></snapshots>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>org.powernukkit</groupId>
            <artifactId>powernukkit-tests-junit5</artifactId>
            <version>0.1.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

### Gradle
```groovy
repositories {
    mavenCentral()
    maven { url 'https://dl.bintray.com/powernukkit/powernukkit' }
}
dependencies {
    testImplementation 'org.powernukkit:powernukkit-tests-junit5:0.1.0'
}
```

## Example Usage
```java
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PowerNukkitExtension.class)
class MyAwesomeTest {
    @Test
    void testStoneClass() {
        assertEquals(BlockStone.class, Block.get(BlockID.STONE).getClass());
    }
}
```

## Java Documentation
The javadoc files can be viewed online at:  
https://powernukkit.github.io/PowerNukkit-JUnit5-Framework/javadoc/0.1.0
