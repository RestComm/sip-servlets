package org.mobicents.servlet.sip.example;

import java.net.URI;
import java.net.URLEncoder;

public class PromptAndCollectServletTTS extends PromptAndCollectServlet{
	@Override
	protected URI getPrompt() throws Exception{
		return URI.create("data:" + URLEncoder.encode("ts(Hello this is JSR309 TTS application)", "UTF-8"));
	}
}
