# Camel on Quarkusプロジェクトデプロイガイド

このガイドは、Camel on Quarkusプロジェクトをビルドし、Docker Hubにプッシュし、KafkaおよびCamelアプリケーションをKubernetesにデプロイし、RESTリクエストを受け入れるサービスを作成するためのステップバイステップの手順を提供します。

## 前提条件

1. Docker
2. Docker Hubアカウント
3. Kubernetesクラスター
4. Kubernetesクラスターに構成された`kubectl`コマンドラインツール
5. Apache Kafka

## 1. Camel on QuarkusプロジェクトのビルドとDocker Hubへのプッシュ

### ステップ1: プロジェクトのビルド

1. プロジェクトディレクトリに移動します。
2. Mavenを使用してQuarkusプロジェクトをビルドします。
   ```sh
   ./mvnw clean package -Dquarkus.package.type=fast-jar
   ```

### ステップ2: Dockerイメージの作成

1. プロジェクトのルートにDockerfileを作成します。
   ```dockerfile
   FROM quay.io/quarkus/ubi-quarkus-native-image:21.3-java11 as build
   COPY . /project
   WORKDIR /project
   RUN ./mvnw clean package -Pnative -Dquarkus.native.container-build=true

   FROM registry.access.redhat.com/ubi8/ubi-minimal
   WORKDIR /work/
   COPY --from=build /project/target/*-runner /work/application
   COPY --from=build /project/target/lib/* /work/lib/
   ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
   ```

2. Dockerイメージをビルドします。
   ```sh
   docker build -t <your-dockerhub-username>/camel-kafka-app:latest .
   ```

### ステップ3: DockerイメージをDocker Hubにプッシュ

1. Docker Hubにログインします。
   ```sh
   docker login
   ```

2. イメージをプッシュします。
   ```sh
   docker push <your-dockerhub-username>/camel-kafka-app:latest
   ```

## 2. KafkaをKubernetesにデプロイ

1. Strimzi Kafka Operator をインストールします。
   ```sh
   kubectl create namespace kafka
   kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
   ```

2. KafkaのYAML構成を適用します。
   ```sh
   kubectl apply -f kafka.yaml -n kafka
   ```

## 3. CamelアプリケーションをKubernetesにデプロイ

### ステップ1: ConfigMapの作成

1. CamelのConfigMapを適用します。
   ```sh
   kubectl apply -f camel-configmap.yaml -n kafka
   ```

### ステップ2: アプリケーションのデプロイ

1. デプロイメント構成を適用します。
   ```sh
   kubectl apply -f deployment.yaml -n kafka
   ```

### ステップ3: サービスの作成

1. サービス構成を適用します。
   ```sh
   kubectl apply -f service.yaml -n kafka
   ```

### ステップ4: Ingressの設定 (オプション)

1. サービスを外部に公開するためにIngress構成を適用します。
   ```sh
   kubectl apply -f ingress.yaml -n kafka
   ```

## 4. デプロイメントのテストと確認

### ステップ1: Kafkaデプロイメントの確認

1. Kafkaポッドの状態を確認します。
   ```sh
   kubectl get pods -l app=kafka -n kafka
   ```

### ステップ2: Camelアプリケーションのデプロイメント確認

1. Camelアプリケーションポッドの状態を確認します。
   ```sh
   kubectl get pods -l app=camel-kafka-app -n kafka
   ```

2. サービスが稼働していることを確認します。
   ```sh
   kubectl get services -n kafka
   ```

### ステップ3: RESTエンドポイントのテスト

1. Ingressを使用している場合は、サービスの外部IPまたはDNS名を見つけます。
   ```sh
   kubectl get ingress -n kafka
   ```

2. ログの確認
   ```sh
   kubectl logs -f $(kubectl get pods -n kafka -l app=camel-kafka-app -o name) -n kafka
   ```

3. `curl`または任意のAPIクライアントを使用してRESTエンドポイントをテストします。
   ```sh
   curl --request POST \
   --url http://localhost/logs \
   --header 'accept: application/json' \
   --header 'content-type: application/json' \
   --data '{"message": "HelloHelloHello World!!!"}'
   ```

### RESTコールの例

`<external-ip-or-dns>`と`<your-endpoint>`を実際のデプロイメントの値に置き換えてください。

## 含まれているファイル

- `testcase.http`: テスト用のサンプルHTTPリクエストを含むファイル。
- `ingress.yaml`: Ingress構成ファイル。
- `service.yaml`: サービス構成ファイル。
- `deployment.yaml`: デプロイメント構成ファイル。
- `camel-configmap.yaml`: Camelアプリケーション用のConfigMapファイル。
- `kafka.yaml`: Kafkaデプロイメント構成ファイル。
- `KafkaConsumerRoute.java`: Kafkaコンシューマルート。
- `KafkaProducerRoute.java`: Kafkaプロデューサルート。
- `LogReceiverRoute.java`: ログ受信ルート。
- `application.properties`: アプリケーションプロパティファイル。

環境と要件に応じてパスと構成の詳細を適応させてください。

## 結論

これらの手順に従うことで、Camel on QuarkusアプリケーションとKubernetes上のKafkaセットアップをビルド、デプロイ、およびテストすることができるはずです。問題が発生した場合は、KubernetesおよびQuarkusのドキュメントを参照するか、それぞれのGitHubリポジトリで問題を報告してください。
