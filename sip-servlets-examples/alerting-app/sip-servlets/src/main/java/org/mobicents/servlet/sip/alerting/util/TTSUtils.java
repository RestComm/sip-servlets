/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.alerting.util;

import java.io.File;
import java.net.URI;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class TTSUtils {
	
	private static Logger logger = Logger.getLogger(TTSUtils.class);
	
	public static void buildAudio(String text, String filename) throws Exception {
		VoiceManager mgr = VoiceManager.getInstance();
		Voice voice = mgr.getVoice("kevin");
		voice.allocate();
		File speech = new File(new URI(filename));
//		File speech = new File(filename);
//		SingleFileAudioPlayer player = new SingleFileAudioPlayer(getBasename(speech.getAbsolutePath()), getAudioType(filename));
		SingleFileAudioPlayer player = new SingleFileAudioPlayer(speech.getAbsolutePath(), getAudioType(filename));
		voice.setAudioPlayer(player);
		voice.startBatch();
		voice.speak(text);
		voice.endBatch();
		player.close();
		voice.deallocate();
	}
	
	public static String getBasename(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return path;
		} else {
			return path.substring(0, index);
		}
	}
		
	
	public static String getExtension(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return null;
		} else {
			return path.substring(index + 1);
		}
	}
	
	public static AudioFileFormat.Type getAudioType(String file) {
		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		String extension = "wav";

		for (int i = 0; i < types.length; i++) {
			if (types[i].getExtension().equals(extension)) {
				return types[i];
			}
		}
		return null;
	}
}
