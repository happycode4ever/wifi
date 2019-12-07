package com.jj.wifi.spark.streaming.hive

import com.alibaba.fastjson.{JSON, TypeReference}
import com.jj.wifi.props.kafka.{Props, SortedProps}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hive.ql.exec.spark.session.SparkSessionImpl
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters
import scala.collection.immutable.ListMap
import scala.collection.mutable.ArrayBuffer

object HiveUtil {
  private val logger = LoggerFactory.getLogger(HiveUtil.getClass)

  private val createSimpleTableStr = "create table if not exists emp(id int, name string) stored as parquet location '/user/hive/warehouse'"
  private val createParTableStr = "create table if not exists emp(id int, name string) partitioned by (year string, month string) stored as parquet location '/user/hive/warehouse'"
  private val addParStr = "alter table emp add if not exists partition(year='2019',month='10') location ''"

  /**
    * 获取低版本spark1.6需要的hiveContext，其中需要手动导入配置文件
    * @param sc
    * @return
    */
  def getHiveContext(sc: SparkContext): HiveContext = {
    val conf = new Configuration()
    conf.addResource("conf/hive/core-site.xml")
    conf.addResource("conf/hive/hdfs-site.xml")
    conf.addResource("conf/hive/hive-site.xml")
    val hiveContext = new HiveContext(sc)
    val it = conf.iterator()
    while (it.hasNext) {
      val entry = it.next()
      hiveContext.setConf(entry.getKey, entry.getValue)
    }
    hiveContext
  }

  /**
    * 动态构建hive外部表建表语句
    * @param table
    * @param fields
    * @param partitions
    * @param location
    * @return
    */
  def generateCreateTableSql(table: String, fields: Array[(String, String)], partitions: Array[(String, String)] = null, location: String = ""): String = {
    val fieldsStr = fields.map(field => field._1 + " " + field._2).mkString(",")
    val partitionsStr = if (partitions != null) {
      "partitioned by (" + partitions.map(partition => partition._1 + " " + partition._2).mkString(",") + ") "
    } else {
      ""
    }
    val sql = s"create external table if not exists $table(" +
      fieldsStr +
      ") " +
      partitionsStr +
      "stored as parquet " +
      "location '" + location + "'"
    logger.error(sql)
    sql
  }

  /**
    * 动态构建添加分区语句
    * @param table
    * @param partitions
    * @param location
    * @return
    */
  def generateAddPartititionSql(table: String, partitions: Array[(String, String)], location: String = ""): String = {
    val partitionsStr = if (partitions == null || partitions.isEmpty) {
      throw new Exception("missing params for adding partitions")
    } else {
      " add if not exists partition(" + partitions.map(par => s"${par._1}='${par._2}'").mkString(",") + ")"
    }
    val locationStr = if (location != "") {
      s" location '$location'"
    } else {
      ""
    }

    val sql = "alter table " +
      table +
      partitionsStr +
      locationStr

    logger.error(sql)
    sql
  }

  /**
    * 将乱序的javamap数据转换为配置文件的字段顺序并封装成row
    *
    * @param javaMap
    * @return
    */
  def convertJsonToSortedFieldRow(javaMap: java.util.Map[String, AnyRef]): Row = {
    val scalaMap = JavaConverters.mapAsScalaMapConverter(javaMap).asScala
    val table = scalaMap("table").toString
    //按照数据类型获取对应的key列表
    val iterator = SortedProps.getCompositeConf.getKeys(table)
    val arr: ArrayBuffer[Any] = ArrayBuffer()
    while (iterator.hasNext) {
      val key = iterator.next().toString
      val field = key.split("\\.")(1)
      arr += scalaMap.get(field).get
    }
//    arr += (scalaMap.get("year"))
//    arr += (scalaMap.get("month"))
//    arr += (scalaMap.get("day"))
    logger.error(s"row -> ${arr.mkString(",")}")
    Row.fromSeq(arr)
  }

  /**
    * 从fileMapping.properties按table开头的字段顺序构建hivesql需要的数据类型,例如Array(("id","int"),("name","string"))
    *
    * @param table
    * @return
    */
  def getSortedFieldArray(table: String): Array[(String, String)] = {
    val conf = SortedProps.getCompositeConf()
    val iterator = conf.getKeys(table)
    val arr: ArrayBuffer[(String, String)] = ArrayBuffer()
    while (iterator.hasNext) {
      val key = iterator.next().toString
      val value = conf.getString(key)
      val field = key.split("\\.")(1)
      val dataType = value match {
        case "string" => "string"
        case "double" => "double"
        case "long" => "bigint"
      }
      arr += ((field, dataType))
    }
    arr.toArray
  }

  /**
    * 按配置文件顺序构建schema
    *
    * @param table
    * @return
    */
  def getSortedStructType(table: String): StructType = {
    val arr = getSortedFieldArray(table)
    val structFields = arr.map({ case (field, dataTypeStr) => {
      val dataType = dataTypeStr match {
        case "long" => DataTypes.StringType
        case "double" => DataTypes.DoubleType
        case _ => DataTypes.StringType
      }
      StructField(field, dataType)
    }
    })
    logger.error(s"table -> $table schema -> ${arr.mkString(",")}")
    StructType(structFields)
  }
}
