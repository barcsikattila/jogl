#! /bin/sh

if [ -e /opt-share/etc/profile.ant ] ; then
    . /opt-share/etc/profile.ant
fi


#    -Dc.compiler.debug=true 

ant \
    -Djogl.cg=1 \
    -Drootrel.build=build-macosx \
    $* 2>&1 | tee make.jogl.all.macosx.log
