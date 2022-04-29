#!/bin/bash

./build.sh

./copy-archive-to-s3.sh $1

