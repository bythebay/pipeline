// Databricks notebook source exported at Thu, 2 Jul 2015 00:29:48 UTC
// MAGIC %md # **Spark After Dark Demo Notebook**

// COMMAND ----------

// MAGIC %md ## Prepare actor/actress reference data for enrichment of incoming likes

// COMMAND ----------

// actresses
sqlContext.read.format("json").load("/mnt/fluxcapacitor/sparkafterdark/data/actresses-flat.json").write.mode(SaveMode.Overwrite).saveAsTable("actresses")

val actressesDF = sqlContext.sql("SELECT (90000 + people.index) as id, people.name.text as name, people.bio.text as bio, people.hero.src as img FROM actresses LATERAL VIEW EXPLODE (results.people) p AS people")

// actors
sqlContext.read.format("json").load("/mnt/fluxcapacitor/sparkafterdark/data/actors-flat.json").write.mode(SaveMode.Overwrite).saveAsTable("actors")

val actorsDF = sqlContext.sql("SELECT (10000 + people.index) as id, people.name.text as name, people.bio.text as bio, people.hero.src as img FROM actors LATERAL VIEW EXPLODE (results.people) p AS people")

// union actresses and actors
val actressesAndActorsDF = actressesDF.unionAll(actorsDF).cache()
actressesAndActorsDF.saveAsTable("actresses_and_actors", SaveMode.Overwrite)
display(actressesAndActorsDF)

// COMMAND ----------

// MAGIC %md ## Create StreamingContext with a 5 second batch interval

// COMMAND ----------

//ssc.stop(stopSparkContext=false, stopGracefully=false)

// COMMAND ----------

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext

def createSsc(): StreamingContext = {
  @transient val newSsc = new StreamingContext(sc, Seconds(5)) 
  newSsc
}
val ssc = StreamingContext.getActiveOrCreate(createSsc)

// COMMAND ----------

// MAGIC %md ## Create Direct Kafka Stream with Given Brokers and Topics

// COMMAND ----------

import org.apache.spark.streaming.kafka.KafkaUtils
import kafka.serializer.StringDecoder

val brokers = "52.27.56.210:9092,52.27.56.210:9093"
val topicSet = Set("likes")
val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

val messageStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicSet)

// COMMAND ----------

// MAGIC %md ## Set up the Stream Transformations and Actions - and Start the Stream
// MAGIC * ### Append incoming to the likes table

// COMMAND ----------

messageStream.foreachRDD{ rdd =>
  // enrich the data upon ingest and save to table
  val df = rdd.toDF("from_user_id","to_user_id")
  df.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> "sparkafterdark", "table" -> "likes"))
          .save()
}
ssc.start()

// COMMAND ----------

// MAGIC %md ## Analyze the Results

// COMMAND ----------

val likesDF = sqlContext.read.format("org.apache.spark.sql.cassandra")
  .options(Map("keyspace" -> "sparkafterdark", "table" -> "likes"))
  .load().toDF("from_user_id", "to_user_id")
display(likesDF)

// COMMAND ----------

// MAGIC %md ## Total Number of Incoming Likes

// COMMAND ----------

likesDF.count()

// COMMAND ----------

// MAGIC %md ## Show Number of Distinct Users Participating

// COMMAND ----------

import org.apache.spark.sql.functions._

val distinctUsersDF =   
  likesDF.select(countDistinct($"from_user_id").as("distinct_users"))
display(distinctUsersDF)

// COMMAND ----------

// MAGIC %md ## Show Top Users by Like Count 

// COMMAND ----------

display(likesDF.groupBy("to_user_id").count().sort($"count".desc))

// COMMAND ----------

// MAGIC %md ## Compare the Physical Plans between DataFrames and SQL

// COMMAND ----------

likesDF.groupBy("to_user_id").count().sort($"count".desc).explain()

// COMMAND ----------

likesDF.registerTempTable("likes")

sqlContext.sql("SELECT to_user_id, count(to_user_id) as count FROM likes GROUP BY to_user_id ORDER BY count(to_user_id) DESC").explain()

// COMMAND ----------

// MAGIC %md ## Enrich Likes with Actress and Actor Reference Data

// COMMAND ----------

likesDF.cache()
val enrichedLikesDF = likesDF.join(actressesAndActorsDF, $"to_user_id" === $"id")
    .select($"from_user_id", $"to_user_id", $"name", $"bio", $"img")
    .cache()
display(enrichedLikesDF)

// COMMAND ----------

// MAGIC %md # Non-personalized Recommendations

// COMMAND ----------

// MAGIC %md ## 1. Top Users by Like Count

// COMMAND ----------

val topUsersByCountDF = enrichedLikesDF.select($"to_user_id", $"name", $"img")
	.groupBy($"to_user_id".as("id"), $"name", $"img")
    .count()
    .sort($"count".desc).where($"count" > 1)
    .limit(5)
	.cache()

display(topUsersByCountDF)

// COMMAND ----------

// MAGIC %md ## Generate top-users-by-count.json for the UI

// COMMAND ----------

val topUsersByCountJSONArray = "[" + topUsersByCountDF.toJSON.collect().mkString(",") + "]"

dbutils.fs.put("/mnt/fluxcapacitor/sparkafterdark/recommendations/top-users-by-count.json", topUsersByCountJSONArray, true)

// COMMAND ----------

// MAGIC  %md ## 2. Top Influencers by Like Graph

// COMMAND ----------

