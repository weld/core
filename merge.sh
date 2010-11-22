#!/bin/sh

usage()
{
cat << EOF
usage: $0 options

This script will checkout a branch of Weld, update JBoss AS with it, and run
all the tests against it. It will then merge the branch to master for final
review. It will not push the branch to upstream.

OPTIONS:
   -h      Show this usage message
   -b      The name of the branch to checkout
   -m      The name of the branch to merge into, by default "master"
   -v      Be more verbose
EOF
}

work()
{
echo "Checking out $BRANCH"
TMP_LOCAL_BRANCH=tmp_local_branch_$BRANCH
git checkout -b $TMP_LOCAL_BRANCH $BRANCH
git rebase $BASE_BRANCH
echo "Rebased $BRANCH onto $BASE_BRANCH"
echo "Executing $BUILD_COMMAND"
`$BUILD_COMMAND`
git checkout $BASE_BRANCH
echo "Merging $BRANCH into $BASE_BRANCH"
git merge $TMP_LOCAL_BRANCH
git status
}

BRANCH=
VERBOSE=0
RUN=1
BASE_BRANCH="origin/master"
BUILD_COMMAND="mvn clean install && mvn verify -Dincontainer"

while getopts "vhb:m:" OPTION
do
     case $OPTION in
         h)
             usage
             RUN=0
             ;;
         b)
             BRANCH=$OPTARG
             ;;
         m)
             BASE_BRANCH=$OPTARG
             ;;
         v)
             VERBOSE=1
             ;;
         [?])
             usage;
             RUN=0
             ;;
     esac
done

if [ "$RUN" -eq "1" ]
then
   work;
fi
