package me.jumper251.replay;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import me.jumper251.replay.database.DatabaseRegistry;
import me.jumper251.replay.filesystem.ConfigManager;
import me.jumper251.replay.filesystem.saving.DatabaseReplaySaver;
import me.jumper251.replay.filesystem.saving.DefaultReplaySaver;
import me.jumper251.replay.filesystem.saving.ReplaySaver;
import me.jumper251.replay.filesystem.saving.S3ReplaySaver;
import me.jumper251.replay.replaysystem.Replay;
import me.jumper251.replay.replaysystem.utils.ReplayCleanup;
import me.jumper251.replay.utils.Metrics;
import me.jumper251.replay.utils.ReplayManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class ReplaySystem extends JavaPlugin {
    public final static String PREFIX = "§8[§3Replay§8] §r§7";
    public static ReplaySystem instance;
    public static Metrics metrics;

    public static ReplaySystem getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        for (Replay replay : new HashMap<>(ReplayManager.activeReplays).values()) {
            if (replay.isRecording() && replay.getRecorder().getData().getActions().size() > 0) {
                replay.getRecorder().stop(ConfigManager.SAVE_STOP);
            }
        }
        if (ConfigManager.USE_DATABASE || ConfigManager.USE_MONGO)
            DatabaseRegistry.getDatabase().disconnect();
    }

    @Override
    public void onEnable() {
        instance = this;

        Long start = System.currentTimeMillis();

        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.addMavenCentral();

        getLogger().info("Loading Replay v" + getDescription().getVersion() + " by " + getDescription().getAuthors());

        ConfigManager.loadConfigs();
        ReplayManager.register();

        if (ConfigManager.USE_DATABASE || ConfigManager.USE_MONGO) {
            ReplaySaver.register(new DatabaseReplaySaver());
            DatabaseRegistry.getDatabase().getService().getReplays().stream()
                    .forEach(info -> DatabaseReplaySaver.replayCache.put(info.getID(), info));
        } else if (ConfigManager.USE_S3) {
            Library minioLibrary = Library.builder()
                    .groupId("io{}minio")
                    .artifactId("minio")
                    .version("8.5.12")
                    .resolveTransitiveDependencies(true)
                    .build();
            libraryManager.loadLibrary(minioLibrary);
            S3ReplaySaver s3ReplaySaver = new S3ReplaySaver(
                    ConfigManager.s3Cfg.getString("endpoint_url"),
                    ConfigManager.s3Cfg.getString("access_key"),
                    ConfigManager.s3Cfg.getString("secret_key"),
                    ConfigManager.s3Cfg.getString("bucket_name")
            );

            s3ReplaySaver.connect().thenAccept(isConnected -> {
                if (!isConnected)
                    getLogger().warning("Could not connect to S3 storage backend.");
            });

            ReplaySaver.register(s3ReplaySaver);
        } else {
            ReplaySaver.register(new DefaultReplaySaver());
        }

        metrics = new Metrics(this, 2188);

        if (ConfigManager.CLEANUP_REPLAYS > 0) {
            ReplayCleanup.cleanupReplays();
        }

        getLogger().info("Finished (" + (System.currentTimeMillis() - start) + "ms)");
    }
}
