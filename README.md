**gRPC and Protobuf - Weather Service Example**
===============================================

**Introduction to gRPC and Protocol Buffers (Protobuf)**
--------------------------------------------------------

**gRPC** (Google Remote Procedure Call) is a high-performance, open-source and language-agnostic framework designed for
building and consuming remote services. gRPC uses **Protocol Buffers (Protobuf)** as its interface definition language (
IDL) and message serialization format.

### **Why gRPC?**

* **Efficient**: gRPC is built on HTTP/2, which offers significant performance improvements over HTTP/1.1, including
  multiplexing, header compression, and more efficient streaming.

* **Language Agnostic**: Supports a wide variety of programming languages such as Go, Python, Java, Node.js, and C++.

* **Streaming**: Supports four types of communication patterns (unary, client streaming, server streaming, and
  bidirectional streaming).

* **Simple to Use**: Provides simple service definitions and automatic code generation for client and server code.

### **Why Protobuf for Serialization?**

* **Compact**: Protobuf messages are smaller and faster to serialize and deserialize compared to JSON or XML.

* **Efficient**: Protobuf allows for efficient binary encoding, resulting in faster processing of data.

* **Schema-based**: It uses a defined schema (specified in .proto files), making it easy to evolve messages over time
  while maintaining backward compatibility.

**Understanding the .proto File**
---------------------------------

The .proto file defines the structure of the data, services, and methods that will be used in gRPC. Below is a
step-by-step explanation of the key components:

### **1\. Syntax Declaration**

```proto   
    protoCopyEditsyntax = "proto3";   
```

* **syntax = "proto3";**: Specifies that the file uses **Proto3** syntax (the latest version of Protocol Buffers).
  Proto3 is simpler than Proto2, with fewer options, and supports modern features like JSON support.

### **2\. Option Statements**

```proto
    protoCopyEditoption java_multiple_files = true;  
    option java_package = "na.library.grpcweather.proto";  
    option java_outer_classname = "WeatherProto";   
```

* **option java\_multiple\_files = true;**: Instructs the Protocol Buffers compiler to generate multiple Java files
  instead of a single file, which helps in organizing the generated code.

* **option java\_package = "na.library.grpcweather.proto";**: Defines the Java package name for the generated classes.

* **option java\_outer\_classname = "WeatherProto";**: Specifies the outer class name that will wrap all the generated
  classes.

### **3\. message and service Definitions**

#### **Message Definitions**

A **message** defines the data structures exchanged in gRPC calls.

For example:

```proto 
     LocationRequest {    
        string city = 1;    
        string country = 2;  
        }   
```

* **LocationRequest**: A message that contains two fields: city and country, both of type string.

* **Field numbers** (1, 2): These are unique identifiers used to encode and decode data in the binary format. These
  field numbers are crucial for backward compatibility when evolving your schema.

#### **Service Definitions**

A **service** defines a set of methods (RPCs) that clients can call remotely.

For example:

```proto     
 WeatherService {    
    rpc GetCurrentWeather(LocationRequest) 
    returns (WeatherResponse) {}  
    }  
```

* **WeatherService**: Defines the GetCurrentWeather method, which takes a LocationRequest message and returns a
  WeatherResponse message.

### **4\. Field Numbers**

* **Field numbers** are crucial in Protobuf as they are used to identify fields in the binary format. When the message
  is serialized, the field numbers are used instead of field names, making the message more compact.

* Field numbers must be unique within a message and should be stable across schema changes.

### **5\. gRPC Communication Patterns**

gRPC supports four different types of RPC (Remote Procedure Call) patterns, which define how data is exchanged between
clients and servers:

* **Unary RPC**: A single request and a single response.

    * **Use case**: Requesting current weather information for a given location.

* **Server Streaming RPC**: A single request and a stream of responses.

    * **Use case**: Receiving weather forecast updates for a location.

* **Client Streaming RPC**: A stream of requests and a single response.

    * **Use case**: Submitting multiple weather data readings.

* **Bidirectional Streaming RPC**: A stream of requests and a stream of responses.

    * **Use case**: Real-time weather monitoring and alerts.

**Detailed Breakdown of the grpc.proto File**
---------------------------------------------

### **Service Definition**

