# quarkus-nagios

## Example

```
@Wellness
@Singleton
public class QueueSizeHealth implements HealthCheck {

    @Override
    public NagiosCheckResponse call() {
        int queueSize = ...
        return QUEUE_SIZE.result(queueSize).asResponse();
    }

    private static final NagiosCheck QUEUE_SIZE = NagiosCheck.named("queue size")
            .performance()           // export as performance data
            .warningIf().above(30)   // warning range
            .criticalIf().above(100) // critical range
            .build();
}
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

## deploy

```
> mvn -DnewVersion=x.x.x versions:set
> mvn -DnewVersion=x.x.x -pl quarkus-nagios-extension versions:set
> mvn -Prelease clean deploy
```
