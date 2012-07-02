package com.thenewmotion.akka.http.sample

import com.thenewmotion.akka.http._
import com.thenewmotion.akka.http.Endpoints._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.thenewmotion.akka.http.Async.Complete
import akka.actor.{ActorSystem, ActorRef, Actor, Props}
import akka.dispatch.{Promise, Future}
import scala.Some
import com.thenewmotion.akka.http.Async.Complete

/**
 * @author Yaroslav Klymko
 */
class HelloWorldServlet extends AkkaHttpServlet with StaticEndpoints {

  var helloWorldActor: Option[ActorRef] = None

  val helloWorldFunction = RequestResponse {
    req =>
    // doing some heavy work here then

    // creating function responsible for completing request, this function might not be called if request expired
      FutureResponse {
        res =>
          res.getWriter.write(
            <html>
              <body>
                <h1>Hello World</h1>
                <h3>endpoint function</h3>
              </body>
            </html>.toString())
          res.getWriter.close()
      }
  }


  override def onSystemInit(system: ActorSystem, endpoints: EndpointsAgent) {
    super.onSystemInit(system, endpoints)

    helloWorldActor = Some(system.actorOf(Props[HelloWorldActor]))
  }

  def providers = {
    //endpoint as a function will be used for "/" and "/function" urls
    case "/" | "/function" => helloWorldFunction
    //endpoint as an actor will be used for "/actor" url
    case "/actor" => helloWorldActor.get
  }
}


class HelloWorldActor extends Actor {

  def receive = {
    case req: HttpServletRequest =>

      // doing some heavy work here

      //will be called for completing request
      val future = FutureResponse {
        res =>
          res.getWriter.write(
            <html>
              <body>
                <h1>Hello World</h1>
                <h3>endpoint actor</h3>
              </body>
            </html>.toString())
          res.getWriter.close()
      }

      //passing func to AsyncActor, created for this AsyncContext
      sender ! Complete(future)
  }
}
