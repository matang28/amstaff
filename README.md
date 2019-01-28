# Amstaff (WIP)

## Motivation
As the adoption of CI/CD increases dramatically, software development relies on monitoring & alerting solutions more than ever before. 
As an experienced developer and entrepreneur, monitoring has always been the "Achilles heel" of the company, mainly because current tools has bad developer experience (DX)
which makes people hate writing alerts.  
Amstaff is an open-source project which provides the following features:
1) Easy to use & Elegant API.
2) Easy installation.
3) Fault-tolerant and highly available.
4) Zero dependencies.
5) Dynamically push monitoring modules into the server.

## Architecture


## Core Concepts
Amstaff uses well defined and elegant concepts in order for anyone to easily extend it for its own purposes.

#### The Sampler
The sampler refers to the actual engine that we want to query. It could be an Http Sampler, Sql Sampler or any kind of metrics you would like to sample.
```kotlin
interface Sampler {
    // The unique name of the sample
    val name: String
    
    // A set of tags associated with this sampler
    val tags: Set<String>
    
    // A suspendable function that will do the actual probing
    suspend fun probe(): SampleResult
}
```

You can see that the definition is concise. Amstaff will build all the Samplers at startup allowing you to use them when building alerts.

Amstaff is shipped with the GraphiteSampler, HttpSampler and SqlSampler.

#### A Scheduled Sampling
Having a sampler make no sense if we can't schedule it :)
A scheduled sample defines the timing on which the sampler should run and which handlers do we need to notify.

> For example: "I want to run an *http* health-check *every 1 minute* and send the results to a *slack* channel"
 
```kotlin
data class ScheduledSampling(val sampler: Sampler,
                             val timing: Schedule,
                             val handlers: Sequence<SampleHandler>)
```

Amstaff uses [skedule](https://github.com/shyiko/skedule) to define timing.

#### The Sample Handler
The Sample Handler defines how you want to react when sample occurs:
```kotlin
interface SampleHandler {
    suspend operator fun invoke(sampler: Sampler, result: SampleResult): SampleStatus
}
```

You can put any kind of logic that works for you but Amstaff is shipped with the following handlers: EmailHandler, HttpHandler, OpsGenieHandler, SlackHandler which simply notify on any sample result change.


## Example of usage
Amstaff is built with Kotlin, and provides a simple to use DSL to create type-safe monitoring modules.
```kotlin
// Define the module
amstaff("User Service") {

    // Define new scheduled sampling
    monitor("Health-Check Failed") {
        // Using the HttpSampler
        sampler = httpGet {
            url = "http://localhost:20"
            okStatus = setOf(200, 201)
        }
        
        // This sampler should run every 30 minutes
        timing = every(30.minutes)     

        // Use slack to pass messages
        handlers = sequenceOf(
            slack("API-KEY") {
                message = "User service is down!!!"
                channel = "user-service-prod"
            }
        )
        
        tags = setOf("production", "user-service", "team8")
    }

    monitor("High CPU") {
        sampler = graphite(conf) {
            query = ""
            warning = 70
            critical = 85
        }

        tags = setOf("production", "user-service", "devops")

        timing = every(5.minutes)

        handlers = sequenceOf(
            slack("API-KEY") {
                message = "High CPU usage in user-service"
                channel = "user-service-prod"
            }
        )
    }
}
```

Just save this file within your repository and push it to the server when deploying the module, a simple `curl` will do the job:
```bash
curl -x POST -d @user-service.kts amstaff.mydomain.com/monitor
```
