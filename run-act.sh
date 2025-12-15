#!/bin/bash
act -P ubuntu-latest=custom-act-image:latest --pull=false --container-options "--privileged" --env TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal

