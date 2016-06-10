package net.sourceforge.jaad.spi.javasound;

import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

/**
 * This class is just to have a public constructor taking the
 * number of bytes of the whole file. The public constructor of
 * AudioFileFormat doesn't take this parameter, the one who takes
 * it is protected.
 */
class JaadAudioFileFormat extends AudioFileFormat {

	JaadAudioFileFormat(Type type, AudioFormat format, int frameLength, Map<String, Object> properties) {
		super(type, format, frameLength, properties);
	}

	JaadAudioFileFormat(Type type, AudioFormat format, int frameLength) {
		super(type, format, frameLength);
	}

	JaadAudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
		super(type, byteLength, format, frameLength);
	}

}
