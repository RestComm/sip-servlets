# Compile sipp
wget -m -nd http://superb-east.dl.sourceforge.net/sourceforge/sipp/sipp-3.1.src.tar.gz
tar -xzf sipp-3.1.src.tar.gz
cd sipp-3.1.src

wget -m -nd http://ftp.gnu.org/pub/gnu/ncurses/ncurses-5.6.tar.gz
tar -xzf ncurses-5.6.tar.gz
cd ncurses-5.6
./configure > /dev/null
make > /dev/null
cd ..
wget -m -nd http://people.redhat.com/lbarreir/hudson/call-setup-test/Makefile
make > /dev/null
mv -f sipp ..
cd ..
rm -fr  sipp-3.1.src*
