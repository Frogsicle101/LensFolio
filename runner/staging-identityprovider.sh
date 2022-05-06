fuser -k 9500/tcp || true
fuser -k 9502/tcp || true
java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
  --spring.application.name=identity-provider \
  --grpc.server.port=9502 \
  --server.port=9500 \
  --protocol=https \
  --hostName=csse-s302g6.canterbury.ac.nz \
  --port=443
  --rootPath=/test/identityprovider