The service **WeatherService** exposes the following RPC methods:

1. protoCopyEditrpc GetCurrentWeather(LocationRequest) returns (WeatherResponse);

    * **Input**: LocationRequest (contains city and country).

    * **Output**: WeatherResponse (contains current weather data).

2. protoCopyEditrpc GetWeatherForecast(LocationRequest) returns (stream WeatherResponse);

    * **Input**: LocationRequest (contains city and country).

    * **Output**: A stream of WeatherResponse messages (weather forecast updates).

3. protoCopyEditrpc SubmitWeatherData(stream WeatherData) returns (SubmitResponse);

    * **Input**: A stream of WeatherData (multiple weather readings from the client).

    * **Output**: SubmitResponse (indicating whether the data submission was successful).

4. protoCopyEditrpc MonitorWeather(stream LocationRequest) returns (stream WeatherAlert);

    * **Input**: A stream of LocationRequest (cities to monitor).

    * **Output**: A stream of WeatherAlert (real-time weather alerts for each monitored location).

### **Message Definitions**

1. **LocationRequest**:

    * **Fields**: city and country (both string).

    * Used for specifying the location (city and country) for weather-related queries.

2. **WeatherResponse**:

    * **Fields**: location (string), temperature (float), description (string), humidity (float), wind\_speed (float),
      timestamp (string).

    * Contains the current weather data for a location.

3. **WeatherData**:

    * **Fields**: location (string), temperature (float), humidity (float), pressure (float), wind\_speed (float),
      timestamp (string).

    * Represents a weather data reading submitted by the client.

4. **SubmitResponse**:

    * **Fields**: success (bool), records\_processed (int32), message (string).

    * Indicates the result of the weather data submission.

5. **WeatherAlert**:

    * **Fields**: location (string), alert\_type (string), description (string), severity (string), timestamp (string).

    * Contains weather alerts (e.g., storms, floods, heatwaves) for a location.

### **Why gRPC and Protobuf?**

* **Faster Serialization/Deserialization**: Protobuf uses a binary format, making it much faster and more compact than
  JSON or XML for data serialization.

* **Schema-based**: Protobuf provides a predefined schema (.proto file), making it easy to evolve and maintain messages
  over time without breaking backward compatibility.

* **Streaming**: gRPC supports bidirectional streaming, which is very useful for real-time applications like weather
  monitoring, where both the client and server need to exchange continuous data.

### **Compile the .proto File**

Once you've set up the dependencies, the next step is to **compile the .proto file**. Here's what happens during this
step:

1. **Protocol Buffers Compiler (protoc)**: The protobuf-maven-plugin triggers the Protocol Buffers compiler (protoc)
   when you run a build (mvn clean install or mvn compile).

    * It reads the .proto file.

    * It generates Java source files that represent the messages and services defined in the .proto file.

2. **Generated Files**: The generated files are placed in the target/generated-sources/protobuf directory. These files
   include:

    * **Message Classes**: These represent the structures of the data, such as LocationRequest and WeatherResponse.
      These classes have getter and setter methods for the fields defined in the .proto file.

    * **Service Stubs**: These represent the server and client stubs for the service methods defined in the .proto file,
      such as WeatherServiceGrpc.
      Important Information

---------------------

> **Note about grpc-server-spring-boot-starter:**

Originally, the project manually starts a gRPC server inside the GrpcServer class:

```grpc server  
   server = ServerBuilder.forPort(port)      
   .addService(weatherService)      
   .build()      
   .start();   
```  

Because of this, **you should NOT add** the following dependencies to your pom.xml:

```xml      
     <!-- gRPC Starter -->
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-server-spring-boot-starter</artifactId>
            <version>${grpc-spring-boot-starter.version}</version>
        </dependency>  
```

If you add these, Spring Boot will automatically start another gRPC server on the same port (8090), causing the
following error:


```text    
    perlCopyEditCaused by: java.net.BindException: Address already in use: bind   
```

This happens because **two servers** try to bind to **the same port**.

✅ Therefore, in this project setup:

* We **manually control** the gRPC server startup and shutdown.

* We **do not use** grpc-server-spring-boot-starter.

🛠 Running the Application
--------------------------

1. Make sure port 8090 is free.

