import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {

  val db = Database.forConfig("mysqlDB")

  val peopleTable = TableQuery[People]

  val dropPeopleCmd = DBIO.seq(peopleTable.schema.drop)

  val initPeopleCmd = DBIO.seq(peopleTable.schema.create)



  def dropDB = {
    val dropFuture = Future{db.run(dropPeopleCmd)}
    Await.result(dropFuture, Duration.Inf).andThen{
      case Success(_) =>  initialisePeople
      case Failure(error) => println("Dropping the table failed due to: " + error.getMessage)
        initialisePeople
    }
  }

  def initialisePeople = {
    val setupFuture =  Future {
      db.run(initPeopleCmd)
    }

    Await.result(setupFuture, Duration.Inf).andThen{
      case Success(_) => runQuery
      case Failure(error) => println("Initialising the table failed due to: " + error.getMessage)
    }
  }

  def runQuery = {
    val insertPeople = Future {
      val query = peopleTable ++= Seq(

        (10, "Jack", "Wood", 36, 3, "Maple Drive", "Wakefield"),
        (20, "Tim", "Brown", 24, 6,"Green Avenue", "Wakefield")
      )
      println(query.statements.head)
      db.run(query)
    }
    Await.result(insertPeople, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
  }

  def listPeople = {
    val queryFuture = Future {

      db.run(peopleTable.result).map(_.foreach {
        case (id, fName, lName, age, houseNumber, address, city) => println(s" $id $fName $lName $age $houseNumber $address $city")})
    }
    Await.result(queryFuture, Duration.Inf).andThen {
      case Success(_) =>  db.close()
      case Failure(error) => println("Listing people failed due to: " + error.getMessage)
    }
  }

  def updatePerson() = {

    val updateFuture = Future {
      val q = for {c <- peopleTable if c.fName === "Jack"} yield c
      val updateAction = q.update(41, "Donny", "Soldier", 12, 11,"Maple Avenue", "Doncaster")

      db.run(updateAction)
    }
    Await.result(updateFuture, Duration.Inf).andThen {
      case Success(_) =>  db.close()
        println("Update successful")
      case Failure(error) => println("Update failed due to: " + error.getMessage)
    }


  }

  def deletePerson(id: Int) = {
    val deleteFuture = Future {
      val q = peopleTable.filter(_.id === id)
      val action = q.delete
      db.run(action)
    }

    Await.result(deleteFuture, Duration.Inf).andThen {
      case Success(_) =>  db.close()
        println("Deletion successful")
      case Failure(error) => println("Delete failed due to: " + error.getMessage)
    }
  }

  def selectPerson(firstName: String) = {
    val readFuture = Future {

      db.run(peopleTable.result).map(_.foreach{person =>
        if (person._2.equals(firstName))
        println(s"${person._2} ${person._3}")
      })

    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()
      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }
  }

  def addPerson(firstName: String, lastName: String, age: Int, houseNumber: Int, address: String, city: String): Unit = {
    val insertPeople = Future {
      val query = peopleTable ++= Seq((11, firstName, lastName, age, houseNumber,address, city))
      db.run(query)
    }
    Await.result(insertPeople, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
  }

  def count = {

    val readFuture = Future {
      db.run(peopleTable.size.result).map(println)

    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()
      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }
  }

  def avgAge = {

    val readFuture = Future {
      db.run(peopleTable.map(_.age).avg.result).map(println)
    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()

      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }

  }

  def modeFirstName() = {
    val readFuture = Future {
      db.run(peopleTable.result).map(_.groupBy(person => person._2).mapValues(_.size).maxBy(_._2)._1).map(println)
    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()

      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }

  }

  def modeLastName = {
    val readFuture = Future {
      db.run(peopleTable.result).map(_.groupBy(person => person._3).mapValues(_.size).maxBy(_._2)._1).map(println)
    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()

      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }

  }

  def modeCity = {
    val readFuture = Future {
      db.run(peopleTable.result)
        .map(_.groupBy(person => person._6).mapValues(_.size).maxBy(_._2)._1)
        .map(println)
    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()

      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }

  }

  def neighbours = {
    val readFuture = Future {
      db.run(peopleTable.result)
        .map(_.groupBy(person => (person._6, person._7)).mapValues(_.size))
        .map(_.foreach{element =>
          if (element._2 > 1){
            println(s"Neighbours at ${element._1._1} in ${element._1._2}")
          }
        })
    }

    Await.result(readFuture, Duration.Inf).andThen {
      case Success(_) => db.close()

      case Failure(error) => println("Read failed due to: " + error.getMessage)
    }
  }


neighbours




  Thread.sleep(10000)

}
