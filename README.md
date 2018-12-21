# README

## Introduction

This simple starter module allow us to define retrofit service contract (Java interface annotated with Retrofit annotation), and let them scanned by spring container then automatically create the service and register as spring bean, based on a little configuration.

The main objective is to minimize verbose code of creating any retrofit service. Second is to simplify testing (e.g using mock server).

## Usage

Add this starter into your project, also add retrofit's call adapter and converter dependencies, this starter wont add them for you except the core retrofit dependency and rxjava2 call adapter factory.

```xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <dependencies>
        <dependency>
            <groupId>com.tiket.oss.spring.starters</groupId>
            <artifactId>retrofit-support-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
        
        <dependency>
            <groupId>com.squareup.retrofit2</groupId>
            <artifactId>retrofit</artifactId>
        </dependency>
        <dependency>
            <groupId>com.squareup.retrofit2</groupId>
            <artifactId>adapter-rxjava2</artifactId>
        </dependency>
        
        <!-- Optional, when required -->
        <dependency>
            <groupId>com.squareup.retrofit2</groupId>
            <artifactId>adapter-java8</artifactId>
        </dependency>
        <!-- Optional, when required -->
        <dependency>
            <groupId>com.squareup.retrofit2</groupId>
            <artifactId>converter-jackson</artifactId>
        </dependency>
    </dependencies>
</project>

```

Retrofit's ```retrofit2.CallAdapter.Factory``` and ```retrofit2.Converter.Factory``` will be auto-configured based on their presence in classpath.

Next step, annotate your spring boot's main class (or java configuration) with ```@com.tiket.tix.common.spring.retrofit.annotation.RetrofitServiceScan```. This annotation start scanning of ```@RetrofitService```, create the service and register into spring container.

```java

package com.tiket.tix.sample;

@SpringBootApplication
@RetrofitServiceScan
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

```

By default, ```@RetrofitServiceScan``` will scan all packages under which package its appear. In above example, all packages under ```com.tiket.tix.sample```. You can configure which package to be scanned by setting ```basePackages``` of ```basePackageClasses``` attributes.


Next step, assume you already have Retrofit service contract as following snippet, annotated with ```@com.tiket.tix.common.spring.retrofit.annotation.RetrofitService``` so can be detected by service scanner.

```java

@RetrofitService
public interface SampleClient {
    @POST("/ping")
    Single<Pong> ping(@Body Ping data);
}
    
```

Following snippets show you attributes of ```@com.tiket.tix.common.spring.retrofit.annotation.RetrofitService```

```java

package com.tiket.tix.common.spring.retrofit.annotation;
 
 import com.tiket.tix.common.spring.retrofit.registry.RetrofitRegistry;
 import org.springframework.core.annotation.AliasFor;
 import org.springframework.stereotype.Component;
 
 import java.lang.annotation.*;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
 @Inherited
 @Documented
 @Component
 public @interface RetrofitService {
     /**
      * Bean identifier of created retrofit client instance.
      *
      * @return
      */
     @AliasFor("name")
     String value() default "";
 
     /**
      * Bean identifier of created retrofit client instance.
      *
      * @return
      */
     @AliasFor("value")
     String name() default "";
 
     /**
      * {@link retrofit2.Retrofit} instance identifier, which registered in {@link RetrofitRegistry} bean,
      * responsible for creating this retrofit service.
      *
      * @return
      */
     String retrofit() default RetrofitRegistry.DEFAULT_RETROFIT;
 
     /**
      * Flag whether the client bean is singleton or not.
      *
      * @return
      */
     boolean singleton() default true;
 }

```

- ```value``` - bean name of the service object maintained in spring container, so you can look up or autowire from context container using this name.
- ```name``` - alias for ```value```.
- ```factory``` - which configured custom retrofit factory will be used in creating the service. By default, ```default``` endpoint will be used, configuration of default base url and additional endpoints will be explained next.
- ```singleton``` - flag whether service create is singleton bean or not.


On any spring managed bean, you can auto wire the service bean like normal spring bean.

```java

@Component
class SpringComponentSample {
    @Autowired
    private SampleClient sampleClient;
}

```

Configure default base url for your retrofit service in ```application.properties```

```properties

tiket.retrofit.default-url=http://example.com:10080/

```

>Please note, when ```default-url``` not provided, default retrofit object wont be created.

Other configurable properties for default ```Retrofit```

```properties

# Whether to debug http request and response payload (headers and body). Default is ```false```.
tiket.retrofit.connection.debug-request=true
# Connect timeout in milliseconds, default is ```30_000```
tiket.retrofit.connection.connect-timeout=30000
# Write timeout in milliseconds, default is ```30_000```
tiket.retrofit.connection.write-timeout=30000
# Read timeout in milliseconds, default is ```30_000```
tiket.retrofit.connection.read-timeout=30000
# Whether to use rxjava Scheduler (default is I/O scheduler) or just use caller thread. Default is ```true```
tiket.retrofit.connection.async-request=true
# Override default Scheduler (I/O).
tiket.retrofit.connection.scheduler.override-default=true
# Custom thread name prefix.
tiket.retrofit.connection.scheduler.thread-name-prefix=NamePrefix
# Scheduler core poll size.
tiket.retrofit.connection.scheduler.core-poll-size=50

```

In case you need multiple retrofit client which require different endpoint and/or connection configurations, you can configure additional endpoint in ```application.properties```, for example

```properties

tiket.retrofit.factories.custom-client.base-url=http://localhost:10090/custom-endpoint
tiket.retrofit.factories.custom-client.connection.debug-request=true
tiket.retrofit.factories.custom-client.connection.scheduler.thread-name-prefix=CustSchdulr
tiket.retrofit.factories.custom-client.connection.scheduler.core-poll-size=50

```

> Please note, ```custom-client``` is custom retrofit name, change based on your requirement.

Then set ```@RetrofitService#retrofit``` with name of configured retrofit, in above sample case is ```custom-client```. Following snippet show you how

```java

@RetrofitService(retrofit="custom-client")
public interface OtherRetrofitClient {
    @POST("/ping")
    Single<Pong> ping(@Body Ping data);
}

```

Other properties for configure additional ```Retrofit``` object

```properties

tiket.retrofit.factories.custom-client.connection.debug-request=true
tiket.retrofit.factories.custom-client.connection.connect-timeout=30000
tiket.retrofit.factories.custom-client.connection.write-timeout=30000
tiket.retrofit.factories.custom-client.connection.read-timeout=30000
tiket.retrofit.factories.custom-client.connection.async-request=true
tiket.retrofit.factories.custom-client.connection.scheduler.override-default=true
tiket.retrofit.factories.custom-client.connection.scheduler.thread-name-prefix=NamePrefix
tiket.retrofit.factories.custom-client.connection.scheduler.core-poll-size=50

```

> Please note, ```custom-client``` is custom retrofit name, change based on your requirement.

## Source

This project inspired by [this project](https://github.com/syhily/spring-boot-retrofit-support)