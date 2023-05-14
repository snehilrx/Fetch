#!/usr/bin/env bash

echo "${BASE64_STRING}" | base64 --decode > ${KEY_PATH}
