import dutyAssigner.DutyWorker
import flowdock.FlowdockAPI
import flowdock.model.Activity
import flowdock.model.Author
import flowdock.model.Thread
import flowdock.model.UpdateAction
import google.Authorization
import google.Calendar
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.time.Duration
import java.time.Instant
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val errorHandler = { e: Throwable ->
        println("Got error ${e.message}. Shutting down")
        exitProcess(-1)
    }

    val credential = Authorization.authorize()
    val calendar = Calendar(credential)
    val dutyWorker = DutyWorker(calendar = calendar, flowdockAPI =  FlowdockAPI(System.getenv("FLOW_TOKEN")))

    val job = PeriodicJob(
        runEvery = Duration.ofMinutes(1),
        job = dutyWorker::perform,
        errorHandler = errorHandler
    )

    job.start()


    val server = embeddedServer(
        Netty,
        port = 8080,
        watchPaths = listOf("src/main/kotlin"),
        module = Application::dutyAssigner
    )

    server.start(wait = true)
}