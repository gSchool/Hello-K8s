 docker rmi rlwingjr/hello-k8s:security
 ./gradlew clean build
 docker build -t rlwingjr/hello-k8s:security .
 docker push rlwingjr/hello-k8s
 kubectl delete -f hello-deploy.yaml
 kubectl apply -f hello-deploy.yaml