// MAGIC %md ![Most Desirable Users](https://raw.githubusercontent.com/cfregly/spark-after-dark/master/img/pagerank.png)

// COMMAND ----------

// MAGIC %md ## Convert Likes to (fromUserId, toUserId) Edge Tuples for Graph Analytics

// COMMAND ----------

import org.apache.spark.graphx._
import org.apache.spark.graphx.util._

val edgeTuples = enrichedLikesDF.map(like => 
  (like(0).toString.toLong, like(1).toString.toLong)
)

// COMMAND ----------

// MAGIC %md ## Create Graph from Edge Tuples and Run PageRank for Top Influencers

// COMMAND ----------

val graph = Graph.fromEdgeTuples(edgeTuples, "",   
  Some(PartitionStrategy.RandomVertexCut))

val pageRank = graph.pageRank(0.01).cache()

// COMMAND ----------

// MAGIC %md ## Enrich Top 10 Influencers based on PageRank

// COMMAND ----------

val topInfluencers = pageRank.vertices.top(10)(Ordering.by(rank => rank._2))

val topInfluencersDF = sc.parallelize(topInfluencers)
  .toDF("user_id", "rank")

val enrichedTopInfluencersDF = 
  topInfluencersDF.join(actressesAndActorsDF,   
    $"user_id" === $"id")
    .select($"id", $"name", $"bio", $"img", $"rank")
    .limit(5)
    .cache()

val topInfluencersJSONArray = "[" + enrichedTopInfluencersDF.toJSON.collect().mkString(",") + "]"

dbutils.fs.put("/mnt/fluxcapacitor/sparkafterdark/recommendations/top-influencers.json", topInfluencersJSONArray, true)

display(enrichedTopInfluencersDF)

// COMMAND ----------

// MAGIC %md # Personalized Recommendations

// COMMAND ----------

// MAGIC %md ## 3. Collaborative Filtering:  Alternating Least Squares Matrix Factorization

// COMMAND ----------

// MAGIC %md ![Alternating Least Squares - Matrix Factorization](https://raw.githubusercontent.com/cfregly/spark-after-dark/master/img/ALS.png)

// COMMAND ----------

// MAGIC %md ## Prepare and Split Likes Dataset for Model Training and Testing
// MAGIC * ###Training (80%)
// MAGIC * ###Testing (20%)

// COMMAND ----------

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating

val ratings = enrichedLikesDF.map(like => 
  Rating(like(0).asInstanceOf[Int], like(1).asInstanceOf[Int], 1)
)

val splitRatings = ratings.randomSplit(Array(0.80,0.20))	
val (trainingRatings, testingRatings) = (splitRatings(0), splitRatings(1))
trainingRatings.cache()
testingRatings.cache()

// COMMAND ----------

// MAGIC %md ## Configure Model Hyper-parameters and Train with Training Data

// COMMAND ----------

val rank = 10
val numIterations = 20
val convergenceThreshold = 0.01

val model = ALS.train(trainingRatings, rank, numIterations, convergenceThreshold)

// COMMAND ----------

// MAGIC %md ## Compare Predictions of Known Testing Ratings to the Actual Ratings
// MAGIC * ### Using Root Mean Squared Error (RMSE)

// COMMAND ----------

val testFromTo = testingRatings.map { 
  case Rating(fromUserId, toUserId, rating) => (fromUserId, toUserId)
}

val predictedTestRatings = 
  model.predict(testFromTo).map { 
    case Rating(fromUserId, toUserId, rating) => ((fromUserId, toUserId), rating)
  }

val actualTestRatings = testingRatings.map { 
  case Rating(fromUserId, toUserId, rating) => ((fromUserId, toUserId), rating)
}

val RMSE = Math.sqrt(actualTestRatings.join(predictedTestRatings).map { 
  case ((fromUserId, toUserId), (r1, r2)) => {
  	val err = (r1 - r2)
  	err * err
  }
}.mean())

// COMMAND ----------

// MAGIC %md ## Generate Personalized Recs for Each Distinct User

// COMMAND ----------

val recommendationsDF = model.recommendProductsForUsers(5)
  .toDF("user_id","ratings")
  .cache()

case class Like(from_user_id: Int, to_user_id: Int, confidence: Double)

val explodedRecommendationsDF = 
  recommendationsDF.explode($"ratings") { 
	case Row(likes: Seq[Row]) => likes.map(like => 
      Like(like(0).asInstanceOf[Int], 
      like(1).asInstanceOf[Int], 
      like(2).asInstanceOf[Double])) 
  }.select($"from_user_id", $"to_user_id", $"confidence")
   .join(actressesAndActorsDF, $"to_user_id" === $"id")
   .select($"from_user_id", $"to_user_id", $"name", $"bio", 
      $"img", $"confidence")
   .cache()

val recommendationsJSONArray = "[" + explodedRecommendationsDF.toJSON.collect().mkString(",") + "]"

dbutils.fs.put("/mnt/fluxcapacitor/sparkafterdark/recommendations/personalized-als.json", recommendationsJSONArray, true)

display(explodedRecommendationsDF)

// COMMAND ----------

// MAGIC %md ## Top Personalized Recs Among All Users

// COMMAND ----------

val topPersonalizedRecsDF = 
  explodedRecommendationsDF.select($"to_user_id", $"name", $"bio", $"img", $"confidence").groupBy($"to_user_id", $"name", $"bio", $"img").agg("confidence" -> "sum")
display(topPersonalizedRecsDF)

// COMMAND ----------


