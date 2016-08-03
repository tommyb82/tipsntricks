# Tips and Tricks

### Java

#### Get object identity  when toString / hashcode overridden:
    String objToString = Integer.toHexString(System.identityHashCode(objectReference));


#### Weak references
In order from strongest to weakest:

* Strong reference, e.g. `StringBuilder sb = new StringBuilder();`
* Soft reference, e.g. `SoftReference<Widget> softRef = new SoftReference<>();` - good for cache, tend to stick about while there's plenty of memory available
* Weak reference, e.g. `WeakReference<Widget> weakRef = new WeakReference<>();` - referant is cleared by GC when no more strong references detected
* Phantom reference, e.g. `PhantomReference<Widget> phantomRef = new PhantomReference<>();` - the `get()` method always returns null. Can be used to determine when an object is GC'd.. in reality rarely used.

Soft and weak references are quite similar. Phantom references are quite different!

#### Connection pools / unwrapping.
DO NOT call close on the unwrapped connection - only on the delegate:
http://stackoverflow.com/questions/28002176/tomcat-connection-pool-few-methods-not-releasing-connection

- `JdbcTemplate` seems to be suppressing calls to close the connection when the connection has been unwrapped or the simple native JDBC extractor
has been used for the same purpose.  Wrapping the delegate connection in a try-with forces closure of the wrapping connection and this seems to work.

#### Java 8 

##### New methods on Collection interfaces
In addition to the new stream-related methods, there are a number of really useful new default methods on the `Map` interface, for example:

```java
computeIfAbsent(K key, Function<? super K,? extends V> mappingFunction);
computeIfPresent(K key, BiFunction<? super K,? super V,? extends V> remappingFunction);
putIfAbsent(K key, V value);
```
etc - see [Java 8 Map API](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html)

##### Type annotations
Some nice stuff: https://dzone.com/articles/java-8-type-annotations.  In conjunction check out the [Checker Framework](http://types.cs.washington.edu/checker-framework/current/checker-framework-manual.html)

##### LongAccumulator
When using `LongAccumulator` in a multi-threaded environment always ensure that the binary accumulation function provided is not order-dependent.  For example, `Math::addExact` would produce consistent results, where as `Math::subtractExact` would not (addition is order-independent, subtraction is not).

