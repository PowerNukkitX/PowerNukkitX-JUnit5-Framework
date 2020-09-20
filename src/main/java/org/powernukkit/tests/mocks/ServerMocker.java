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

package org.powernukkit.tests.mocks;

import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.console.NukkitConsole;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.level.Level;
import cn.nukkit.metadata.EntityMetadataStore;
import cn.nukkit.metadata.LevelMetadataStore;
import cn.nukkit.metadata.PlayerMetadataStore;
import cn.nukkit.network.Network;
import cn.nukkit.network.query.QueryHandler;
import cn.nukkit.network.rcon.RCON;
import cn.nukkit.permission.BanList;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.plugin.service.NKServiceManager;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.PlayerDataSerializer;
import cn.nukkit.utils.Watchdog;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.apiguardian.api.API;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.util.FileUtils;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.configuration.injection.scanner.MockScanner;
import org.mockito.internal.util.collections.Sets;
import org.powernukkit.tests.api.MockServer;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.mockito.Mockito.*;
import static org.powernukkit.tests.api.ReflectionUtil.*;

/**
 * @author joserobjr
 */
@API(status = EXPERIMENTAL, since = "0.1.0")
public class ServerMocker extends Mocker<Server> {
    @API(status = EXPERIMENTAL, since = "0.1.0")
    public static void setServerInstance(@Nullable Server server) {
        execute(()-> setField(null, Server.class.getDeclaredField("instance"), server));
    }
    
    private static final Class<?> classPositionTrackingService;
    private static final Method getPositionTrackingService;
    private static final Field positionTrackingServiceField;
    static {
        Class<?> c;
        Method m;
        Field f;
        try {
            c = Class.forName("cn.nukkit.positiontracking.PositionTrackingService");
            m = Server.class.getDeclaredMethod("getPositionTrackingService");
            f = Server.class.getDeclaredField("positionTrackingService");
        } catch (ReflectiveOperationException ignored) {
            c = null;
            m = null;
            f = null;
        }
        classPositionTrackingService = c;
        getPositionTrackingService = m;
        positionTrackingServiceField = f;
    }
    
    private MockServer config;
    
    @Mock
    PluginManager pluginManager;
    
    @Mock
    PlayerDataSerializer playerDataSerializer;
    
    @Mock
    DB nameLookup;
    
    @Mock
    Watchdog watchdog;
    
    @Mock
    NukkitConsole console;
    
    @Mock
    QueryRegenerateEvent queryRegenerateEvent;
    
    @Mock
    Network network;

    @Mock
    RCON rcon;

    @Mock
    EntityMetadataStore entityMetadata;

    @Mock
    PlayerMetadataStore playerMetadata;

    @Mock
    LevelMetadataStore levelMetadata;
    
    @Mock
    QueryHandler queryHandler;
    
    @Mock
    ServerScheduler serverScheduler;
    
    Object posTrackingService;
    
    File tempDir;
    File worldsDir;
    File playersDir;
    File pluginsDir;
    File resourcePacksDir;
    File bannedPlayersFile;
    File bannedIpsFile;
    File posTrackingServiceDir;
    
    Server server;
    
