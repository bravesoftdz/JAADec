package net.sourceforge.jaad.spi.javasound;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

public class AACFormatConversionProvider extends FormatConversionProvider {
	private static final Encoding[] EMPTY_ENCODINGS = new Encoding[]{};

	@Override
	public Encoding[] getSourceEncodings() {
		return new Encoding[]{AACAudioFileReader.AAC_ENCODING};
	}

	@Override
	public Encoding[] getTargetEncodings() {
		return new Encoding[]{AudioFormat.Encoding.PCM_SIGNED};
	}

	@Override
	public Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
		if(AACAudioFileReader.AAC_ENCODING.equals(sourceFormat.getEncoding())){
			return new Encoding[]{AudioFormat.Encoding.PCM_SIGNED};
		}else{
			return EMPTY_ENCODINGS;
		}
	}

	private static int getPcmFrameSize(AudioFormat audioFormat){
		return (audioFormat.getSampleSizeInBits()==AudioSystem.NOT_SPECIFIED || audioFormat.getChannels()==AudioSystem.NOT_SPECIFIED)?AudioSystem.NOT_SPECIFIED:(audioFormat.getSampleSizeInBits()/8*audioFormat.getChannels());
	}

	@Override
	public AudioFormat[] getTargetFormats(Encoding targetEncoding, AudioFormat sourceFormat) {
		if(Arrays.asList(getTargetEncodings(sourceFormat)).contains(targetEncoding)){
			final Set<AudioFormat> targetFormats = new HashSet<AudioFormat>();
			int frameSize = getPcmFrameSize(sourceFormat);
			AudioFormat targetFormat = new AudioFormat(targetEncoding,sourceFormat.getSampleRate(),sourceFormat.getSampleSizeInBits(),sourceFormat.getChannels(),frameSize,sourceFormat.getFrameRate(),sourceFormat.isBigEndian());
			targetFormats.add(targetFormat);
			targetFormats.addAll(Arrays.asList(AudioSystem.getTargetFormats(targetEncoding, targetFormat)));
			return targetFormats.toArray(new AudioFormat[]{});
		}else{
			return new AudioFormat[]{};
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(Encoding targetEncoding, AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		if(!isConversionSupported(targetEncoding, sourceFormat)){
			throw new IllegalArgumentException("Unsupported conversion: " + sourceFormat.toString() + " to " + targetEncoding.toString());
		}
		int frameSize = getPcmFrameSize(sourceFormat);
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sourceFormat.getSampleRate(),sourceFormat.getSampleSizeInBits(),sourceFormat.getChannels(),frameSize, sourceFormat.getSampleRate(),sourceFormat.isBigEndian());
		try{
			return new MP4AudioInputStream(sourceStream, targetFormat, sourceStream.getFrameLength());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		if(!isConversionSupported(targetFormat, sourceFormat)){
			throw new IllegalArgumentException("Unsupported conversion: " + sourceStream.getFormat().toString() + " to " + targetFormat.toString());
		}
		try{
			int frameSize = getPcmFrameSize(sourceFormat);
			AudioFormat mp4AudioInputStreamAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sourceFormat.getSampleRate(),sourceFormat.getSampleSizeInBits(),sourceFormat.getChannels(),frameSize, sourceFormat.getSampleRate(),sourceFormat.isBigEndian());
			AudioInputStream mp4AudioInputStream = new MP4AudioInputStream(sourceStream, mp4AudioInputStreamAudioFormat, sourceStream.getFrameLength());
			if(targetFormat.matches(mp4AudioInputStreamAudioFormat)){
				return mp4AudioInputStream;
			}else{
				return AudioSystem.getAudioInputStream(targetFormat, mp4AudioInputStream);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
