package org.opendolphin.core.client.comm

import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.DefaultHttpClient
import org.opendolphin.core.client.ClientDolphin
import org.opendolphin.core.comm.Command
import org.opendolphin.core.comm.CreatePresentationModelCommand
import org.opendolphin.core.comm.JsonCodec
import org.opendolphin.core.comm.SignalCommand

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HttpClientConnectorTests extends GroovyTestCase {

    HttpClientConnector connector

    @Override
    protected void setUp() throws Exception {
        connector = new HttpClientConnector(new ClientDolphin(), 'dummyURL')
        connector.throwExceptionOnSessionChange = false
        connector.codec = new JsonCodec()

    }

    void testCallConnector() {
        connector.httpClient = new DefaultHttpClient() {
            @Override
            def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
                return """
                        [{"pmId":"p1","clientSideOnly":false,"id":"CreatePresentationModel","attributes":[],"pmType":null,"className":"org.opendolphin.core.comm.CreatePresentationModelCommand"}]
                        """
            }
        }
        def result = connector.transmit([new CreatePresentationModelCommand(pmId: 'p1')])
        assert 1 == result.size()
        assert result[0] instanceof CreatePresentationModelCommand
        assert 'p1' == result[0].pmId
    }

    void testSignal() {
        connector.setReleaseCommand(new SignalCommand("test signal"))
        connector.waiting = true
        CountDownLatch httpWasCalled = new CountDownLatch(1)
        connector.signalHttpClient = new DefaultHttpClient() {
            @Override
            def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
                httpWasCalled.countDown()
                return "[]"
            }
        }
        connector.release()
        sleep 100 // make sure the SimpleResponseHandler had time to be called
        httpWasCalled.await(2, TimeUnit.SECONDS)
        assert 0 == httpWasCalled.count
    }


    void testCallWithException() {
        try {
            connector.transmit([new Command()])
            fail()
        } catch (Exception e) {
            // ignore
        }

    }
}
