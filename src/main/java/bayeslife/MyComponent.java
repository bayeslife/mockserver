package bayeslife;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

/**s
 */
public class MyComponent implements Callable {

    public Object onCall(MuleEventContext eventContext) throws Exception {

        //eventContext.getMessage().setInvocationProperty("myProperty", "Hello World!");
        System.out.println("Java Component called with payload: "+eventContext.getMessageAsString());

        return eventContext.getMessage();
    }
}
