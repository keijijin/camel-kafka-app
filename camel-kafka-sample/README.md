# Camel Kafka App on Kubernetes

このプロジェクトは、KubernetesにKafkaをデプロイし、Camel Kafka Appをビルドしてデプロイする方法を説明します。

## 前提条件

- Kubernetes クラスターがセットアップされていること
- `kubectl` コマンドラインツールがインストールされていること
- Docker がインストールされていること

## 手順

### 1. Kafka のデプロイ

1. Strimzi Kafka Operator をインストールします：

   ```
   kubectl create namespace kafka
   kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
   ```

2. `kafka.yaml` ファイルを使用して Kafka クラスターをデプロイします：

   ```
   kubectl apply -f kafka.yaml -n kafka
   ```

3. Kafka クラスターの状態を確認します：

   ```
   kubectl get kafka -n kafka
   kubectl get pods -n kafka
   ```

### 2. Camel Kafka App のビルド

1. プロジェクトディレクトリに移動します。

2. Dockerイメージをビルドします：

   ```
   docker build -t your-docker-repo/camel-kafka-app:latest .
   ```

3. ビルドしたイメージをDockerレジストリにプッシュします：

   ```
   docker push your-docker-repo/camel-kafka-app:latest
   ```

### 3. ConfigMap の作成

`camel-configmap.yaml` ファイルを使用してConfigMapを作成します：

```
kubectl apply -f camel-configmap.yaml -n kafka
```

### 4. Camel Kafka App のデプロイ

1. `deployment.yaml` ファイル内の `image` フィールドを、ステップ2でプッシュしたイメージのURLに更新します。

2. デプロイメントを適用します：

   ```
   kubectl apply -f deployment.yaml -n kafka
   ```

3. デプロイメントの状態を確認します：

   ```
   kubectl get deployments -n kafka
   kubectl get pods -n kafka
   ```

### 5. アプリケーションのログの確認

以下のコマンドでアプリケーションのログを確認できます：

```
kubectl logs -f $(kubectl get pods -n kafka -l app=camel-kafka-app -o name) -n kafka
```

## トラブルシューティング

- ポッドが起動しない場合は、以下のコマンドでポッドの詳細を確認してください：
  ```
  kubectl describe pod <pod-name> -n kafka
  ```

- Kafkaに接続できない場合は、ConfigMapの設定とKafkaクラスターの状態を確認してください。

## クリーンアップ

アプリケーションを停止し、リソースを削除するには：

```
kubectl delete deployment camel-kafka-app -n kafka
kubectl delete configmap camel-app-config -n kafka
kubectl delete kafka kafka-cluster -n kafka
```

## 注意

本番環境にデプロイする際は、適切なセキュリティ設定やリソース制限を行ってください。
