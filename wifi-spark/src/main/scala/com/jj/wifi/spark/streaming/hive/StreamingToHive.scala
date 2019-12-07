package com.jj.wifi.spark.streaming.hive

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import com.jj.wifi.props.kafka.{ParseData, Props, SortedProps}
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{DefaultKafkaManager, KafkaUtils}
import org.slf4j.LoggerFactory

import scala.collection.mutable

object StreamingToHive {
  private val logger = LoggerFactory.getLogger(StreamingToHive.getClass)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[4]").setAppName("StreamingTest")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val sc = ssc.sparkContext
        sc.setLogLevel("ERROR")
    val hiveContext = HiveUtil.getHiveContext(sc)

    //driver端优先创建hive表
    SortedProps.getCompositeConf.getStringArray("tables").foreach(table => {
      //获取字段映射
      val fieldArr = HiveUtil.getSortedFieldArray(table)
      //构建分区目录
      val partitionsArr = Array(("year", "int"), ("month", "int"), ("day", "int"))
      val hdfsTablePath = s"hdfs://cdh124:8020/user/hive/wifi/external/$table"
      val createTableSql = HiveUtil.generateCreateTableSql(table, fieldArr, partitionsArr, hdfsTablePath)
      //执行sql
      hiveContext.sql(createTableSql)
    })

    val kafkaParams = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "cdh126:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.GROUP_ID_CONFIG -> "g1",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "smallest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "true"

    )
    val kafkaManager = new DefaultKafkaManager(kafkaParams)
    val kafkaDStream = kafkaManager.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, Set("wifi"))

    val tables = SortedProps.getCompositeConf.getStringArray("tables")
    //用于临时存储每个类型的采集时间的map
//    val collectDateMap: mutable.Map[String, mutable.Set[String]] = mutable.Map()
    kafkaDStream.foreachRDD(rdd => {
      //根据数据源的采集时间和数据类型动态构建hive的sql

      //分别按照数据类型筛选rdd
      tables.foreach(table => {
        val filterTableRDD = rdd.map({ case (k, v) => {
          logger.error(s"table -> $table json -> $v")
          //解析json
          val javaMap = ParseData.convertData(v)
          //收集采集时间字段并且去重
          val currentTable = javaMap.get("table").toString
          val collectTime = javaMap.get("collect_time").toString.toLong * 1000
          val localDate = Instant.ofEpochMilli(collectTime).atOffset(ZoneOffset.ofHours(8)).toLocalDate
          val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
          val collectDate = localDate.format(formatter)
//          collectDateMap.get(currentTable) match {
//            case None => collectDateMap += (currentTable -> mutable.Set(collectDate))
//            case Some(set) => set += collectDate
//          }
          javaMap.put("collect_date", collectDate)
          javaMap.put("year",collectDate.substring(0,4))
          javaMap.put("month",collectDate.substring(4,6))
          javaMap.put("day",collectDate.substring(6,8))
          logger.error(s"table -> $table current -> $currentTable javamap -> $javaMap collectDate -> $collectDate")
          javaMap
        }
        })
          .filter(map => {
            logger.error(s"curr -> ${map.get("table")} table -> $table ${table.equals(map.get("table"))}")
            table.equals(map.get("table"))
          })
        logger.error(s"table -> $table filterRDD empty:{}",filterTableRDD.isEmpty())
        if (!filterTableRDD.isEmpty()) {
          //筛选完table类型的rdd再次筛选采集时间的rdd组装row
          val collectDateArray = filterTableRDD.map(javaMap => javaMap.get("collect_date").toString).distinct().collect()
          collectDateArray.foreach(collectDate => {
            //构建row
            val rowRDD = filterTableRDD
              .filter(javaMap => collectDate.equals(javaMap.get("collect_date")))
              .map(javaMap => HiveUtil.convertJsonToSortedFieldRow(javaMap))
            //根据rowRDD和schema构建df

            logger.error(s"rowRDD empty:{}",rowRDD.isEmpty())
            val df = hiveContext.createDataFrame(rowRDD, HiveUtil.getSortedStructType(table))

            //构建分区实际值
            val year = collectDate.substring(0, 4)
            val month = collectDate.substring(4, 6)
            val day = collectDate.substring(6, 8)
            val hdfsTablePartitionPath = s"hdfs://cdh124:8020/user/hive/wifi/external/$table/$year/$month/$day"
            val partitions = Array(("year", year), ("month", month), ("day", day))
            val addPartitionSql = HiveUtil.generateAddPartititionSql(table, partitions, hdfsTablePartitionPath)

            //df存入数据
            df.write.mode(SaveMode.Append).parquet(hdfsTablePartitionPath)
            //执行分区sql
            hiveContext.sql(addPartitionSql)
            df.show(2)
          })
        }

      })
    })


    //启动ssc
    ssc.start()
    ssc.awaitTermination()
  }
}
