package com.thenewmotion.akka.http.sample

import com.thenewmotion.akka.http.{EndpointsAgent, StaticAkkaHttpServlet}
import com.thenewmotion.akka.http.Endpoints._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.thenewmotion.akka.http.Async.Complete
import akka.actor.{ActorSystem, ActorRef, Actor, Props}

/**
 * @author Yaroslav Klymko
 */
class HelloWorldServlet extends StaticAkkaHttpServlet {

  var helloWorldActor: Option[ActorRef] = None

  val helloWorldFunction: Processing = (req: HttpServletRequest) => {

    // doing some heavy work here then

    // creating function responsible for completing request, this function might not be called if request expired
    (res: HttpServletResponse) => {
      res.getWriter.write(
        <html>
          <body>
            <h1>Hello World</h1>
            <h3>endpoint function</h3>
          </body>
        </html>.toString())
      res.getWriter.close()

      // our callback whether response succeed
      (b: Boolean) => println("SUCCEED: " + b)
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
      val func = (res: HttpServletResponse) => {
        res.getWriter.write(
          <html>
            <body>
              <h1>Hello World</h1>
              <h3>endpoint actor</h3>
            </body>
          </html>.toString())
        res.getWriter.close()


        // our callback whether response succeed
        (b: Boolean) => println("SUCCEED: " + b)
      }

      //passing func to AsyncActor, created for this AsyncContext
      sender ! Complete(func)
  }
}
