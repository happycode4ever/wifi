import java.time.{Instant, LocalDate, ZoneId, ZoneOffset}

import org.junit.Test

import scala.collection.mutable

class LocalDateTest {
  @Test
  def dateTest: Unit ={
    //1557305988000
//    val localDate = Instant.ofEpochMilli(1557305988000L).atZone(ZoneOffset.ofHours(8)).toLocalDate
    val localDate = Instant.ofEpochMilli(1557305988000L).atZone(ZoneId.systemDefault()).toLocalDate
    val localDate2 = Instant.ofEpochMilli(1557305988111L).atZone(ZoneId.systemDefault()).toLocalDate
    println(localDate)
    println(localDate2)
    println(localDate == localDate2)
    val list = List(localDate,localDate2).distinct
    println(list)
  }

  @Test
  def collectTest: Unit ={
    val collectDateMap = mutable.Map("qq" -> mutable.Set("20191210"))
    val localDate = "20191210"
    val currentTable = "mail"
    collectDateMap.get(currentTable) match {
          case None => collectDateMap += (currentTable -> mutable.Set(localDate))
          case Some(set) => set += localDate
    }
    println(collectDateMap)
  }

}
