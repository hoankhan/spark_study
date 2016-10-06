package org.test.spark.MachineLearning

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

//Collaborative filtering: alternating least squares (ALS)

object ALSTest {
  def main(args: Array[String]) = {

//    System.setProperty("hadoop.home.dir", "E:\\Softs\\Hadoop2.7.1");
    println(System.getProperty("hadoop.home.dir"))

    //Start the Spark context
    val conf = new SparkConf()
      .setAppName("PiEstimation")
      .setMaster("local")
    val sc = new SparkContext(conf)
    
    val fileInput = "MovieLens_1000000r_3900i_6040u.txt"

    // Load and parse the data
    val data = sc.textFile(fileInput)
    val ratings = data.map(_.split(',') match {
      case Array(user, item, rate, timestemp) =>
        Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 10
    val model = ALS.train(ratings, rank, numIterations, 0.01)

    // Evaluate the model on rating data
    val usersProducts = ratings.map {
      case Rating(user, product, rate) =>
        (user, product)
    }
    val predictions =
      model.predict(usersProducts).map {
        case Rating(user, product, rate) =>
          ((user, product), rate)
      }
    val ratesAndPreds = ratings.map {
      case Rating(user, product, rate) =>
        ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map {
      case ((user, product), (r1, r2)) =>
        val err = (r1 - r2)
        err * err
    }.mean()
    println("Mean Squared Error = " + MSE)

    // Save and load model
    model.save(sc, fileInput + ".model")

    //    val sameModel = MatrixFactorizationModel.load(sc, "target/tmp/myCollaborativeFilter")

  }
}