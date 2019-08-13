#!/usr/bin/env bash

rsync -avh --progress ../target/integrations.jar distil-cass2:/tmp
