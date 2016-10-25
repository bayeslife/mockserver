var mockServer = require('mockserver-client'),
    mockServerClient = mockServer.mockServerClient;

mockServerClient("localhost", 1080).mockAnyResponse(
    {
        'httpRequest': {
            'method': 'GET',
            'path': '/api/.*'
        },
        'httpResponse': {
            'statusCode': 500,
            'headers': [
              {
                "name": "Content-Type",
                "values": ["text/html; charset=utf-8"]
              }
            ],
            'body': '"<html><body>Test</body></html>"',
            'delay': {
                'timeUnit': 'MILLISECONDS',
                'value': 100
            }
        },
        'times': {
            'unlimited': true
        }
    }
);
