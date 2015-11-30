# SoundCheckServer

Server side component composed by multiple servlets.

- Recording: it allows to store the provided byte stream as a Wav file on WEB-INF folder called Recording.wav
- FFT: returns the double array containing the magnitude of the frequency spectrum of the Recording.wav
- Spectrogram: returns a double matrix containing the magnitude per frequency/time of the Recording.wav
- CheckSound: still work in progress. The idea is to compare the Recording.wav with the input coming on the servlet
