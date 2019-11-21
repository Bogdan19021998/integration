#!/usr/bin/env bash

cd ../ && mvn clean install -DskipTests=true && cd deployment/
rsync -avh --progress ../target/integrations.jar distil-cass2:/tmp
