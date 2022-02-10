package by.pklabs.aormiobot.database;

import by.pklabs.aormiobot.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static Database instance;
    private final Config config = Config.getInstance();
    private HikariConfig hikariConfig = new HikariConfig();
    private HikariDataSource ds;
    private Connection con;


    private Database(){
        hikariConfig.setJdbcUrl(config.get("jdbcUrl"));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.get("databaseUser"));
        hikariConfig.setPassword(config.get("databasePassword"));
        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource(hikariConfig);
        try{
            this.con = ds.getConnection();
        } catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

    public static Database getInstance(){
        if (instance == null){
            instance = new Database();
        }
        return instance;
    }

    public ResultSet getAll(){
        try{
            if (con.isClosed()){
                con = ds.getConnection();
            }
            PreparedStatement ps = con.prepareStatement("SELECT * FROM muted_users", ResultSet.TYPE_SCROLL_INSENSITIVE
                    , ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = ps.executeQuery();
            return rs;
        }catch (SQLException e){
            logger.error(e.getMessage());
        }
        return null;
    }

    public void insertMuted(long userId, LocalDateTime unmuteTime){
        try{
            if (con.isClosed()){
                con = ds.getConnection();
            }
            logger.debug("Inserting in database");
            PreparedStatement ps = con.prepareStatement("INSERT INTO muted_users(user, unmute_time)"
                    + "VALUES(?, ?)");
            ps.setLong(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(unmuteTime));
            ps.executeUpdate();
            logger.debug("Inserted");
        }catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

    public void deleteMuted(long userId){
        try{
            if (con.isClosed()){
                con = ds.getConnection();
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM muted_users WHERE user = ?");
            ps.setLong(1, userId);
            ps.executeUpdate();
            logger.debug("Deleted");
        }catch (SQLException e){
            logger.error(e.getMessage());
        }
    }
}