2. bashCopyEdit./mvnw spring-boot:run

3. The gRPC server will start and listen on localhost:8090.

Logs will show:
```text     
    nginxCopyEditgRPC Server started, listening on port 8090   
```

🧩 Future Options
-----------------

If you want to migrate to **Spring Boot Starter** management in the future:

* Delete the manual GrpcServer class.

* Add grpc-server-spring-boot-starter to your dependencies.

* propertiesCopyEditgrpc.server.port=8090grpc.server.address=\*

Spring will automatically manage the gRPC server lifecycle for you.

**Keep your manual GrpcServer class (don't use grpc-server-spring-boot-starter)**

```xml      
     <!-- gRPC Starter -->
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-server-spring-boot-starter</artifactId>
            <version>${grpc-spring-boot-starter.version}</version>
        </dependency>  
```

* Keep using your GrpcServer class.

* You fully control how the server starts and stops.

✅ **Simple, works, no conflict.**

### 2\. **Switch to grpc-server-spring-boot-starter (delete your GrpcServer class)**

If you want **Spring Boot** to manage the gRPC server automatically:

* **Delete** your GrpcServer class completely.

* Keep your gRPC service classes (like WeatherServiceImpl) — Spring will auto-register them.

* grpc.server.port=9090

* Starter will automatically detect @GrpcService annotated classes and start the server.

✅ **Spring-style, easier to configure advanced features** (like interceptors, reflection, etc.)

## ⚖️ Manual Server vs Spring Boot Starter

When setting up a gRPC server in a Spring Boot application, you can choose between managing the server manually or using
a Spring Boot starter dependency. Here's a comparison:

| Feature                              | Manual (`GrpcServer` Class)                                 | Spring Boot Starter (`grpc-server-spring-boot-starter`)             |
|:-------------------------------------|:------------------------------------------------------------|:--------------------------------------------------------------------|
| **Control**                          | Full control over server behavior                           | Auto-configuration and lifecycle management                         |
| **Service Registration**             | Need to manually add services and manage shutdown           | Spring automatically registers services and manages lifecycle       |
| **Customization**                    | Good if you want custom startup logic (e.g., hooks, checks) | Good if you want fast setup and easier maintenance                  |
| **Adding Interceptors / Middleware** | Harder, must be done manually                               | Easy to configure security, interceptors, reflection, metrics, etc. |
| **Suitability**                      | Best for custom or lightweight setups                       | Best for larger, production-grade applications needing flexibility  |

## 🎯 gRPC Global Interceptors and Exception Handling

### @GrpcGlobalServerInterceptor

- `@GrpcGlobalServerInterceptor` is an annotation provided by the gRPC Spring Boot Starter.
- It allows you to automatically register a **gRPC server interceptor** without manually attaching it to each service.
- Interceptors are useful for **logging**, **authentication**, **metrics collection**, **tracing**, and **modifying
  requests or responses**.

### @GrpcAdvice

* @GrpcAdvice is used to implement **global exception handling** for gRPC services, similar to @ControllerAdvice in
  Spring MVC.
* It allows you to catch exceptions thrown by your gRPC services and map them to **proper gRPC error responses**.
*  With @GrpcAdvice, you can keep your service logic clean and handle all exceptions in a centralized way.

Versioning and Backward Compatibility
-------------------------------------

gRPC uses Protocol Buffers (Protobuf) for schema definition, and following best practices in .proto evolution ensures safe upgrades and long-term compatibility between clients and servers.

### ✅ Best Practices for Protobuf Evolution

1.  **Never change tag numbers**

    *   Once a field tag is assigned, **do not reuse or renumber it**, even if the field is removed.

    *   This ensures compatibility with older clients/servers still using those field positions.

2.  **Use reserved keywords**

    *  reserved 3, 4;reserved "oldFieldName";

3.  **Add, don't modify**

    *   Adding **new optional fields** is safe and backward-compatible.

    *   Old clients will ignore unknown fields; new servers must handle missing values safely.

4.  **Avoid required fields**

    *   Protobuf 3 removed required because it breaks compatibility.

    *   Always use optional (implicitly by default) or repeated.

5.  **Deprecate, don’t delete immediately**

    * string old_field = 5 [deprecated = true];
