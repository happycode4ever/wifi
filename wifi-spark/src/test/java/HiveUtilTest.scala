import com.jj.wifi.props.kafka.{ParseData, SortedProps}
import com.jj.wifi.spark.streaming.hive.HiveUtil
import org.junit.Test

class HiveUtilTest {
  @Test
  def test1(): Unit = {
//    val str = List(HiveParam("name","aaa"),HiveParam("age","1111")).map(field => field.name + " " + field.dataType).mkString(",")
//    println(str)
    val fields = Array(("id","int"),("name","age"))
    val partitions  = Array(("year","int"),("month","int"))
    val location = "hdfs://hadoop:9000/user/hive/warehouse/wifi/external/qq"
    val str = HiveUtil.generateCreateTableSql("emp",fields,partitions,location)
    println(str)
    val str2 = HiveUtil.generateAddPartititionSql("emp",partitions)
    println(str2)

  }
  @Test
  def sortedFieldTest: Unit ={
    val arr = HiveUtil.getSortedFieldArray("qq")
    println(arr.mkString(","))
    val arr2 = SortedProps.getCompositeConf.getStringArray("tables")
    println(arr2.mkString(","))
  }
  @Test
  def convertTest: Unit ={
    val json = "{\"rksj\":\"1575534428\",\"latitude\":\"24.000000\",\"imsi\":\"000000000000000\",\"accept_message\":\"\",\"phone_mac\":\"aa-aa-aa-aa-aa-aa\",\"device_mac\":\"bb-bb-bb-bb-bb-bb\",\"message_time\":\"1789098762\",\"filename\":\"qq_source1_1111162.txt\",\"absolute_filename\":\"H:\\\\bigdata-dev\\\\ideaworkspace\\\\tanzhou\\\\wifi-root\\\\wifi-resources\\\\target\\\\classes\\\\test.data\\\\qq\\\\qq_source1_1111162.txt\",\"phone\":\"18609765432\",\"device_number\":\"32109231\",\"imei\":\"000000000000000\",\"id\":\"0a6afc142161446b826eb0d0d5d36d4c\",\"collect_time\":\"1557305988\",\"send_message\":\"\",\"table\":\"qq\",\"object_username\":\"judy\",\"longitude\":\"23.000000\",\"username\":\"andiy\"}"
    val map = HiveUtil.convertJsonToSortedFieldRow(ParseData.convertData(json))
    println(map)
  }
}
