package com.example.riskcontrol.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Aggregates.*;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Repository
public class MongoDao {

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String database;

    /**
     * 数据库连接
     *
     * @return
     */
    private MongoDatabase getDB() {
        return mongoClient.getDatabase(database);
    }

    /**
     * 新增document
     *
     * @param collectionName
     * @param document
     */
    public void insert(String collectionName, Document document) {
        getDB().getCollection(collectionName).insertOne(document);
    }

    /**
     * 统计次数
     *
     * @param collectionName
     * @param filter
     * @return
     */
    public int count(String collectionName, Document filter) {
        return (int) getDB().getCollection(collectionName).count(filter);
    }

    /**
     * 统计频数
     *
     * @param collectionName
     * @param match
     * @param distinctField
     * @return
     */
    public int distinctCount(String collectionName, Document match, String distinctField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, addToSet("_array", "$" + distinctField))
                        , project(new Document("_num", new Document("$size", "$_array")))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.getInteger("_num");
        }
        return 0;
    }

    /**
     * 获取不一样的记录
     *
     * @param collectionName
     * @param match
     * @param distinctField
     * @return
     */
    public List distinct(String collectionName, Document match, String distinctField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, addToSet("_array", "$" + distinctField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return (List) first.get("_array");
        }
        return null;
    }


    /**
     * 最大统计
     *
     * @param collectionName
     * @param match
     * @param maxField
     * @return
     */
    public Object max(String collectionName, Document match, String maxField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, Accumulators.max("_max", "$" + maxField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.get("_max");
        }
        return null;
    }

    /**
     * 最小统计
     *
     * @param collectionName
     * @param match
     * @param minField
     * @return
     */
    public Object min(String collectionName, Document match, String minField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, Accumulators.min("_min", "$" + minField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.get("_min");
        }
        return null;
    }

    /**
     * 合统计
     *
     * @param collectionName
     * @param match
     * @param sumField
     * @return
     */
    public Double sum(String collectionName, Document match, String sumField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, Accumulators.sum("_sum", "$" + sumField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.getDouble("_sum");
        }
        return null;
    }

    /**
     * 平均统计
     *
     * @param collectionName
     * @param match
     * @param avgField
     * @return
     */
    public Double avg(String collectionName, Document match, String avgField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, Accumulators.avg("_avg", "$" + avgField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.getDouble("_avg");
        }
        return null;
    }


    /**
     * 最早统计
     *
     * @param collectionName
     * @param match
     * @param firstField
     * @param sort
     * @return
     */
    public Object first(String collectionName, Document match, String firstField, Document sort) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , sort(sort)
                        , group(null, Accumulators.first("_first", "$" + firstField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.get("_first");
        }
        return null;
    }

    /**
     * 最近统计
     *
     * @param collectionName
     * @param match
     * @param lastField
     * @param sort
     * @return
     */
    public Object last(String collectionName, Document match, String lastField, Document sort) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , sort(sort)
                        , group(null, Accumulators.last("_last", "$" + lastField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.get("_last");
        }
        return null;
    }


    /**
     * 标准差统计
     *
     * @param collectionName
     * @param match
     * @param stdDevField
     * @return
     */
    public Double stdDevPop(String collectionName, Document match, String stdDevField) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group(null, Accumulators.stdDevPop("_stdDev", "$" + stdDevField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.getDouble("_stdDev");
        }
        return null;
    }


    /**
     * 采样标准差统计
     *
     * @param collectionName
     * @param match
     * @param stdDevField
     * @param sampleSize
     * @return
     */
    public Double stdDevSamp(String collectionName, Document match, String stdDevField, int sampleSize) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , sample(sampleSize)
                        , group(null, Accumulators.stdDevSamp("_stdDev", "$" + stdDevField))
                )
        );
        Document first = aggregate.first();
        if (first != null) {
            return first.getDouble("_stdDev");
        }
        return null;
    }


    /**
     * 根据统计字段计算统计结果（gte最小值）并排序
     *
     * @param collectionName 集合名
     * @param match          match条件
     * @param field          统计字段
     * @param minCount       最小值
     * @return
     */
    public LinkedHashMap<String, Integer> sortMap(String collectionName, Document match, String field, int minCount) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)
                        , group("$" + field, Accumulators.sum("_count", 1))
                        , match(new Document("_count", new Document("$gte", minCount)))
                        , sort(new Document("_count", -1))
                )
        );

        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        MongoCursor<Document> iterator = aggregate.iterator();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            map.put(next.getString("_id"), next.getInteger("_count"));
        }
        return map;
    }


    /**
     * 统计值 是否在统计结果（gte最小值）中
     *
     * @param collectionName 集合名
     * @param match          match条件
     * @param field          统计字段
     * @param value          统计值
     * @param minCount       最小值
     * @return
     */
    public boolean inSortMap(String collectionName, Document match, String field, Object value, int minCount) {
        AggregateIterable<Document> aggregate = getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match.append(field, value))
                        , group("$" + field, Accumulators.sum("_count", 1))
                        , match(new Document("_count", new Document("$gte", minCount)))
                )
        );

        Document first = aggregate.first();
        return first == null ? false : true;
    }

}
