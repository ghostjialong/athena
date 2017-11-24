package com.athena.store.driver;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by wangjialong on 11/19/17.
 */
public class TestMongoConnector {

    private MongoConnector mongoConnector;

    @Before
    public void setUp() {
        System.out.println("test begin......");
        mongoConnector = new MongoConnector();
        System.out.println("connect finish.....");
    }

    @Test
    public void testMongoInsert() {
        MongoClient mongoClient = mongoConnector.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase("athena");
        MongoCollection coll = db.getCollection("mycol");
        Document doc = new Document("name", "mongo").append("info", new Document("ver", "3.0"));
        coll.insertOne(doc);
    }

    @Test
    public void testMongoInsertMethod() {
        System.out.println("fuck");
        mongoConnector.insertOne("test_db", new Document(
                "name", "mongo"
        ).append("info", new Document("ver", "3.0")));
    }
}
