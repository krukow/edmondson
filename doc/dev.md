# Documentation

TBD


## Building base container

1. Update `.devcontainer/Dockerfile` with the appropriate changes.

1a. `mkdir tmp`

2. run `docker build -f .devcontainer/Dockerfile -t krukow/edmondson:<version> ./tmp`

3. run `docker push krukow/edmondson:<version>`


## Building a -dev docker container

Use the latest `<base-container-version>` (see building base container).

```
docker run -it -u jovyan -p 8888:8888 \
            -v ${PWD}:/home/jovyan/work/ \
            -w /home/jovyan/ \
            krukow/edmondson:<base-container-version> \
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
docker commit --author "Karl Krukow <krukow@github.com>" -m "<msg>" <cont-id> krukow/edmondson:<version>
```

4. In host shell

``` bash
docker push krukow/edmondson:<version>
```
