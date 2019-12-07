import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter

import com.jj.wifi.props.kafka.ParseData
import com.jj.wifi.spark.streaming.hive.HiveUtil
import org.apache.spark.sql.SaveMode
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

import scala.collection.mutable

class StreamingToHiveTest {
  @Test
  def testJson: Unit ={
    val json = "{\"rksj\":\"1575556013\",\"latitude\":\"24.000000\",\"imsi\":\"000000000000000\",\"accept_message\":\"\",\"phone_mac\":\"aa-aa-aa-aa-aa-aa\",\"device_mac\":\"bb-bb-bb-bb-bb-bb\",\"message_time\":\"1789098762\",\"filename\":\"qq_source1_11112553422.txt\",\"phone\":\"18609765432\",\"absolute_filename\":\"/opt/project/wifi/ftp/success/2019-12-05/qq_source1_11112553422.txt\",\"device_number\":\"32109231\",\"imei\":\"000000000000000\",\"collect_time\":\"1557305988\",\"id\":\"6ba07cccf0c241cab7c1dc816e76c119\",\"send_message\":\"\",\"object_username\":\"judy\",\"table\":\"qq\",\"longitude\":\"23.000000\",\"username\":\"andiy\"}"
    val collectDateMap: mutable.Map[String, mutable.Set[String]] = mutable.Map()
    //解析json
    val javaMap = ParseData.convertData(json)
    //收集采集时间字段并且去重
    val currentTable = javaMap.get("table").toString
    val collectTime = javaMap.get("collect_time").toString.toLong * 1000
    val localDate = Instant.ofEpochMilli(collectTime).atOffset(ZoneOffset.ofHours(8)).toLocalDate
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val collectDate = localDate.format(formatter)
    collectDateMap.get(currentTable) match {
      case None => collectDateMap += (currentTable -> mutable.Set(collectDate))
      case Some(set) => set += collectDate
    }
    println(collectDateMap)
  }
  @Test
  def testDf: Unit ={
    val sparkConf = new SparkConf().setMaster("local[4]").setAppName("StreamingToHiveTest")
    val sc = new SparkContext(sparkConf)
    val hiveContext = HiveUtil.getHiveContext(sc)
    val json = "{\"rksj\":\"1575556013\",\"latitude\":\"24.000000\",\"imsi\":\"000000000000000\",\"accept_message\":\"aaa\",\"phone_mac\":\"aa-aa-aa-aa-aa-aa\",\"device_mac\":\"bb-bb-bb-bb-bb-bb\",\"message_time\":\"1789098762\",\"filename\":\"qq_source1_11112553422.txt\",\"phone\":\"18609765432\",\"absolute_filename\":\"/opt/project/wifi/ftp/success/2019-12-05/qq_source1_11112553422.txt\",\"device_number\":\"32109231\",\"imei\":\"000000000000000\",\"collect_time\":\"1557305988\",\"id\":\"6ba07cccf0c241cab7c1dc816e76c119\",\"send_message\":\"aaaa\",\"object_username\":\"judy\",\"table\":\"qq\",\"longitude\":\"23.000000\",\"username\":\"andiy\"}"
    val javaMap = ParseData.convertData(json)

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


    val rdd = sc.makeRDD(Array(javaMap))
    val structType = HiveUtil.getSortedStructType(javaMap.get("table").toString)
    val rowRDD = rdd.map(javaMap => HiveUtil.convertJsonToSortedFieldRow(javaMap))
    val df = hiveContext.createDataFrame(rowRDD,structType)

    val table = javaMap.get("table").toString
    val year = collectDate.substring(0, 4)
    val month = collectDate.substring(4, 6)
    val day = collectDate.substring(6, 8)
    val hdfsTablePartitionPath = s"hdfs://cdh124:8020/user/hive/wifi/external/$table/$year/$month/$day"
    df.write.mode(SaveMode.Append).parquet(hdfsTablePartitionPath)

    val partitions = Array(("year", year), ("month", month), ("day", day))
    val addPartitionSql = HiveUtil.generateAddPartititionSql(table, partitions, hdfsTablePartitionPath)
    hiveContext.sql(addPartitionSql)

  }

  @Test
  def testLong: Unit ={
    val sparkConf = new SparkConf().setMaster("local[4]").setAppName("StreamingToHiveTest")
    val sc = new SparkContext(sparkConf)
    val hiveContext = HiveUtil.getHiveContext(sc)
    val df = hiveContext.sql("select * from qq")
    df.printSchema()
  }
}
