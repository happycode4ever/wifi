package com.jj.wifi.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingTest {
  def main(args: Array[String]): Unit = {
    val sc = new SparkConf().setMaster("local[4]").setAppName("StreamingTest")
    val ssc = new StreamingContext(sc,Seconds(2))


    //开启ssc
    ssc.start()
    ssc.awaitTermination()
  }
}
