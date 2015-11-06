package com.viqueen.playground.twitter

import akka.actor.{ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import com.typesafe.config.ConfigFactory
import twitter4j._
import twitter4j.auth.AccessToken


object TwitterSentiment extends App {

  val config = ConfigFactory.load().getConfig("twitter")
  val system = ActorSystem("twitter-sentiment")
  val tweetActor = system.actorOf(Props[TweetSentimentActor])
  val instance = new TwitterStreamFactory().getInstance()

  // yuk ... any way to just write config["consumer_key"]
  instance.setOAuthConsumer(config.getString("consumer_key"), config.getString("consumer_secret"))
  instance.setOAuthAccessToken(new AccessToken(config.getString("access_token_key"), config.getString("access_token_secret")))
  instance.addListener(new StatusSentimentListener(system))

  instance.filter(new FilterQuery(("javascript")))
}

class StatusSentimentListener(val actors : ActorSystem) extends StatusListener {

  override def onStallWarning(stallWarning: StallWarning): Unit
    = {}

  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit
    = {}

  override def onScrubGeo(latitude: Long, longitude: Long): Unit // could be the opposite, didn't look it up
    = {}

  override def onStatus(status: Status): Unit
    = actors.eventStream.publish(new TweetSentiment(status))

  override def onTrackLimitationNotice(i: Int): Unit
    = {}

  override def onException(exception: Exception): Unit
    = {}
}


case class TweetSentiment(status: Status) {
  def value () : Float = 0

}

class TweetSentimentActor extends ActorPublisher[TweetSentiment] {
  context.system.eventStream.subscribe(self, classOf[TweetSentiment])

  override def receive: Receive = {
    case tweet : TweetSentiment => println(s"yey $tweet")
  }
}