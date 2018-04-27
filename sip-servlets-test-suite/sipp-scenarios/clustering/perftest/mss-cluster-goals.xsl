<?xml version="1.0" encoding="windows-1252"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="false" omit-xml-declaration="yes"/>
	<xsl:include href="junitTestCaseTemplate.xsl"/>

	<xsl:template match="/" priority="9">
		<testsuite>
			<xsl:for-each select="//key[text()='Cpu']/parent::entry/value/median">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'CPUMedian'" />
					<xsl:with-param name="thresholdValue"  select="'67'" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='Mem']/parent::entry/value/min">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'MemMin'" />
					<xsl:with-param name="thresholdValue"  select="'500'" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='GcMemAfter']/parent::entry/value/min">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'GcMemAfterMin'" />
					<xsl:with-param name="thresholdValue"  select="'500'" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='GcPauseDuration']/parent::entry/value/median">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'GcPauseDurationMedian'" />
					<xsl:with-param name="thresholdValue"  select="'500'" />
				</xsl:call-template>
			</xsl:for-each>


			<xsl:for-each select="//key[text()='SIPTotalCallCreated']/parent::entry/value/sum">
				<xsl:call-template name="biggerThanTemplate">
					<xsl:with-param name="caseName" select="'SIPTotalCallCreatedSum'" />
					<xsl:with-param name="thresholdValue"  select="'300'" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='SIPFailedCalls']/parent::entry/value/sum">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'SIPFailedCallsSum'" />
					<xsl:with-param name="thresholdValue"  select="'10'" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='SIPFailedCalls']/parent::entry/value/sum">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'SIPFailedCallsRatio'" />
					<xsl:with-param name="thresholdValue"  select="'0.1'" />
					<xsl:with-param name="measA"  select="//key[text()='SIPFailedCalls']/parent::entry/value/sum" />
					<xsl:with-param name="measB"  select="//key[text()='SIPTotalCallCreated']/parent::entry/value/sum" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="//key[text()='SIPRetransmissions']/parent::entry/value/sum">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'SIPRetransmissionsSum'" />
					<xsl:with-param name="thresholdValue"  select="'100'" />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select="//key[text()='SIPResponseTime']/parent::entry/value/median">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'SIPResponseTimeMedian'" />
					<xsl:with-param name="thresholdValue"  select="'100'" />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select="//key[text()='HTTPElapsed']/parent::entry/value/median">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'HTTPElapsed'" />
					<xsl:with-param name="thresholdValue"  select="'100'" />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select="//key[text()='HTTPConnect']/parent::entry/value/median">
				<xsl:call-template name="lessThanTemplate">
					<xsl:with-param name="caseName" select="'HTTPConnect'" />
					<xsl:with-param name="thresholdValue"  select="'100'" />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select="//key[text()='HTTPErrorCount']/parent::entry/value/sum">
					<xsl:call-template name="ratioLessThanTemplate">
							<xsl:with-param name="caseName" select="'HTTPFailureRatio'" />
							<xsl:with-param name="thresholdValue"  select="'0.1'" />
							<xsl:with-param name="measA"  select="//key[text()='HTTPErrorCount']/parent::entry/value/sum" />
							<xsl:with-param name="measB"  select="//key[text()='HTTPSampleCount']/parent::entry/value/sum" />
					</xsl:call-template>
			</xsl:for-each>

		</testsuite>
	</xsl:template>
</xsl:stylesheet>
