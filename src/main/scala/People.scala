import slick.jdbc.MySQLProfile.api._

class People(tag: Tag) extends Table[(Int, String, String, Int, Int,String, String)](tag, "PEOPLE") {
  def id = column[Int]("PER_ID", O.PrimaryKey, O.AutoInc)
  def fName = column[String]("PER_FNAME")
  def lName = column[String]("PER_LNAME")
  def age = column[Int]("PER_AGE")
  def houseNumber = column[Int]("PER_HOUSE_NUMBER")
  def address = column[String]("PER_STREET")
  def city = column[String]("PER_CITY")
  def * = (id, fName, lName, age, houseNumber, address, city)
}
