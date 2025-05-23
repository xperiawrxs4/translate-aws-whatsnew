# get-aws-whatsnew

Lambda 関数で AWS の What’s New の英語版を取得して日本語に翻訳して Android アプリで表示する
詳細画面のボタンからはオリジナル（英語）のページへ遷移します（デフォルトブラウザで開くはず）

![aws-whatsnew-1](https://github.com/user-attachments/assets/23baf1d6-f6f2-41d3-8c79-29f97632f952)
![aws-whatsnew-pulltorefresh](https://github.com/user-attachments/assets/088b76be-de24-4af4-b9f8-f6de01e1d4fe)

## アーキテクチャ図
![アーキテクチャ図 drawio](https://github.com/user-attachments/assets/85bda7e7-afe5-4adf-b8ec-a969886313c3)

## Android アプリ側の設定

- NewsApiService.kt
  - @GET("news")　：API のエンドポイントを指定してください　例でnewsを指定しています
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

## Lambda 関数のユニットテスト

プロジェクトには、Lambda 関数のためのユニットテストが含まれています。これらのテストは `pytest` を使用して実行できます。

### テストのセットアップ

1.  テストの依存関係をインストールします。リポジトリのルートディレクトリで次のコマンドを実行します（もしくは `lambda` ディレクトリで直接実行しても構いませんが、パスの調整が必要になる場合があります）。
    ```bash
    pip install -r lambda/requirements-dev.txt
    ```

### テストの実行

1.  `lambda` ディレクトリに移動します。
    ```bash
    cd lambda
    ```
2.  `pytest` を実行します。
    ```bash
    pytest
    ```
    これにより、`get-whatsnew` および `scan-dynamodb` サブディレクトリ内の `test_*.py` ファイルが自動的に検出され、実行されます。

    テスト実行時に `PYTHONPATH` の設定が必要な場合や、モジュールが見つからないエラーが出る場合は、`lambda` ディレクトリから次のように `PYTHONPATH` を設定して `pytest` を実行してみてください。
    ```bash
    PYTHONPATH=. pytest
    ```
    これにより、`lambda` ディレクトリ内のモジュール（`get-whatsnew` や `scan-dynamodb` の中の `lambda_function.py`）が正しくインポートされるようになります。
