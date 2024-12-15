#!/bin/bash
set -eou pipefail
git submodule update --init --recursive
cd paperweight
./gradlew pTML