    BaseLang baseLang = new BaseLang(BaseLang.FALLBACK_LANGUAGE);

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public ServerMocker() {
        this(PowerNukkitExtension.class.getAnnotation(MockServer.class));
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public ServerMocker(MockServer config) {
        this.config = config;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public void setActive() {
        setServerInstance(server);
    }
    
    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    @API(status = EXPERIMENTAL, since = "0.1.0")
    @Override
    public Server create() {
        MockitoAnnotations.initMocks(this);
        
        server = config.callsRealMethods()? mock(Server.class, CALLS_REAL_METHODS) : mock(Server.class);
        
        if (config.initPrivateFields() || config.createTempDir()) {
            tempDir = config.createTempDir()? Files.createTempDir() : new File(".");
            worldsDir = new File(tempDir, "worlds");
            playersDir = new File(tempDir, "players");
            pluginsDir = new File(tempDir, "plugins");
            resourcePacksDir = new File(tempDir, "resource_packs");
            bannedPlayersFile = new File(tempDir, "banned-players.json");
            bannedIpsFile = new File(tempDir, "banned-ips.json");
            if (positionTrackingServiceField != null) {
                posTrackingServiceDir = new File(tempDir, "services/position_tracking_db");
            }
        }

        if (positionTrackingServiceField != null) {
            boolean delete = false;
            if (posTrackingServiceDir == null) {
                posTrackingServiceDir = Files.createTempDir();
                delete = true;
            }
            
            MockSettings mockSettings = withSettings().useConstructor(posTrackingServiceDir);
            if (config.callsRealMethods()) {
                mockSettings = mockSettings.defaultAnswer(CALLS_REAL_METHODS);
            }
            
            posTrackingService = mock(classPositionTrackingService, mockSettings);
            
            if (delete) {
                FileUtils.deleteRecursively(posTrackingServiceDir);
            }
        }
        
        setField(server, Server.class.getDeclaredField("isRunning"), new AtomicBoolean(true));
        
        if (!config.callsRealMethods()) {
            BanList nameBans = mock(BanList.class);
            BanList ipBans = mock(BanList.class);
            
            setField(server, Server.class.getDeclaredField("playerDataSerializer"), playerDataSerializer);
            setField(server, Server.class.getDeclaredField("pluginManager"), pluginManager);
            
            if (posTrackingService != null) {
                lenient().when(getPositionTrackingService.invoke(server)).thenReturn(posTrackingService);
            }
            
            lenient().when(server.getConfig(anyString(), any())).thenAnswer(call-> call.getArgument(1));
            lenient().when(server.getConfig()).thenReturn(new Config());
            lenient().when(server.getPluginManager()).thenReturn(pluginManager);
            lenient().when(server.getPlayerDataSerializer()).thenCallRealMethod();
            lenient().when(server.getConsoleSender()).thenReturn(new ConsoleCommandSender());
            lenient().when(server.getNetwork()).thenReturn(network);
            lenient().when(server.getEntityMetadata()).thenReturn(entityMetadata);
            lenient().when(server.getPlayerMetadata()).thenReturn(playerMetadata);
            lenient().when(server.getPlayerDataSerializer()).thenReturn(playerDataSerializer);
            lenient().when(server.getLevelMetadata()).thenReturn(levelMetadata);
            lenient().when(server.getQueryInformation()).thenReturn(queryRegenerateEvent);
            lenient().when(server.getMaxPlayers()).thenReturn(config.maxPlayers());
            lenient().when(server.getLogger()).thenCallRealMethod();
            lenient().when(server.getScheduler()).thenReturn(serverScheduler);
            lenient().when(server.getNameBans()).thenReturn(nameBans);
            lenient().when(server.getIPBans()).thenReturn(ipBans);
            lenient().when(server.getLanguage()).thenReturn(new BaseLang(BaseLang.FALLBACK_LANGUAGE));
            lenient().when(server.isWhitelisted(any())).thenReturn(true);
            lenient().when(server.getOfflinePlayerData(any(UUID.class), anyBoolean())).thenCallRealMethod();
            lenient().when(server.getResourcePackManager()).thenReturn(mock(ResourcePackManager.class));
            lenient().when(server.getCommandMap()).thenReturn(mock(SimpleCommandMap.class));
            lenient().when(server.isRunning()).thenReturn(true);
            
            if (config.createTempDir()) {
                String path = tempDir.getAbsolutePath() + "/";
                lenient().when(server.getDataPath()).thenReturn(path);
                lenient().when(server.getFilePath()).thenReturn(path);
                lenient().when(server.getPluginPath()).thenReturn(pluginsDir.getAbsolutePath()+"/");
            }
        }

        if (!config.initPrivateFields()) {
            return server;
        }
        
        Set<Object> mocks = Sets.newMockSafeHashSet();
        new MockScanner(this, ServerMocker.class).addPreparedMocks(mocks);
        mocks.remove(server);

        Field serverField = ServerMocker.class.getDeclaredField("server");
        MockUtil.injectMocks(serverField, mocks, this);

        Field instance = Server.class.getDeclaredField("instance");
        Object before = getField(null, instance);
        setField(null, instance, server);

        Config serverProperties = new Config();
        serverProperties.set("motd", "A PowerNukkit Server");
        serverProperties.set("sub-motd", "https://powernukkit.org");
        serverProperties.set("server-port", 19132);
        serverProperties.set("server-ip", "0.0.0.0");
        serverProperties.set("view-distance", 10);
        serverProperties.set("white-list", false);
        serverProperties.set("achievements", true);
        serverProperties.set("announce-player-achievements", true);
        serverProperties.set("spawn-protection", 16);
        serverProperties.set("max-players", config.maxPlayers());
        serverProperties.set("allow-flight", false);
        serverProperties.set("spawn-animals", true);
        serverProperties.set("spawn-mobs", true);
        serverProperties.set("gamemode", 0);
        serverProperties.set("force-gamemode", false);
        serverProperties.set("hardcore", false);
        serverProperties.set("pvp", true);
        serverProperties.set("difficulty", 1);
        serverProperties.set("generator-settings", "");
        serverProperties.set("level-name", "world");
        serverProperties.set("level-seed", "");
        serverProperties.set("level-type", "DEFAULT");
        serverProperties.set("allow-nether", true);
        serverProperties.set("enable-query", true);
        serverProperties.set("enable-rcon", false);
        serverProperties.set("rcon.password", "random");
        serverProperties.set("auto-save", true);
        serverProperties.set("force-resources", false);
        serverProperties.set("xbox-auth", true);

        Config nukkitYml = new Config();
        try (InputStream in = Server.class.getResourceAsStream("/default-nukkit.yml")) {
            nukkitYml.load(in);
        }
        
        if (posTrackingService != null && positionTrackingServiceField != null) {
            setField(server, positionTrackingServiceField, posTrackingService);
        }
        
        setField(server, Server.class.getDeclaredField("properties"), serverProperties);
        setField(server, Server.class.getDeclaredField("config"), nukkitYml);
        setField(server, Server.class.getDeclaredField("operators"), new Config());
        setField(server, Server.class.getDeclaredField("whitelist"), new Config());
        setField(server, Server.class.getDeclaredField("ignoredPackets"), new HashSet<>());
        setField(server, Server.class.getDeclaredField("profilingTickrate"), 20);
        setField(server, Server.class.getDeclaredField("tickAverage"), new float[]{20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20});
        setField(server, Server.class.getDeclaredField("useAverage"), new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        setField(server, Server.class.getDeclaredField("maxTick"), 20);
        setField(server, Server.class.getDeclaredField("redstoneEnabled"), true);
        setField(server, Server.class.getDeclaredField("networkCompressionLevel"), 7);
        setField(server, Server.class.getDeclaredField("autoTickRateLimit"), 20);
        setField(server, Server.class.getDeclaredField("baseTickRate"), 1);
        setField(server, Server.class.getDeclaredField("difficulty"), 1);
        setField(server, Server.class.getDeclaredField("autoSaveTicks"), 6000);
        setField(server, Server.class.getDeclaredField("uniquePlayers"), new HashSet<>());
        setField(server, Server.class.getDeclaredField("players"), new HashMap<>());
        setField(server, Server.class.getDeclaredField("playerList"), new HashMap<>());
        setField(server, Server.class.getDeclaredField("currentThread"), Thread.currentThread());
        setField(server, Server.class.getDeclaredField("filePath"), tempDir.getAbsolutePath()+"/");
        setField(server, Server.class.getDeclaredField("dataPath"), tempDir.getAbsolutePath()+"/");
        setField(server, Server.class.getDeclaredField("pluginPath"), pluginsDir.getAbsolutePath()+"/");
        setField(server, Server.class.getDeclaredField("consoleSender"), new ConsoleCommandSender());
        setField(server, Server.class.getDeclaredField("serviceManager"), new NKServiceManager());
        setField(server, Server.class.getDeclaredField("banByName"), new BanList(bannedPlayersFile.getAbsolutePath()));
        setField(server, Server.class.getDeclaredField("banByIP"), new BanList(bannedIpsFile.getAbsolutePath()));
        setField(server, Server.class.getDeclaredField("maxPlayers"), 20);
        setField(server, Server.class.getDeclaredField("baseLang"), baseLang);
        setField(server, Server.class.getDeclaredField("allowNether"), true);

        setField(server, Server.class.getDeclaredField("commandMap"), new SimpleCommandMap(server));
        setField(server, Server.class.getDeclaredField("craftingManager"), new CraftingManager());
        setField(server, Server.class.getDeclaredField("resourcePackManager"), new ResourcePackManager(resourcePacksDir));

        final Field levelArray = Server.class.getDeclaredField("levelArray");
        setField(server, levelArray, new Level[0]);
        setField(server, Server.class.getDeclaredField("levels"), new HashMap<Integer, Level>() {
                    @Override
                    public Level put(Integer key, Level value) {
                        Level result = super.put(key, value);
                        setField(server, levelArray, values().toArray(new Level[0]));
                        return result;
                    }

                    @Override
                    public boolean remove(Object key, Object value) {
                        boolean result = super.remove(key, value);
                        setField(server, levelArray, values().toArray(new Level[0]));
                        return result;
                    }

                    @Override
                    public Level remove(Object key) {
                        Level result = super.remove(key);
                        setField(serverField, levelArray, values().toArray(new Level[0]));
                        return result;
                    }
                }
        );

        setField(null, instance, before);
        return this.server;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public Server getServer() {
        return server;
    }

    @API(status = EXPERIMENTAL, since = "0.1.0")
    public MockServer getConfig() {
        return config;
    }

    public void releaseResources() {
        if (tempDir != null) {
            FileUtils.deleteRecursively(tempDir);
        }
    }
}
