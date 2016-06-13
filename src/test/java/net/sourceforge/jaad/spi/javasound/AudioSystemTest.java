package net.sourceforge.jaad.spi.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.SourceDataLine;

import org.junit.Assume;
import org.junit.Test;

public class AudioSystemTest {

	private static boolean canPlaySound(){
		SourceDataLine sdl = null;
		try{
			sdl = AudioSystem.getSourceDataLine(new AudioFormat(44100,16,2,true,true));
			return true;
		}catch(Exception e){
			// This system isn't capable of playing audio, so don't run these tests
			return false;
		}finally{
			if(sdl!=null) {
				sdl.close();
			}
		}
	}
	
	@Test
	public void testClipValidProfileLC() throws Exception {
		Assume.assumeTrue(canPlaySound());
		AudioInputStream ais = AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, AudioSystem.getAudioInputStream(getClass().getResource("valid_profile_lc.mp4")));
        try{
			Clip clip = AudioSystem.getClip();
			try{
		        clip.open(ais);
		        clip.start();
		        clip.drain();
			}finally{
				clip.close();
			}
        }finally{
			ais.close();
        }
	}
}
