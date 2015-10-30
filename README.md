# Tips and Tricks

Tom Bunting 2015

### Java

#### Get object identity  when toString / hashcode overridden:
```java
String objToString = Integer.toHexString(System.identityHashCode(objectReference));
```

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

### Eclipse / STS
#### Improve startup time (dramatically)
Minimise the number of plugins

* installed
* set to load on startup - to change go to **Window - Preferences - General - Startup & Shutdown** and untick all but the 'essential' ones.

#### v4.5 (Mars) JDT features
Some nice stuff esp. related to lambdas: https://www.eclipse.org/eclipse/news/4.5/jdt.php e.g. Quick assist (CTRL+1) for options to convert anonymous blocks to lambdas, lambdas to method references, make inferred lambda type parameters explicit, etc.

#### Preferences
Regularly export preferences and code format (whenever a change has been made and adopted).

### Git / EGit

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
```
git reset --hard <commit-hash>
git push -f origin master
```

This will discard previous commit so be careful when others may be working on the same remote branch!

#### Undo (reset) last commit
Refer to: http://stackoverflow.com/questions/927358/how-do-you-undo-the-last-commit
```sh
git reset --soft HEAD~1
git push -f origin [name of branch]
```
That'll reset the active local branch.. then to push to the remote branch if you've really messed things up:

#### Merge from upstream/master favouring remote branch changes:
```sh
git merge -s recursive --strategy-option=theirs upstream/master
```
This will resolve all conflicts in favour of 'theirs'

### Apache / Tomcat
#### Listen to comms coming in to apache:

```sh
sudo tcpdump -A -s 0 'tcp dst port 80'`
```

#### Listening to communication between Apache and tomcat:
```sh
sudo tcpdump -A -s 0 -i lo 'tcp dst port 8083'`
```

(where tomcat is proxied on port 8083 via Apache). Crucial thing here is the -i lo to specify the LOCAL interface, not eth0, eth1 etc

### Spring
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

#### Spring Integration - Java DSL
##### Inbound / Outbound HTTP gateways
Use an inbound http-gateway to setup a *replying* HTTP messaging endpoint akin to an MVC controller request mapping, and or an inbound channel when the request is one-way (response not required beyond **200 OK** status returned):

```java
@Bean
public IntegrationFlow locationsGateway() {
    return IntegrationFlows
            .from(inboundGateway("/locations").requestMapping(r -> r.params("station_name"))
                    .payloadExpression("#requestParams.station_name"))
            .enrichHeaders(headerSpec -> headerSpec.header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON_VALUE, Boolean.TRUE))
            .gateway(obsLocationsOutboundFlow()).get();
}
```


### Maven / M2E

#### JUnit class not found
Run maven compile to resolve.

#### m2eclipse project import incorrectly configured project / classpath
Your workspace is likely corrupt - create a new one!

#### gmaven m2e project import problem: 
Quick fix - ignore - solved. Or: http://stackoverflow.com/questions/15938466

### Linux:
#### Remove windows line delimiters from files in vi:
```sh
:set fileformat=unix`
```

### Google Chrome
#### Slow Omni bar
Clear contents of: "C:\Users\[user_name]\AppData\Local\Google\Chrome\User Data\Default\Cache"

### Stack Overflow
#### reliably formatting multiline code blocks
Wrap the block in:
```html
<pre><code>{code}</code></pre>
```
