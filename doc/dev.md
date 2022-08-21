# Documentation

TBD


## Building base container

0. Select a build name, e.g. `export BUILD=lab-3-4-5-clojupyter-0-3-5-base-1`

1. Update `.devcontainer/Dockerfile` with the appropriate changes.

1a. `mkdir tmp`

2. run `docker build -f .devcontainer/Dockerfile -t krukow/edmondson:$BUILD ./tmp`

3. run `docker push krukow/edmondson:$BUILD`


## Building a -dev docker container

Use the latest `$BUILD` (see building base container).

```
docker run -it -u jovyan -p 8888:8888 \
            -v ${PWD}:/home/jovyan/work/ \
            -w /home/jovyan/ \
            krukow/edmondson:$BUILD \
            /bin/bash
```

2. In container bash:

``` bash
(base) jovyan@4234588889ae:~$ cd work/
(base) jovyan@4234588889ae:~/work$ ./script/go.sh docker
Building
Downloading:...
...
Installation successful.

exit(0)
(base) jovyan@d79e868e1e4c:~/work$ /usr/local/bin/clojure -A:upload -P
Downloading:...
```

3. In host shell `docker ps` to finder container id; then

``` bash
docker commit --author "Karl Krukow <krukow@github.com>" -m "<msg>" <cont-id> krukow/edmondson:<dev-version>
```

If you just have that one container:

```bash
dev_version="lab-3-4-5-clojupyter-0-3-5-dev-1"
container_id=$(docker ps --format "{{.ID}}")
docker commit --author "Karl Krukow <krukow@github.com>" -m "Dev container for $BUILD" $container_id krukow/edmondson:$dev_version
```


4. In host shell

``` bash
docker push krukow/edmondson:$dev_version
```
