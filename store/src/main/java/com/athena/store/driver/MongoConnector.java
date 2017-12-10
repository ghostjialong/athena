package com.athena.store.driver;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang.StringUtils;
import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import static com.mongodb.WriteConcern.ACKNOWLEDGED;

/**
 * Created by wangjialong on 11/19/17.
 */
@Component
public class MongoConnector {

    public static String clientUrl = "";
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static String database;

    private Logger logger = Logger.getLogger("MongoConnector.class");

    static {
        try {
            String conf = System.getProperty("athena.conf", "classpath:athena.conf");
            Properties properties = new Properties();
            String configName = StringUtils.substringAfter(conf, "classpath:");
            properties.load(MongoConnector.class.getClassLoader().getResourceAsStream(configName));
            clientUrl = properties.getProperty("clientUrl");
            database  = properties.getProperty("database");
            MongoClientURI uri = new MongoClientURI(clientUrl, new MongoClientOptions.Builder()
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .connectionsPerHost(100)
                    .serverSelectionTimeout(1000)
                    .threadsAllowedToBlockForConnectionMultiplier(50)
                    .readPreference(ReadPreference.primaryPreferred())
                    .writeConcern(ACKNOWLEDGED));
            MongoClient mongoClient = new MongoClient(uri);
            mongoDatabase = mongoClient.getDatabase(database);
        } catch (IOException e) {

        }
    }


    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void insertOne(String collection, Document document) {
        try {
            MongoCollection mongoCollection = mongoDatabase.getCollection(collection);
            mongoCollection.insertOne(document);
        } catch (com.mongodb.MongoWriteException e) {
            e.printStackTrace();
        } catch (MongoWriteConcernException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }


    public void update(String collection, Bson filter, Bson document) {
        MongoCollection mongoCollection = mongoDatabase.getCollection(collection);
        UpdateResult result = mongoCollection.updateMany(filter, document);
    }
}
