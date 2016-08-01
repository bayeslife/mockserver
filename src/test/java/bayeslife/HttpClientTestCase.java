package bayeslife;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpForward;
import org.mockserver.model.Parameter;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.module.http.internal.request.DefaultHttpRequester;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.SystemProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpForward.forward;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;
import static org.mockserver.verify.VerificationTimes.atLeast;
import static org.mockserver.verify.VerificationTimes.once;


public class HttpClientTestCase extends FunctionalTestCase
{

    private ClientAndProxy proxy;
    private ClientAndServer mockServer;

    Logger logger = Logger.getLogger(HttpClientTestCase.class.getName());

    @Override
    protected String getConfigResources()
    {
        return "http.consumer.xml";
    }

    @Rule
    public SystemProperty hostProp = new SystemProperty("host","localhost");

    @Rule
    public SystemProperty portProp = new SystemProperty("port","4444");

    @Rule
    public SystemProperty pathProp = new SystemProperty("path","/something");


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

//        mockServer
//                .when(
//                        request()
//                                .withMethod("GET")
//                )
//                .forward(
//                        forward()
//                                .withHost("www.google.com")
//                                .withPort(80)
//                                .withScheme(HttpForward.Scheme.HTTP)
//                );
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void startApplication() throws Exception
    {

        final Flow f = (Flow)muleContext.getRegistry().lookupFlowConstruct("entry");

        List<MessageProcessor> mps = f.getMessageProcessors();

        DefaultHttpRequester req= (DefaultHttpRequester) mps.get(0);

        //req.setHost("localhost");
        //req.setPort("80");

        DefaultMuleMessage m = new DefaultMuleMessage("", muleContext);
        final MuleEvent me = new DefaultMuleEvent(m, MessageExchangePattern.REQUEST_RESPONSE,null, f);
        MuleEvent res = f.process(me);
        MuleMessage msg = res.getMessage();

    }

}
