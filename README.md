# quarkus-nagios

## deploy

```
> mvn -DnewVersion=x.x.x versions:set
> mvn -DnewVersion=x.x.x -pl quarkus-nagios-extension versions:set
> mvn -Prelease clean deploy
```

## Nagios

Shell script to set-up as an active service check in nagios:

```
#!/bin/bash

url=$1

if [ -z "$url" ]; then
    echo "usage: $0 URL"
    exit
fi

declare -A statusmap=(
  ["OK"]="0"
  ["WARN"]="1"
  ["WARNING"]="1"
  ["CRIT"]="2"
  ["CRITICAL"]="2"
  ["UNKNOWN"]="3"
)

result=$(curl --fail $url 2>/dev/null|head -1)

status=${result%%:*}

exitcode=3
if [ -z "$result" ]; then
    result="UNKNOWN: Got unkown result from $url"
elif [ -z "${statusmap[$status]}" ]; then
    exitcode=${statusmap[$status]}
fi

echo $result
exit $exitcode
```