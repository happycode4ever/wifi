package com.jj.wifi.spark.streaming

import kafka.consumer
import kafka.serializer.StringDecoder
import org.apache.hadoop.hive.ql.exec.spark.session.SparkSession
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingTest {
  def main(args: Array[String]): Unit = {
    val sc = new SparkConf().setMaster("local[4]").setAppName("StreamingTest")
    val ssc = new StreamingContext(sc,Seconds(2))
    ssc.sparkContext.setLogLevel("ERROR")
    val kafkaParams = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "cdh126:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.GROUP_ID_CONFIG -> "g1",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "smallest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "true"

    )
    val kafkaDStream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,Set("wifi"))
    kafkaDStream.foreachRDD(rdd =>{
      val ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      ranges.foreach(range =>{
        println(s"range -> ${range}")
      })
      rdd.foreach({case (k,v) => {
        println(s"key -> $k value -> $v")
      }})
    })

    //开启ssc
    ssc.start()
    ssc.awaitTermination()
  }
}
