# SoundCheckServer

This is the server side component of my Android App SoundCheck. See https://github.com/ctarabusi/SoundCheckAndroid

It is a composed by four servlets easily deployable in any servlet containers.

- Recording: it allows to store the provided byte stream as a Wav file on WEB-INF folder called Recording.wav
- FFT: returns the double array containing the magnitude of the frequency spectrum of the Recording.wav
- Spectrogram: returns a double matrix containing the magnitude per frequency/time of the Recording.wav
- CheckSound: It returns an integer indicating the number of consecutives spectrogram matches between the provided byte stream and Recording.wav