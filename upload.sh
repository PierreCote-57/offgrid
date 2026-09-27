#!/bin/bash
#
#	upload.sh
#
#	Mirrors folder.local to the FullHost node, all but logs/ (the node writes its own): a file
#	deleted here is deleted there. The local folder is read from application-local.yaml, the node's folder
#	is folder.local in application-host.yaml. fullhost-offgrid is the entry in ~/.ssh/config.
#	Extra arguments go to rsync: ./upload.sh --dry-run
#
set -e
cd "$(dirname "$0")"

LOCAL_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' src/main/resources/application-local.yaml)
HOST_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' src/main/resources/application-host.yaml)
HOST="fullhost-offgrid"

rsync -avz --progress --delete --exclude logs "$@" "$LOCAL_FOLDER/" "$HOST:$HOST_FOLDER/"
