wget http://sipp.sourceforge.net/snapshots/sipp.2009-07-29.tar.gz
tar -xzf sipp.*.tar.gz
make -C sipp.svn > /dev/null
mv -f sipp.svn/sipp .
rm -fr sipp.*.tar.gz sipp.svn