##### Parallel streams and Spliterators
Great article working through and explaining different divide-and-conquer approaches to prime number generation, from single-thread brute through recursive fork-join to streams and spliterators: [DZone article](https://dzone.com/articles/parallel-streams-and)

##### Stream extensions: jOOL library
For extensions to the Stream API providing for example some useful sequential stream functionality, check out the jOOL library: [jOOL blog](http://blog.jooq.org/2014/09/10/when-the-java-8-streams-api-is-not-enough/), [jOOL API v0.9.7](http://www.jooq.org/products/jOO%CE%BB/javadoc/0.9.7/index.html?overview-summary.html)

##### Other goodies
[InfoQ article](http://www.infoq.com/articles/Java-8-Quiet-Features) e.g. `StampedLock`, `LongAdder` (**always** favour over `AtomicLong`, `AtomicInteger` etc due to high-contention performance improvements), `Arrays.parallelSort(myArray)`, `StringJoiner`, `Long.hashCode(long value)`

### Eclipse / STS
#### Improve startup/shutdown time (dramatically!)
##### Minimise the number of plugins

* installed
* set to load on startup - to change go to **Window - Preferences - General - Startup & Shutdown** and untick all but the 'essential' ones.

##### Limit local history to 1 day
Window - Preferences - General - Workspace - Local History

#### v4.5 (Mars) JDT features
Some nice stuff esp. related to lambdas: https://www.eclipse.org/eclipse/news/4.5/jdt.php e.g. Quick assist (CTRL+1) for options to convert anonymous blocks to lambdas, lambdas to method references, make inferred lambda type parameters explicit, etc.

#### Preferences
Regularly export preferences and code format (whenever a change has been made and adopted).

#### Favourite static imports
Add regularly used static imports to **Window - Preferences - Java - Editor - Content Assist - Favourites**.  Good candidates are for example the Spring helper classes used in testing, e.g. `MockRestResponseCreators.*`, `MockMvcResultMatchers.*`, JUnit's `Assert.*` class, etc

### Git / EGit

#### Local and remote branch rename:
See https://multiplestates.wordpress.com/2015/02/05/rename-a-local-and-remote-branch-in-git/
* Rename your local branch. If you are on the branch you want to rename: 

    git branch -m new-name
    
* Delete the old-name remote branch and push the new-name local branch:

    git push origin :old-name new-name
    
* Reset the upstream branch for the new-name local branch. Switch to the branch and then:

    git push origin -u new-name

#### EGit: fetch new remotes 
Right click the origin server under Remotes -> origin and select 'Fetch'. Then checkout into a (new) local branch and switch to the new branch

#### Dependencies for Jackson 2.5.*:

```xml
<dependency>
	<groupId>com.fasterxml.jackson.core</groupId>
	<artifactId>jackson-core</artifactId>
	<version>2.5.1</version>
</dependency>

<dependency>
	<groupId>com.fasterxml.jackson.core</groupId>
	<artifactId>jackson-databind</artifactId>
	<version>2.5.1</version>
</dependency>

<dependency>
	<groupId>com.fasterxml.jackson.core</groupId>
	<artifactId>jackson-annotations</artifactId>
	<version>2.5.0</version>
</dependency>
```

#### Remote branch reset
Refer to: http://stackoverflow.com/questions/5816688/reseting-remote-to-a-certain-commit

    git reset --hard <commit-hash>
    git push -f origin master

This will discard previous commit so be careful when others may be working on the same remote branch!

#### Undo (reset) last commit
Refer to: http://stackoverflow.com/questions/927358/how-do-you-undo-the-last-commit

    git reset --soft HEAD~1
    git push -f origin [name of branch]

That'll reset the active local branch.. then to push to the remote branch if you've really messed things up:

#### Merge from upstream/master favouring remote branch changes:

    git merge -s recursive --strategy-option=theirs upstream/master

This will resolve all conflicts in favour of 'theirs'

#### Squashing a load of commits via an interactive rebase
After making a series of small or subsequently reverted commits, for example when iteratively testing some change affecting or affected by the
CI builds, it is a good idea to squash all these meaningless commits into one single commit with the result being a sensible set of changes that
reflect the feature being built. This can be done via an interactive rebase on the working branch:

##### Using interactive rebase:
    git rebase -i <last_commit_to_preserve_as_is> 

Then use the interactive editor to replace `pick` with `squash` or `fixup` for the second and subsequent commits you want
to squash into the first.  Then optionally force-push the rewritten history to the remote tracking branch:

    git push -f origin <branch_name>

### Apache / Tomcat
#### Listen to comms coming in to apache:

    sudo tcpdump -A -s 0 'tcp dst port 80'

#### Listening to communication between Apache and tomcat:

    sudo tcpdump -A -s 0 -i lo 'tcp dst port 8083'

(where tomcat is proxied on port 8083 via Apache). Crucial thing here is the -i lo to specify the LOCAL interface, not eth0, eth1 etc

### Spring
#### Kicking off a background task at startup

Class 1 (do something in the background..):

    ```java
    @Component
    public class BackgroundTask {
    
        private int counter;
    
        @Async
        void slowCount() throws InterruptedException {
            while (true) {
                out.println("Counter is now: " + ++counter);
                Thread.sleep(5000);
            }
        }
    
    }
    ```java

Class 2: task scheduler

    ```java
    @Configuration
    @EnableAsync
    public class BackgroundTaskInitialiser {
    
        @Autowired
        private BackgroundTask backgroundTask;
    
        @PostConstruct
        public void initialise() throws InterruptedException {
            backgroundTask.slowCount();
        }
    
    }
    ```java

Reference: http://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html

#### Configuring SpringLoaded
In Run Configuration - vmargs:

`-javaagent:"C:\applications\springloaded-1.2.1.RELEASE.jar" -noverify`

#### Spring Boot - auto config / test harness
When adding your own auto-config in the test harness this sort of thing can be done:

```java
@SpringBootApplication
@ComponentScan("uk.gov.meto.commons.hal.api.testsupport.controller")
public class TestAppStarter {
/* required beans and stuff */
}
```
That way, any other `@Configuration` classes will be located, but component scanning (e.g. for `@ControllerAdvice` annotated classes) will be limited to the specified base classes.  This is handy for example when `@ControllerAdvice` classes don't have a default constructor but instead rely on beans to be created in the factory prior.

Or to run with maven spring-boot plugin, see:
http://docs.spring.io/spring-boot/docs/current/reference/html/howto-hotswapping.html

#### Enterprise Integration Patterns
##### Spring Integration / Apache Camel comparison
Great comment on semantic diffs between two EIP frameworks: http://callistaenterprise.se/blogg/teknik/2015/10/12/apache-camel-vs-spring-integration/

#### Spring Integration - Java DSL
##### Inbound / Outbound HTTP gateways
Use an inbound http-gateway to setup a *replying* HTTP messaging endpoint akin to an MVC controller request mapping, and or an inbound channel when the request is one-way (response not required beyond **200 OK** status returned):

```java
@Bean
public IntegrationFlow locationsGateway() {
    return IntegrationFlows
            .from(inboundGateway("/accounts").requestMapping(r -> r.params("account_num"))
                    .payloadExpression("#requestParams.account_num"))
            .enrichHeaders(headerSpec -> headerSpec.header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON_VALUE, Boolean.TRUE))
            .gateway(obsLocationsOutboundFlow()).get();
}
```
##### Architecture
Message channels represent the 'pipes' in a pipes-and-filters architecture.  Producers **send** messages to a channel and consumers **recive** messages from a channel. A message channel may follow either P2P (aka queue, at most one subscriber receives each msg) or Pub-Sub (aka topic; msgs broadcast to all subscribers) semantics.

In SI, **Pollable* channels are capable of buffering messages in a queue, supporting throttling and overloading of a msg consumer.

### Maven / M2E

#### JUnit class not found
Run maven compile to resolve.

#### m2eclipse project import incorrectly configured project / classpath
Your workspace is likely corrupt - create a new one!

#### gmaven m2e project import problem: 
Quick fix - ignore - solved. Or: http://stackoverflow.com/questions/15938466

#### Archetype createion / usage
##### Creating an archetype from an existing project
    mvn archetype:create-from-project -DoutputDirectory ../pwms-springboot-service

##### Generating a new project from an archetype

### Linux:
#### Remove windows line delimiters from files in vi:
    :set fileformat=unix

### Google Chrome
#### Slow Omni bar
Clear contents of: "C:\Users\[user_name]\AppData\Local\Google\Chrome\User Data\Default\Cache"

### Stack Overflow
#### reliably formatting multiline code blocks
Wrap the block in:
    <pre><code>{code}</code></pre>


### Apache Camel
#### Reading a CSV and split-streaming to a Kafka topic
Reading a CSV once an hour from an HTTP endpoint (via a `POST` request), unmarshalling it (lazily), splitting the contents by line then streaming the marhsalled JSON objects (each representing a line in the CSV) to the local logging system and a Kafka topic.

```java
void configure() {
        final CsvDataFormat csvFormat = new CsvDataFormat();
        csvFormat.setUseMaps(TRUE); // unmarshal CSV to maps rather than Lists
        csvFormat.setLazyLoad(TRUE); // read line-by-line (avoid OOM)

        final String localKafkaTopicURI =
                new StringJoiner("&").add("kafka:localhost:9092?topic=obs").add("zookeeperConnect=localhost:2181")
                        .add("serializerClass=kafka.serializer.StringEncoder").add("requestRequiredAcks=-1").toString();

        from("timer://obstimer?fixedRate=true&period=1h").setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/x-www-form-urlencoded"))
                .setBody(
                        simple("Type=Observation&PredictionSiteID=ALL&ObservationSiteID=ALL&Date=26%2F11%2F2015&PredictionTime=0500"))
                .to("https://weather.data.gov.uk/query").routeId("obs").unmarshal(csvFormat).split(body()).streaming()
                .marshal().json(JsonLibrary.Jackson).convertBodyTo(String.class)
                .to("log:example.com.routers.landobs?level=TRACE&showExchangePattern=false").to(localKafkaTopicURI)
                .end();
    }
```

### Docker
#### Open a shell within an existing container
    sudo docker exec -it <containerID> bash
    
or, e.g. for Alpine linux:

    sudo docker exec -it <containerID> /bin/sh

#### Remove all exited containers
    docker rm $(docker ps -a -q -f status=exited)

#### Statistics for all running containers
    docker stats \`docker ps | awk '{print $NF}' | grep -v NAMES\`
    
#### Spin up new container
In the background, with max container memory, host/guest OS port and volume mapping:

    docker run --name my-service -d -m 256m -p 8080:8080 -v /data/my-service:/app/data tommyb/my-service

#### Open shell into running container
    docker exec -it "id of running container" bash