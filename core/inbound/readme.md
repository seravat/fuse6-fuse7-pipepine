Contains the core business logic of the application and top-level concerns of the application only.

Should be free of resource and component code tied with different technology stacks. 

Use code Annotations and maven+spring dependency injection to tie in or bind processors, services, components, resources.  

~~~

This project starts a REST server at http://127.0.0.1:9098 and exposes our service at /cxf/demos/ with resources:
 - /persons -> POST and PUT methods
 - /persons/{name} -> GET method

 The REST service processes the message by sending it to our first Camel route. The first route reads a header and directs the message based on the header.

The secondary routes marshals the data and puts it in a queue asynchronously. After that, our service simulates a response.
We could have done a synchronous transaction to the queue, to ensure we our message was delivered to the queue and our response is not a false positive, however we need this simple business REST service to be fast and non-blocking.

The main logic for the REST service can be found in the src/main/java/com/redhat/usecase/service/impl/DEIMServiceImpl.java file. We receive the message, build a response, send the message to the Camel route and then set the response based on the code returned to us. Finally we send the response back to the caller. If you're feeling brave, you can modify this to send back differnt messages or codes.
