package com.jj.wifi.spark.streaming.es

import com.jj.wifi.es.client.ESUtils
import com.jj.wifi.props.kafka.{ParseData, Props}
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.DefaultKafkaManager
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingToES {
  def main(args: Array[String]): Unit = {
    val sc = new SparkConf().setMaster("local[4]").setAppName("StreamingTest")
    val ssc = new StreamingContext(sc, Seconds(2))
    ssc.sparkContext.setLogLevel("ERROR")
    val kafkaParams = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.GROUP_ID_CONFIG -> "g1",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "smallest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "true"

    )
    val kafkaManager = new DefaultKafkaManager(kafkaParams)
    val kafkaDStream = kafkaManager.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, Set("test"))

    kafkaDStream.foreachRDD(rdd => {
      //获取的消息每批rdd可能存在不同类型的数据 用filter代替groupby减少shuffle产生的IO
      val esFieldMappingProps = Props.getEsFieldMappingProps
      val tables = esFieldMappingProps.getProperty("tables").split(",")
      //每种数据类型存入各自对应的ES索引
      tables.foreach(table =>{
        val filterRDD = rdd.map({case (k,v) => {
          ParseData.convertData(v)
        }}).filter(map => map.get("table").eq(table))
        //每批数据都会判断与创建一次索引不合理 应该放在driver端执行
        /*val createIndexRequest = ESUtils.CreateIndexRequest.builder().
          index(table)
          .mappingJson("")
          .build()
        ESUtils.createIndexAndMapping(createIndexRequest)*/
      })
    })

    //开启ssc
    ssc.start()
    ssc.awaitTermination()
  }
}
