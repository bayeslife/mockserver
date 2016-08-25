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
            'body': JSON.stringify({ someproperty: 'somevalue' }),
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
