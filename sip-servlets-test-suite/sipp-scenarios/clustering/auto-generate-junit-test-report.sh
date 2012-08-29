echo "generating tests reports"
export numTests=`cat $1 | wc -l`
export numPassed=`grep "0" $1 | wc -l`
export reportfile=$2

rm -rf $reportfile

export numFailed=$((numTests-numPassed))

echo '<?xml version="1.0" encoding="UTF-8" ?>' >> $reportfile
echo '<testsuite errors="0" failures="'$numFailed'" hostname="host" name="org.mobicents.servlet.sipp.failover" tests="'$numTests'" time="1" timestamp="2010-03-15T02:06:01">'>> $reportfile
echo '  <properties>'>> $reportfile
echo '  </properties>'>> $reportfile
for child in $(cat $1 | awk "{ if ( \$2 == 0 ) { print \$1 }}")
do
  echo '  <testcase classname="org.mobicents.servlet.sipp.failover.SipServletsFailoverTest" name="'$child'" time="1" />'>> $reportfile
done

for child in $(cat $1 | awk "{ if ( \$2 != 0 ) { print \$1 }}")
do
  echo '  <testcase classname="org.mobicents.servlet.sipp.failover.SipServletsFailoverTest" name="'$child'" time="1" ><failure message="Not available" type="junit.framework.AssertionFailedError">junit.framework.AssertionFailedError: expected sipp to return 0</failure></testcase>'>> $reportfile
done
echo '  <system-out><![CDATA[Data is avilable in our files]]></system-out>'>> $reportfile
echo '  <system-err><![CDATA[]]></system-err>'>> $reportfile
echo '</testsuite>'>> $reportfile
