package me.jumper251.replay.filesystem;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import me.jumper251.replay.ReplaySystem;
import me.jumper251.replay.database.DatabaseRegistry;
import me.jumper251.replay.database.MongoDatabase;
import me.jumper251.replay.database.MySQLDatabase;
import me.jumper251.replay.replaysystem.data.CompressType;
import me.jumper251.replay.replaysystem.recording.optimization.ReplayQuality;
import me.jumper251.replay.replaysystem.replaying.session.ReplayProgressType;
import me.jumper251.replay.replaysystem.replaying.session.ReplayProgression;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ConfigManager {

    public static File sqlFile = new File(ReplaySystem.getInstance().getDataFolder(), "mysql.yml");
    public static FileConfiguration sqlCfg = YamlConfiguration.loadConfiguration(sqlFile);

    public static File s3File = new File(ReplaySystem.getInstance().getDataFolder(), "s3.yml");
    public static FileConfiguration s3Cfg = YamlConfiguration.loadConfiguration(s3File);

    public static File mongoFile = new File(ReplaySystem.getInstance().getDataFolder(), "mongodb.yml");
    public static FileConfiguration mongoCfg = YamlConfiguration.loadConfiguration(mongoFile);

    public static File file = new File(ReplaySystem.getInstance().getDataFolder(), "config.yml");
    public static FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

    public static int MAX_LENGTH, CLEANUP_REPLAYS;

    public static boolean RECORD_BLOCKS, REAL_CHANGES;
    public static boolean RECORD_ITEMS, RECORD_ENTITIES;
    public static boolean RECORD_CHAT;
    public static boolean SAVE_STOP, RECORD_STARTUP, USE_OFFLINE_SKINS, HIDE_PLAYERS, UPDATE_NOTIFY, USE_DATABASE, ADD_PLAYERS;
    public static boolean WORLD_RESET;

    public static CompressType RECORD_COMPRESSION;

    public static ReplayProgression PROGRESS_TYPE = ReplayProgressType.XP_BAR;

    public static boolean USE_S3;

    public static boolean USE_MONGO;


    public static ReplayQuality QUALITY = ReplayQuality.HIGH;

    public static String CHAT_FORMAT;

    public static void loadConfigs() {
        // SQL Config
        if (!sqlFile.exists()) {
            sqlCfg.set("host", "localhost");
            sqlCfg.set("port", 3306);
            sqlCfg.set("username", "username");
            sqlCfg.set("database", "database");
            sqlCfg.set("password", "password");
            sqlCfg.set("prefix", "");
            sqlCfg.set("properties", MySQLDatabase.DEFAULT_PROPERTIES);

            try {
                sqlCfg.save(sqlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // S3 Config
        if (!s3File.exists()) {
            s3Cfg.set("endpoint_url", "https://example.com/");
            s3Cfg.set("access_key", "123qwertz456");
            s3Cfg.set("secret_key", "987yxcv654");
            s3Cfg.set("bucket_name", "replays");

            try {
                s3Cfg.save(s3File);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // MongoDB
        if (!mongoFile.exists()) {
            mongoCfg.set("username", "username");
            mongoCfg.set("password", "password");
            mongoCfg.set("host", "localhost");
            mongoCfg.set("port", 27017);
            mongoCfg.set("database", "replay");
            mongoCfg.set("collection", "replays");

            try {
                mongoCfg.save(mongoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Default config
        if (!file.exists()) {
            cfg.set("general.max_length", 3600);
            cfg.set("general.record_on_startup", false);
            cfg.set("general.save_on_stop", false);
            cfg.set("general.use_mysql", false);
            cfg.set("general.use_s3", false);
            cfg.set("general.use_mongo", false);
            cfg.set("general.use_offline_skins", false);
            cfg.set("general.quality", "high");
            cfg.set("general.cleanup_replays", -1);
            cfg.set("general.hide_players", false);
            cfg.set("general.add_new_players", false);
            cfg.set("general.update_notifications", true);

            cfg.set("replaying.world.reset_changes", false);
            cfg.set("replaying.progress_display", ReplayProgressType.getDefault().name().toLowerCase());

            cfg.set("recording.blocks.enabled", true);
            cfg.set("recording.blocks.real_changes", true);
            cfg.set("recording.entities.enabled", false);
            cfg.set("recording.entities.items.enabled", true);
            cfg.set("recording.chat.enabled", false);
            cfg.set("recording.chat.format", "&r<{name}> {message}");
            cfg.set("recording.file.compress", 0);

            try {
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ItemConfig.loadConfig();
        Messages.loadMessages();

        loadData(true);
    }

    public static void loadData(boolean initial) {
        MAX_LENGTH = cfg.getInt("general.max_length");
        SAVE_STOP = cfg.getBoolean("general.save_on_stop");
        RECORD_STARTUP = cfg.getBoolean("general.record_on_startup", false);
        USE_OFFLINE_SKINS = cfg.getBoolean("general.use_offline_skins");
        QUALITY = ReplayQuality.valueOf(cfg.getString("general.quality", "high").toUpperCase());
        HIDE_PLAYERS = cfg.getBoolean("general.hide_players");
        CLEANUP_REPLAYS = cfg.getInt("general.cleanup_replays", -1);
        ADD_PLAYERS = cfg.getBoolean("general.add_new_players");
        UPDATE_NOTIFY = cfg.getBoolean("general.update_notifications");
        if (initial) USE_DATABASE = cfg.getBoolean("general.use_mysql");
        USE_MONGO = cfg.getBoolean("general.use_mongo");

        WORLD_RESET = cfg.getBoolean("replaying.world.reset_changes", false);

        CHAT_FORMAT = cfg.getString("recording.chat.format");
        RECORD_BLOCKS = cfg.getBoolean("recording.blocks.enabled");
        REAL_CHANGES = cfg.getBoolean("recording.blocks.real_changes");
        RECORD_ITEMS = cfg.getBoolean("recording.entities.items.enabled");
        RECORD_ENTITIES = cfg.getBoolean("recording.entities.enabled");
        RECORD_CHAT = cfg.getBoolean("recording.chat.enabled");
        RECORD_COMPRESSION = CompressType.fromInt(cfg.getInt("recording.file.compression"));

        PROGRESS_TYPE = ReplayProgressType.valueOf(cfg.getString("replaying.progress_display", ReplayProgressType.getDefault().name()).toUpperCase());

        USE_S3 = cfg.getBoolean("general.use_s3");

        if (USE_MONGO) {
            String username = mongoCfg.getString("username");
            String password = mongoCfg.getString("password");
            String host = mongoCfg.getString("host");
            int port = mongoCfg.getInt("port");
            String database = mongoCfg.getString("database");
            String collectionName = mongoCfg.getString("collection");
            try {
                if (password != null && !password.isEmpty())
                    password = URLEncoder.encode(password, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }

            MongoDatabase mongo = new MongoDatabase(host, port, database, collectionName, username, password);
            DatabaseRegistry.registerDatabase(mongo);
        } else if (USE_DATABASE) {
            String host = sqlCfg.getString("host");
            int port = sqlCfg.getInt("port", 3306);
            String username = sqlCfg.getString("username");
            String database = sqlCfg.getString("database");
            String password = sqlCfg.getString("password");
            String prefix = sqlCfg.getString("prefix", "");
            List<String> properties = (List<String>) sqlCfg.getList("properties", MySQLDatabase.DEFAULT_PROPERTIES);

            MySQLDatabase mysql = new MySQLDatabase(host, port, database, username, password, prefix, properties);
            DatabaseRegistry.registerDatabase(mysql);
            DatabaseRegistry.getDatabase().getService().createReplayTable();
        }

        ItemConfig.loadData();
    }

    public static void reloadConfig() {
        try {
            cfg.load(file);
            ItemConfig.cfg.load(ItemConfig.file);
            Messages.loadMessages();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        loadData(false);
    }

    public static FilterOutputStream getCompressOutputStream(OutputStream out) throws java.io.IOException {
        return switch (RECORD_COMPRESSION) {
            case LZ4 -> new LZ4FrameOutputStream(out);
            case ZSTD -> new ZstdOutputStream(out, 7);
            default -> new GZIPOutputStream(out);
        };
    }

    public static FilterInputStream getCompressInputStream(InputStream in) throws java.io.IOException {
        var result = CompressType.detectCompression(in);
        return switch (result.compressType()) { // Automatic identification
            case LZ4 -> new LZ4FrameInputStream(result.inputStream());
            case ZSTD -> new ZstdInputStream(result.inputStream());
            default -> new GZIPInputStream(result.inputStream());
        };
    }
}
