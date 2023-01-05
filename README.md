![nukkit](https://raw.githubusercontent.com/PowerNukkit/PowerNukkit/master/.github/images/banner.png)

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE) [![Discord](https://img.shields.io/discord/728280425255927879)](https://powernukkit.org/discord)

# PowerNukkit JUnit 5 Testing Framework 
**[PowerNukkit](https://powernukkit.org)** is a modified version of Nukkit that adds support to a huge amount of features like water-logging, all new blocks, more plugin events, offhand slot, bug fixes and many more.

**[JUnit](https://junit.org)** is a very popular Java test unit framework.

**PowerNukkitX JUnit 5 Testing Framework** is an extension to JUnit 5 that allows developers to create
easier PowerNukkit testing codes when developing plugins, tools, and PowerNukkit itself.

## Adding as Dependency
### Maven
```xml
<repository>
    <id>maven-powernukkitx-cn</id>
    <url>https://maven.powernukkitx.cn/repository/maven-public/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>

<dependencies>
    <dependency>
        <groupId>cn.powernukkitx</groupId>
        <artifactId>powernukkitx-tests-junit5</artifactId>
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Gradle
```groovy
repositories {
    mavenCentral()
}
dependencies {
    testImplementation 'cn.powernukkitx:powernukkitx-tests-junit5:0.0.1'
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

There are tons of usage example at https://github.com/PowerNukkit/PowerNukkit/tree/master/src/test because we use
this extension to test PowerNukkit itself using JUnit5.

## Java Documentation
The javadoc files can be viewed online at:  
https://powernukkit.github.io/PowerNukkit-JUnit5-Framework/javadoc/0.1.0
