package net.sourceforge.jaad.spi.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AACFormatConversionProviderTest {
	private FormatConversionProvider formatConversionProvider;

	@Before
	public void before(){
		this.formatConversionProvider=new AACFormatConversionProvider();
	}

	@Test
	public void testPcmToPcmIsConversionSupported() throws Exception {
		AudioFormat audioFormat = new AudioFormat(AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true, true);
		Assert.assertFalse(formatConversionProvider.isConversionSupported(Encoding.PCM_SIGNED, audioFormat));
	}

	@Test
	public void testAACtoPCMIsConversionSupported() throws Exception {
		AudioFormat audioFormat = new AudioFormat(new Encoding("AAC"), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true);
		Assert.assertTrue(formatConversionProvider.isConversionSupported(Encoding.PCM_SIGNED, audioFormat));
	}

	@Test
	public void testAAC1PCMDownsampleRateAndBitsIsConversionSupported() throws Exception {
		AudioFormat audioFormat = new AudioFormat(new Encoding("AAC"), 44100, 16, 2, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true);
		Assert.assertTrue(formatConversionProvider.isConversionSupported(new AudioFormat(8000, 8, 2, true, true), audioFormat));
	}

	@Test
	public void testAACtoAACIsConversionSupported() throws Exception {
		AudioFormat audioFormat = new AudioFormat(new Encoding("AAC"), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true);
		Assert.assertFalse(formatConversionProvider.isConversionSupported(new Encoding("AAC"), audioFormat));
	}
}
