# Using mockserver

In the past , a client needed a way to configure, maintain and run mocks. I had investigated an approach ([mocked app](https://docs.mulesoft.com/mule-user-guide/v/3.6/configuring-reconnection-strategies))based on a nodejs based recorder and mocker but the testers were more comfortable with Soap UI and already had some mechanism to manage and run mocks.

Then recently they ran into a problem invoking Soap UI mocks over https.  In investigating a solution for this problem ,
[Mockserver netty ](https://docs.mulesoft.com/mule-user-guide/v/3.6/configuring-reconnection-strategies) came across my radar.

Its quite a handy mocking capability and I realized that it is reasonably fully featured. 

## Proxying from HTTPS to HTTP

Initially we were just trying to get it to run as a standalone process to proxy requests from https (the client) to http (soap ui mocking listener).

This took a bit of figuring out as initially it wasnt quite clear what was the problem.

We could run mockserver-netty as a standalone application with it proxying requests through to Soap UI.  
```
java -jar ./mockserver-netty-3.10.4-jar-with-dependencies.jar -proxyPort 4444 -proxyRemotePort 80 -proxyRemoteHost localhost
```

If a web server was running and serving content.
```
$ curl http://localhost:80/api/something
{ "foo" : "bar" }
```

We could get to the same content through the mockserver running as a proxy.

```
$ curl http://localhost:4444/api/something
{ "foo" : "bar" }
```

As this proxy runs both http and https on the same port you can also access the content over https.
```
$ curl -k https://localhost:4444/api/something
{ "foo" : "bar" }
```

Note though that I had to use the -k option in order to disable the ssl certificate verification.

This last part was the sticking point as we had to figure out what certificate needed to be trusted and had to make this available to the mule application as it ran.

There is a certificate inside the jar file that needs to extracted to be able to import into a trust store.
```
jar xvf mockserver-netty-3.10.4-jar-with-dependencies.jar  org/mockserver/socket/CertificateAuthorityCertificate.pem

keytool -import -alias mock -file org/mockserver/socket/CertificateAuthorityCertificate.pem -keystore trust.jks
```

## Running standalone.

After reviewing more of the mockserver documentation I realized that it offers the capability that we had previously been looking for and that I had prototyped as
[mockedapp](https://github.com/bayeslife/mockedapp).

It supports a mode where the mocks can be injected into the mockserver and is referred to in that documentation as 'Creating Expectations'

This allows test cases to represent the mocks as well as the test requests and assertions. As the test runs it can insert the mock into the mockserver before submit the request to the system under test.

To demonstrate this behaviour start the mockserver with a server port
```
java -jar ./mockserver-netty-3.10.4-jar-with-dependencies.jar -serverPort 1080 -proxyRemoteHost localhost
```

then run the following to retrieve javascript dependencies
and send a forward expection to a local web server.
```
npm install
node create-forward-expectations.js
```

If the web server server some json to the following curl
```
curl http://localhost:80/api/something
{ "foo" : "bar" }
```
then you can do the same request on https through the mockserver.
```
curl https://localhost:1080/api/something
{ "foo" : "bar" }
```

You can create expectations with javascript or java or ruby.

## Injecting mocks

Expectations can be requests to return mock responses.
The following sample generate a simple name/value response after a delay of 5 secs.

```
var mockServer = require('mockserver-client'),
    mockServerClient = mockServer.mockServerClient;

mockServerClient("localhost", 1080).mockAnyResponse(
    {
        'httpRequest': {
            'method': 'GET',
            'path': '/api/.*'
        },
        'httpResponse': {
            'statusCode': 200,
            'body': JSON.stringify({ name: 'value' }),
            'delay': {
                'timeUnit': 'MILLISECONDS',
                'value': 5000
            }
        },
        'times': {
            'unlimited': true
        }
    }
);
```

## Running in a unit test

Because the mockserver is just java code, it is possible to provision and deprovision the mockserver during the @before and @after of the unit test method, as well as injecting the mock exceptations.

```
@Before
    public void startProxy() {
        mockServer = startClientAndServer(1080);


        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/simple")
        )
                .respond(
                        response()
                                .withStatusCode(202)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")
                                )
                                .withBody("{ message: 'incorrect username and password combination' }")
                                .withDelay(new Delay(TimeUnit.SECONDS, 1))
                );
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }
```
