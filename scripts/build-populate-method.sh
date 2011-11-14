#!/bin/bash

THISDIR=$( cd $(dirname $0) && pwd)
DICTSOURCE='SEO-GoogleMaps-names.txt'

echo 'private void populateGm2seoDict() {'
echo -e "\tgm2seo = new Hashtable<String, SEOStreet>();"

sed 's/^/\tgm2seo.put("/g' < $THISDIR/$DICTSOURCE | sed 's/|/", new SEOStreet("/g' | sed 's/:/", "/g' | sed 's/$/"));/g'

echo '}'
