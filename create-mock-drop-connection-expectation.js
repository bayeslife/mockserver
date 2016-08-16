var mockServer = require('mockserver-client'),
    mockServerClient = mockServer.mockServerClient;

mockServerClient("localhost", 1080).mockAnyResponse(
    {
        'httpRequest': {
            'method': 'GET',
            'path': '/api/.*'
        },
        'httpError': {
            'dropConnection': true
        },
        'times': {
            'unlimited': true
        }
    }
);
