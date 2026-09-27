#!/bin/bash
#
#	upload.sh
#
#	Mirrors folder.local to a FullHost node, all but logs/ (the node writes its own): a file
#	deleted here is deleted there. The first argument is the target (test, later prod). The local
#	folder is read from application-dev.yaml, the node's folder is folder.local in
#	application-<target>.yaml. fullhost-offgrid-<target> is the entry in ~/.ssh/config.
#	Extra arguments go to rsync: ./upload.sh test --dry-run
#
set -e
cd "$(dirname "$0")"

TARGET="$1"
TARGET_YAML="src/main/resources/application-$TARGET.yaml"
if [ -z "$TARGET" ] || [ ! -f "$TARGET_YAML" ]; then
	echo "usage: ./upload.sh <target> [rsync options]   (no $TARGET_YAML)" >&2
	exit 1
fi
shift

LOCAL_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' src/main/resources/application-dev.yaml)
HOST_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' "$TARGET_YAML")
HOST="fullhost-offgrid-$TARGET"

rsync -avz --progress --delete --exclude logs "$@" "$LOCAL_FOLDER/" "$HOST:$HOST_FOLDER/"
