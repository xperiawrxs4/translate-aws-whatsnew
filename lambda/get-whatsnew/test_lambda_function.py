import unittest
from unittest import mock
import os
import sys
from botocore.exceptions import ClientError
import datetime
import hashlib
from datetime import timezone

# Add the lambda function's directory to the Python path
# This allows us to import the lambda_function module
sys.path.append(os.path.join(os.path.dirname(__file__), '.'))

# Import the functions to be tested
# Assuming your lambda handler is in 'lambda_function.py' as per the file listing
try:
    import lambda_function
except ImportError:
    # Fallback for local testing if the file name is slightly different or path issues
    if os.path.exists(os.path.join(os.path.dirname(__file__), 'lambda_function.py')):
        from . import lambda_function
    else:
        raise

class TestGetWhatsNewLambda(unittest.TestCase):
    @mock.patch('lambda_function.translate')
    def test_translate_text_success(self, mock_translate):
        # Configure the mock Translate client and its method
        mock_translate.translate_text.return_value = {'TranslatedText': 'こんにちは'}

        # Call the function
        result = lambda_function.translate_text('Hello', 'en', 'ja')

        # Assertions
        self.assertEqual(result, 'こんにちは')
        mock_translate.translate_text.assert_called_once_with(
            Text='Hello', SourceLanguageCode='en', TargetLanguageCode='ja'
        )

    def test_translate_text_empty_input(self):
        result = lambda_function.translate_text('')
        self.assertEqual(result, '')

    @mock.patch('lambda_function.translate')
    def test_translate_text_api_failure(self, mock_translate):
        # Configure the mock to simulate an API error
        mock_translate.translate_text.side_effect = ClientError(
            error_response={'Error': {'Code': 'SomeError', 'Message': 'Details'}},
            operation_name='TranslateText'
        )
    
        # Call the function with logging check
        original_text = "Hello, world"
        with self.assertLogs('root', level='ERROR') as cm:
            result = lambda_function.translate_text(original_text, 'en', 'ja')

        # Assert that the original text is returned on failure
        self.assertEqual(result, original_text)
        mock_translate.translate_text.assert_called_once_with(
            Text=original_text, SourceLanguageCode='en', TargetLanguageCode='ja'
        )
        # Verify error was logged
        self.assertIn('ERROR:root:Translation Error:', cm.output[0])

    # It's good practice to ensure environment variables are set for tests
    # if the function directly relies on them, though translate_text itself doesn't
    # directly use os.getenv, the module it's in does.
    # We can mock them if necessary for other tests.
    # For now, these tests for translate_text should be fine.

    @mock.patch('lambda_function.feedparser.parse')
    @mock.patch('lambda_function.boto3.resource')
    @mock.patch('lambda_function.table')
    @mock.patch('lambda_function.translate_text')
    @mock.patch('lambda_function.datetime')
    def test_fetch_latest_rss(self, mock_datetime, mock_translate_text, mock_table_obj, mock_boto_resource, mock_feedparser_parse):
        # 固定の現在時刻を設定
        fixed_now = datetime.datetime(2023, 10, 27, 0, 0, 0, tzinfo=timezone.utc)
        seven_days_ago = fixed_now - datetime.timedelta(days=7)
        
        # datetime.now() の戻り値を固定
        mock_datetime.now.return_value = fixed_now
        # timedelta を実際の関数に置き換え
        mock_datetime.timedelta = datetime.timedelta
        # timezone.utc を実際の値に設定
        mock_datetime.timezone.utc = timezone.utc
        
        # datetime クラスを実際のものに置き換え
        class MockDateTime(datetime.datetime):
            @classmethod
            def now(cls, tz=None):
                return fixed_now.astimezone(tz) if tz else fixed_now
            
            @classmethod
            def utcnow(cls):
                return fixed_now.replace(tzinfo=None)
            
            def __new__(cls, *args, **kwargs):
                # 通常のdatetimeコンストラクタを呼び出す
                if args or kwargs:
                    if 'tzinfo' in kwargs and isinstance(kwargs['tzinfo'], mock.MagicMock):
                        kwargs['tzinfo'] = timezone.utc
                    return datetime.datetime.__new__(datetime.datetime, *args, **kwargs)
                return fixed_now
        
        mock_datetime.datetime = MockDateTime


        # 2. Mock feedparser.parse
        mock_feed_entry_old = mock.Mock()
        mock_feed_entry_old.link = "http://example.com/old_news"
        # published_parsed should be a time.struct_time object
        # UTC: 2023-10-19 00:00:00 (8 days ago from mock_now)
        mock_feed_entry_old.published_parsed = datetime.datetime(2023, 10, 19, 0, 0, 0, tzinfo=datetime.timezone.utc).timetuple()
        mock_feed_entry_old.title = "Old News Title"
        mock_feed_entry_old.description = "Old News Description"
        mock_feed_entry_old.published = "Thu, 19 Oct 2023 00:00:00 +0000"
        mock_feed_entry_old.get.side_effect = lambda key, default: {'category': 'Old Category', 'author': 'Old Author'}.get(key, default)


        mock_feed_entry_new_exists = mock.Mock()
        mock_feed_entry_new_exists.link = "http://example.com/new_news_exists"
        # UTC: 2023-10-26 00:00:00 (1 day ago from mock_now)
        mock_feed_entry_new_exists.published_parsed = datetime.datetime(2023, 10, 26, 0, 0, 0, tzinfo=datetime.timezone.utc).timetuple()
        mock_feed_entry_new_exists.title = "New News Title Exists"
        mock_feed_entry_new_exists.description = "New News Description Exists"
        mock_feed_entry_new_exists.published = "Wed, 25 Oct 2023 00:00:00 +0000"
        mock_feed_entry_new_exists.get.side_effect = lambda key, default: {'category': 'New Category', 'author': 'New Author'}.get(key, default)

        mock_feed_entry_new_not_exists = mock.Mock()
        mock_feed_entry_new_not_exists.link = "http://example.com/new_news_not_exists"
        # UTC: 2023-10-25 00:00:00 (2 days ago from mock_now)
        mock_feed_entry_new_not_exists.published_parsed = datetime.datetime(2023, 10, 25, 0, 0, 0, tzinfo=datetime.timezone.utc).timetuple()
        mock_feed_entry_new_not_exists.title = "New News Title Not Exists"
        mock_feed_entry_new_not_exists.description = "New News Description Not Exists"
        mock_feed_entry_new_not_exists.published = "Tue, 24 Oct 2023 00:00:00 +0000"
        mock_feed_entry_new_not_exists.get.side_effect = lambda key, default: {'category': 'Another Category', 'author': 'Another Author'}.get(key, default)

        mock_feedparser_parse.return_value = mock.Mock(entries=[
            mock_feed_entry_old,
            mock_feed_entry_new_exists,
            mock_feed_entry_new_not_exists
        ])

        # 3. Mock DynamoDB table (using the mock_table_obj passed by @mock.patch('lambda_function.table'))
        #    The lambda_function.table is already replaced by mock_table_obj by the decorator.
        def get_item_side_effect(Key):
            if Key['id'] == hashlib.sha256(mock_feed_entry_new_exists.link.encode()).hexdigest():
                return {'Item': {'id': 'some_id'}} # Item exists
            return {} # Item does not exist (empty dict means no 'Item' key)
        mock_table_obj.get_item.side_effect = get_item_side_effect
        
        # We still need to mock boto3.resource('dynamodb').Table('AWS_News') in case it's called during import or elsewhere,
        # though our primary interaction is via the global 'table' object.
        # The 'table' object in lambda_function is initialized at module level.
        # The @mock.patch('lambda_function.table', mock_table_obj) handles this for the function's execution.
        # mock_boto_resource.return_value.Table.return_value = mock_table_obj # This line might be redundant if 'lambda_function.table' is consistently used.


        # 4. Mock translate_text
        mock_translate_text.side_effect = lambda text, src, dest: f"{text}_ja"

        # --- Call the function ---
        lambda_function.fetch_latest_rss()

        # --- Assertions ---
        # Verify feedparser.parse call
        mock_feedparser_parse.assert_called_once_with(lambda_function.RSS_FEED_URL)

        # Verify DynamoDB get_item calls
        # Called for new_news_exists and new_news_not_exists
        expected_get_item_calls = [
            mock.call(Key={'id': hashlib.sha256(mock_feed_entry_new_exists.link.encode()).hexdigest()}),
            mock.call(Key={'id': hashlib.sha256(mock_feed_entry_new_not_exists.link.encode()).hexdigest()})
        ]
        mock_table_obj.get_item.assert_has_calls(expected_get_item_calls, any_order=True)
        self.assertEqual(mock_table_obj.get_item.call_count, 2)


        # Verify translate_text calls (only for the new, non-existing item)
        mock_translate_text.assert_any_call(mock_feed_entry_new_not_exists.title, 'en', 'ja')
        mock_translate_text.assert_any_call(mock_feed_entry_new_not_exists.description, 'en', 'ja')
        self.assertEqual(mock_translate_text.call_count, 2)


        # Verify DynamoDB put_item call (only for the new, non-existing item)
        expected_put_item_arg = {
            'id': hashlib.sha256(mock_feed_entry_new_not_exists.link.encode()).hexdigest(),
            'pubdate': mock_feed_entry_new_not_exists.published,
            'title_en': mock_feed_entry_new_not_exists.title,
            'title_ja': f"{mock_feed_entry_new_not_exists.title}_ja",
            'description_en': mock_feed_entry_new_not_exists.description,
            'description_ja': f"{mock_feed_entry_new_not_exists.description}_ja",
            'category': 'Another Category', # From mock_feed_entry_new_not_exists.get
            'author': 'Another Author',   # From mock_feed_entry_new_not_exists.get
            'link': mock_feed_entry_new_not_exists.link,
        }
        mock_table_obj.put_item.assert_called_once_with(Item=expected_put_item_arg)

    @mock.patch('lambda_function.fetch_latest_rss')
    def test_lambda_handler(self, mock_fetch_latest_rss):
        # Call the handler
        response = lambda_function.lambda_handler({}, None) # event and context are not used by this handler

        # Assertions
        mock_fetch_latest_rss.assert_called_once()
        self.assertEqual(response['statusCode'], 200)
        self.assertEqual(response['body'], "RSS data fetched and stored successfully.")

if __name__ == '__main__':
    unittest.main()
