# get-aws-whatsnew

Lambda 関数で AWS の What’s New の英語版を取得して日本語に翻訳して Android アプリで表示する
詳細画面のボタンからはオリジナル（英語）のページへ遷移します（デフォルトブラウザで開くはず）

![aws-whatsnew-1](https://github.com/user-attachments/assets/23baf1d6-f6f2-41d3-8c79-29f97632f952)

## アーキテクチャ図
![アーキテクチャ図 drawio](https://github.com/user-attachments/assets/85bda7e7-afe5-4adf-b8ec-a969886313c3)

## Android アプリ側の設定

- NewsRepository.kt
  - @GET("your_api_endpoint")　：API のエンドポイントを指定してください
  - .baseUrl("your_api_gateway_endpoint")　：API Gateway のエンドポイントを指定してください
- NetworkModule.kt
  - .baseUrl("your_api_gateway_endpoint")　：API Gateway のエンドポイントを指定してください

## Lambda 設定

- Lambda 環境変数を作成してください
  - 一応、デフォルト値入れているので RSS の URL 側はそのままでも OK
  - DynamoDB のテーブル名は任意のテーブルを指定する場合は、環境変数でそのテーブル名を指定してください
- get-whatsnew
  - RSS で情報を取得して、Translate で日本語に翻訳し DynamoDB へ保存する
  - 実行日から 7 日前以降のデータを取得　※取得データおよび翻訳量が多いとコストかかるかなと思い。。
  - 重複チェック有
  - feedparser のレイヤーもしくは zip を追加してください
- scan-dynamodb
  - Android アプリ側で API Gateway 経由で呼び出す DynamoDB のデータ取得用関数
