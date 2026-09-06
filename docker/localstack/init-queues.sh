#!/bin/bash
set -euo pipefail

queue_name="notification-queue"
dead_letter_queue_name="notification-queue-dlq"

awslocal sqs create-queue --queue-name "$dead_letter_queue_name" >/dev/null
dead_letter_queue_url="$(awslocal sqs get-queue-url --queue-name "$dead_letter_queue_name" --query QueueUrl --output text)"
dead_letter_queue_arn="$(awslocal sqs get-queue-attributes --queue-url "$dead_letter_queue_url" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"
redrive_policy="{\"deadLetterTargetArn\":\"$dead_letter_queue_arn\",\"maxReceiveCount\":\"3\"}"
escaped_redrive_policy=${redrive_policy//\"/\\\"}
queue_attributes="{\"RedrivePolicy\":\"$escaped_redrive_policy\"}"

awslocal sqs create-queue --queue-name "$queue_name" >/dev/null
queue_url="$(awslocal sqs get-queue-url --queue-name "$queue_name" --query QueueUrl --output text)"
awslocal sqs set-queue-attributes \
    --queue-url "$queue_url" \
    --attributes "$queue_attributes" >/dev/null
