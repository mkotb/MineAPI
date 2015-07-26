# MineAPI

MineAPI is a lightweight HTTP (not literally REST, even though it would market better) API which wraps the Mojang Web API which you can host yourself.
The primary use is to have a global UUID <-> Name cache between many sub servers and acheive fast response times. Simple as that.

## Tools and dependencies

MineAPI is written in Kotlin, you can read more about the language [here](http://kotlinlang.org) and is written on-top of the JVM.
If you plan on adding any features to MineAPI, writing it in Java will work out of the box. 
Keep in mind however any pull-requests made must be written in Kotlin. 
[Wasabi](https://github.com/hhariri/wasabi) is used as an HTTP framework, [Google Guava](https://github.com/google/guava) is used for it's wonderful caching, 
and [Unirest](http://unirest.io/) is used for external requests. MineAPI is also compiled for the Java 8 runtime.

## Configuring MineAPI

After the first start of the application, a file called `config.json` will be created, here are the explanation of what each value means:

```kotlin
{
  "port": 3000, // port which you want the server to listen on
  "address": "0.0.0.0", // address you wish the server to listen on
  "debug": false, // enabling debug, information about mojang responses and invalid requests are printed
  "verboseLogging": false, // verbose logging, everything is printed. note enabling this will not enable debug
  "rateLimit": 60, // the amount of requests one host can make every minute

  "idConfig": { // configuration for identifiers (UUIDs, names)
    "cacheTime": 30, // how long an entry will stay in the cache in minutes, time is renewed after every access
    "maxSize": 10000 // maximum size of the cache
  }
}
```

## Integrating into your own applications

Integrating MineAPI into your applications is unbelievably simple, in the following example I will be using Unirest and Kotlin:

```kotlin
// name -> uuid
var response = Unirest.get("https://youraddresshere/v1/player/GitHub/uuid").asJson()

if (response.getStatus() == 400) {
  // invalid name
  return
}

if (response.getStatus() != 200) {
  // internal server error
  return
}

var id = UUID.fromString(response.getBody().getObject().getString("uuid"))

// uuid -> names
response = Unirest.get("https://youraddresshere/v1/player/${id.toString().replace("-", "")}/names")
        .asJson()

// same responses as before dealt accordingly

var names = response.getBody().getObject()
var currentName = response.getString("name")
var oldNames = response.getJSONArray("oldNames")
```

## Adding to MineAPI

Pull requests are always welcome! When writing routes make sure you register them in the RouteRegistrar, keep it clean,
and in Kotlin. Also include documentation about any added functionality or configuration, 
either as an addition to the README or as JavaDoc comments; this helps me keep everything up to date and straight-forward.
Lastly, make sure that you've updated the license headers of newly created files by running `mvn clean` in the project.
