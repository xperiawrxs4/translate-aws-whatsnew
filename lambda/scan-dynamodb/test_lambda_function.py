import unittest
from unittest import mock
import json
import os
import sys
from botocore.exceptions import ClientError

# Add the lambda function's directory to the Python path
sys.path.append(os.path.join(os.path.dirname(__file__), '.'))

# Import the function to be tested
try:
    import lambda_function # Assuming the file is lambda_function.py
except ImportError:
    # Fallback for local testing
    if os.path.exists(os.path.join(os.path.dirname(__file__), 'lambda_function.py')):
        from . import lambda_function
    else:
        raise

class TestScanDynamoDBLambda(unittest.TestCase):
    @mock.patch('lambda_function.table') # Mock the global table object
    def test_lambda_handler_success(self, mock_table_obj):
        # Configure the mock table and its scan method
        mock_items = [
            {'id': '1', 'title': 'Test News 1'},
            {'id': '2', 'title': 'Test News 2'}
        ]
        mock_table_obj.scan.return_value = {'Items': mock_items}

        # Call the handler
        response = lambda_function.lambda_handler({}, None)

        # Assertions
        mock_table_obj.scan.assert_called_once()
        self.assertEqual(response['statusCode'], 200)
        self.assertEqual(response['headers']['Content-Type'], 'application/json')
        self.assertEqual(response['headers']['Access-Control-Allow-Origin'], '*')
        self.assertEqual(json.loads(response['body']), mock_items)

    @mock.patch('lambda_function.table') # Mock the global table object
    def test_lambda_handler_scan_no_items(self, mock_table_obj):
        # Configure the mock table for a scan that returns no items
        mock_table_obj.scan.return_value = {'Items': []} # Or response.get("Items", []) would handle if 'Items' key is missing

        # Call the handler
        response = lambda_function.lambda_handler({}, None)

        # Assertions
        mock_table_obj.scan.assert_called_once()
        self.assertEqual(response['statusCode'], 200)
        self.assertEqual(json.loads(response['body']), [])

    @mock.patch('lambda_function.table') # Mock the global table object
    def test_lambda_handler_scan_exception(self, mock_table_obj):
        # Configure the mock table to raise an exception on scan
        # from botocore.exceptions import ClientError # Already imported at the top
        mock_table_obj.scan.side_effect = ClientError(
            error_response={'Error': {'Code': 'DynamoDBError', 'Message': 'Failed to scan'}},
            operation_name='Scan'
        )

        # Call the handler
        response = lambda_function.lambda_handler({}, None)

        # Assertions
        mock_table_obj.scan.assert_called_once()
        self.assertEqual(response['statusCode'], 500)
        # The exact error message from ClientError can be a bit complex to reconstruct perfectly by hand.
        # Let's check for the presence of the key parts.
        response_body = json.loads(response['body'])
        self.assertTrue('error' in response_body)
        self.assertTrue('DynamoDBError' in response_body['error']) # Check if the error message contains the error code
        self.assertTrue('Failed to scan' in response_body['error']) # Check if the error message contains the message

if __name__ == '__main__':
    unittest.main()
