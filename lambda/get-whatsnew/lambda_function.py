import boto3
import feedparser
import hashlib
import datetime
import os
from botocore.exceptions import BotoCoreError, ClientError

# 環境変数から取得
RSS_FEED_URL = os.getenv(
    "RSS_FEED_URL", "https://aws.amazon.com/about-aws/whats-new/recent/feed/"
)
DYNAMODB_TABLE = os.getenv("DYNAMODB_TABLE", "AWS_News")

# AWS クライアント
dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table(DYNAMODB_TABLE)
translate = boto3.client("translate")


def translate_text(text, source_lang="en", target_lang="ja"):
    """AWS Translate を使ってテキストを翻訳（HTML をそのまま翻訳）"""
    if not text:
        return ""

    try:
        response = translate.translate_text(
            Text=text, 
            SourceLanguageCode=source_lang, 
            TargetLanguageCode=target_lang
        )
        return response["TranslatedText"]
    except (BotoCoreError, ClientError) as e:
        import logging
        logging.error(f"Translation Error: {e}")
        return text  # 翻訳に失敗しても元のテキストを返す


def fetch_latest_rss():
    """RSS フィードを取得し、過去7日間のデータを処理"""
    feed = feedparser.parse(RSS_FEED_URL)
    now = datetime.datetime.now(datetime.timezone.utc)
    seven_days_ago = now - datetime.timedelta(days=7)

    for entry in feed.entries:
        # タイムゾーンをUTCに設定して日時を作成
        pub_date = datetime.datetime(
            *entry.published_parsed[:6],
            tzinfo=datetime.timezone.utc
        )

        # 7日以上前のデータは無視
        if pub_date < seven_days_ago:
            continue

        unique_id = hashlib.sha256(entry.link.encode()).hexdigest()

        # **DynamoDB にすでに存在するかチェック**
        existing_item = table.get_item(Key={"id": unique_id}).get("Item")
        if existing_item:
            continue  # **重複登録を防ぐ**

        # **翻訳処理（HTML付きのまま翻訳）**
        title_ja = translate_text(entry.title, "en", "ja")
        description_ja = translate_text(
            entry.description, "en", "ja"
        )  # 変更: strip_html を削除

        # **DynamoDB に保存**
        table.put_item(
            Item={
                "id": unique_id,
                "pubdate": entry.published,
                "title_en": entry.title,
                "title_ja": title_ja,
                "description_en": entry.description,
                "description_ja": description_ja,  # HTML 付きのまま保存
                "category": entry.get("category", "Unknown"),
                "author": entry.get("author", "Unknown"),
                "link": entry.link,
            }
        )


def lambda_handler(event, context):
    """Lambda 関数のエントリポイント"""
    fetch_latest_rss()
    return {"statusCode": 200, "body": "RSS data fetched and stored successfully."}
