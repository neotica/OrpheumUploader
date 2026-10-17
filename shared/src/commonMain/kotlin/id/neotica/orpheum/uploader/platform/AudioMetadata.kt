package id.neotica.orpheum.uploader.platform

import java.io.File

expect suspend fun readAudioDurationSeconds(file: File): Int?
