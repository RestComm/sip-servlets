package org.mobicents.servlet.sip.proxy;

import java.util.TimerTask;

public class ProxyBranchTimerTask extends TimerTask{
	private ProxyBranchImpl proxyBranch;
	
	public ProxyBranchTimerTask(ProxyBranchImpl proxyBranch)
	{
		this.proxyBranch = proxyBranch;
	}
	
	public void run()
	{
		proxyBranch.onTimeout();
	}
}
