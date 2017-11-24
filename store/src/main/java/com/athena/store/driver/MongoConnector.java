package com.athena.store.driver;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

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
            MongoClientURI uri = new MongoClientURI(clientUrl);
            MongoClient mongoClient = new MongoClient(uri);
            MongoClientOptions options = new MongoClientOptions.Builder()
                    .connectTimeout(1)
                    .maxWaitTime(5).build();
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
        mongoCollection.updateOne(filter, document);
    }
}
