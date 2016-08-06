package com.example.riskcontrol.util;

import org.bson.BsonReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;

import java.util.Date;

/**
 * 特殊处理codec类
 * Created by sunpeak on 2016/8/6.
 */
public class DocumentDecoder extends DocumentCodec {
    @Override
    public Document decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = super.decode(reader, decoderContext);

        //特殊处理operateTime，时间格式
        String operateTime = "operateTime";
        long time = document.getLong(operateTime);
        document.put(operateTime, new Date(time));

        return document;
    }
}
