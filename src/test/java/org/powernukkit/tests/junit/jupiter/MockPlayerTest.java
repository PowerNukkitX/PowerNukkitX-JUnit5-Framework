/*
 * PowerNukkit JUnit 5 Testing Framework
 * Copyright (C) 2020  José Roberto de Araújo Júnior
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.powernukkit.tests.junit.jupiter;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powernukkit.tests.api.MockPlayer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PowerNukkitExtension.class)
class MockPlayerTest {
    @MockPlayer(name = "Hello")
    Player playerHello;

    @MockPlayer(name = "How")
    Player playerHow;

    @MockPlayer(name = "Are")
    Player playerAre;

    @MockPlayer(name = "You")
    Player playerYou;
    
    @Test
    void playerTest() {
        assertEquals("Hello", playerHello.getName());
        assertNotNull(playerHello.getLevel());
        
        assertEquals(playerHello.getLevel(), playerHow.getLevel());

        assertEquals("Are", playerAre.getName());
        
        assertTrue(playerYou.attack(new EntityDamageByEntityEvent(playerAre, playerYou, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10)));
        assertEquals(10, playerYou.getHealth());
    }
}
