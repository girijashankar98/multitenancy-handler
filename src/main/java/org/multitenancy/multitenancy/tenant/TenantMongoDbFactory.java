package org.multitenancy.multitenancy.tenant;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class TenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {

    public TenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName){
            return super.getMongoDatabase(TenantResolver.resolve() + "_" + dbName);
    }
}
