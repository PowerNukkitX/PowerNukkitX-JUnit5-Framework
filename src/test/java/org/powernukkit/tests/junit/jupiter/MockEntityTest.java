package org.powernukkit.tests.junit.jupiter;


import cn.nukkit.entity.passive.EntityPig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.MockUtil;
import org.powernukkit.tests.api.MockEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PowerNukkitExtension.class)
public class MockEntityTest {
    @MockEntity
    EntityPig pig;

    @Test
    void pig() {
        assertTrue(MockUtil.isMock(pig));
    }
}
