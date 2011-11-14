#!/bin/bash

THISDIR=$( cd $(dirname $0) && pwd)
DICTSOURCE='SEO-GoogleMaps-names.txt'

echo 'private void populateGm2seoDict() {'

sed 's:^:\tgm2seo.put(":' < $THISDIR/$DICTSOURCE | sed 's/:/", "/' | sed 's:$:");:'

echo '}'
