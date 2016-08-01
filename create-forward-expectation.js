var mockServer = require('mockserver-client'),
    mockServerClient = mockServer.mockServerClient;


mockServerClient("localhost", 1080).mockAnyResponse(
    {
        'httpRequest': {
            'method': 'GET',
            'path': '/api/.*'
        },
        "httpForward": {
            "host": "localhost",
            "port": 80,
            "scheme": "HTTP"
        },
        'times': {
            'unlimited': true
        }
    }
);
