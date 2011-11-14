#!/bin/bash

THISDIR=$( cd $(dirname $0) && pwd)
DICTSOURCE='SEO-GoogleMaps-names.txt'

echo 'private void populateGm2seoDict() {'

sed 's/^/\tgm2seo.put("/g' < $THISDIR/$DICTSOURCE | sed 's/|/", new SEOStreet("/g' | sed 's/:/", "/g' | sed 's/$/"));/g'

echo '}'
