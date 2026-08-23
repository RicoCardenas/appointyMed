#!/bin/bash
set -euo pipefail

awslocal sqs create-queue --queue-name notification-queue >/dev/null
