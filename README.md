# Gor tester

A [gor](https://github.com/buger/gor) middleware app that allows you to verify if your acc server is sending (more or less) the same responses as your prod server.

## getting up and running

Build it with

```
mvn package
```

Run it with 

```
gor --input-raw <your-prod-server> --output-http=<your-staging-server> --middleware "./target/appassembler/bin/gor-tester default_config.yml" --input-raw-track-response
```

## What does it do?

gor will send each request that was sent to the production server to your staging server. 
This middleware will compare the response from the acceptation server to that of the production server.

The middleware allows you to write [rules](/src/main/java/nl/knaw/huygens/gortester/rewriterules/RewriteRule.java) that

 * [prevent certain requests](/src/main/java/nl/knaw/huygens/gortester/rewriterules/IgnoreStaticRule.java) from being replicated to the auth server
 * modify the replay request. To [replace auth headers](/src/main/java/nl/knaw/huygens/gortester/rewriterules/StoreAuthRule.java) for example.
 * modify the original or replay response. 
    * To [unchunk](/src/main/java/nl/knaw/huygens/gortester/rewriterules/UnchunkRule.java) the response. 
    * To [unzip](/src/main/java/nl/knaw/huygens/gortester/rewriterules/GunzipRule.java) the response so that they can be better compared.

The middleware also allows you to write [rules](/src/main/java/nl/knaw/huygens/gortester/differs/Differ.java) that

 * [Check if the replayed response contains all the headers of the original response](/src/main/java/nl/knaw/huygens/gortester/differs/HeaderDiffer.java) and possibly more
 * [Check if the status codes match](/src/main/java/nl/knaw/huygens/gortester/differs/StatusDiffer.java)
 * [Check if the bodies are exactly equal](/src/main/java/nl/knaw/huygens/gortester/differs/BinaryBodyDiffer.java)

Gor sends the requests and responses in an undefined order to the middleware, but your rule's methods will always be called with the data you need (the middleware stores data if needed and waits for the rest to arrive).

## How do I use it?

A few rules are provided. 
You configure them in a [yaml file](default_config.yml). 
There is a good chance that the provided rules are not enough and you'll have to add some rules of yourself.

Pull requests welcome :)
