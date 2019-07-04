#!/usr/bin/env bash

#jobs with datasource ids 1 and 2 will run immediately, 3 and 4 will wait for finishing any of these
#job with datasource id 10 will run immediately either, even if it was scheduled after all


curl -X POST localhost:8087/sync/schedule --data '{"orgId": 1}' --header 'Content-Type: application/json'
curl -X POST localhost:8087/sync/schedule --data '{"orgId": 2}' --header 'Content-Type: application/json'
curl -X POST localhost:8087/sync/schedule --data '{"orgId": 3}' --header 'Content-Type: application/json'
curl -X POST localhost:8087/sync/schedule --data '{"orgId": 4}' --header 'Content-Type: application/json'
curl -X POST localhost:8087/sync/run_now --data '{"orgId": 10}' --header 'Content-Type: application/json'
