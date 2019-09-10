#!/usr/bin/env bash

rsync -avh --progress ../target/integrations.jar distil-staging:/tmp
