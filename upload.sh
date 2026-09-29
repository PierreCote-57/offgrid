#!/bin/bash
#
#	upload.sh
#
#	Mirrors the folders of folder.local named at the bottom to a FullHost node, without .DS_Store:
#	a file deleted here is deleted there. A folder not named is never touched on the node, so
#	logs/ and ping/ stay the node's own. The first argument is the target (test, later prod). The local
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
RSYNC_OPTION_LIST=("$@")

LOCAL_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' src/main/resources/application-dev.yaml)
HOST_FOLDER=$(sed -n 's/^  local: "\(.*\)"$/\1/p' "$TARGET_YAML")
HOST="fullhost-offgrid-$TARGET"

CopyFolder() {
	rsync -avz --progress --delete --delete-excluded --exclude .DS_Store "${RSYNC_OPTION_LIST[@]}" \
		"$LOCAL_FOLDER/$1/" "$HOST:$HOST_FOLDER/$1/"
}

CopyFolder config
CopyFolder document
CopyFolder ephemeris
CopyFolder images
