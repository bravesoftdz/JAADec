package net.sourceforge.jaad.spi.javasound;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AACAudioFileReaderTest {
	
	private AudioFileReader audioFileReader;

	@Before
	public void before(){
		audioFileReader = new AACAudioFileReader();
	}
	
	/** JAAD cannot currently handle this AAC HE profile MP4 file (it's valid... this is a problem with JAAD that should be fixed)
	 */
	@Test(expected=UnsupportedAudioFileException.class)
	public void testValidProfileHE() throws Exception {
		audioFileReader.getAudioFileFormat(getClass().getResource("valid_profile_he.mp4"));
	}
	
	@Test
	public void testValidProfileLC() throws Exception {
		AudioFileFormat aff = audioFileReader.getAudioFileFormat(getClass().getResource("valid_profile_lc.mp4"));
		Assert.assertEquals("MP4", aff.getType().toString());
		Assert.assertTrue(aff.getFormat().getSampleSizeInBits()>0);
	}

	@Test(expected=UnsupportedAudioFileException.class)
	public void testWav() throws Exception {
		audioFileReader.getAudioFileFormat(getClass().getResource("test.wav"));
	}

	@Test(expected=UnsupportedAudioFileException.class)
	public void testOgg() throws Exception {
		audioFileReader.getAudioFileFormat(getClass().getResource("test.ogg"));
	}

	@Test(expected=UnsupportedAudioFileException.class)
	public void testAu() throws Exception {
		audioFileReader.getAudioFileFormat(getClass().getResource("test.au"));
	}

	@Test(expected=UnsupportedAudioFileException.class)
	public void testAiff() throws Exception {
		audioFileReader.getAudioFileFormat(getClass().getResource("test.aiff"));
	}

}
