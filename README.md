# quarkus-nagios

## deploy

```
> mvn -DnewVersion=x.x.x versions:set
> mvn -DnewVersion=x.x.x -pl quarkus-nagios-extension versions:set
> mvn -Prelease clean deploy
```
