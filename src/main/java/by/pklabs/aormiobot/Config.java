package by.pklabs.aormiobot;


import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_PATH = "data/config.json";
    private Map<String, String> config;
    private static Config instance = null;

    private Config(){
        readConfig();
    }

    public static Config getInstance(){
        if(instance == null){
            instance = new Config();
        }
        return instance;
    }

    private void readConfig(){
        Gson parser = new Gson();
        File configFile = new File(CONFIG_PATH);
        try(FileReader reader = new FileReader(configFile)){
            this.config = parser.fromJson(reader, HashMap.class);
        } catch (FileNotFoundException e) {
            logger.error("Config file not found, please create config.json file");
        } catch (IOException e) {
            logger.error(e.toString());
        }
        logger.info("Loaded config");
    }

    public String get(String key){
        return config.get(key);
    }




}
