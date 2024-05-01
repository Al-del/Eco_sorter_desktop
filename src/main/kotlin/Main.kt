import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.sound.sampled.*
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import javax.sound.sampled.*
import javax.sound.sampled.*
import java.io.FileOutputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
private class MyApplicationState {
    val windows = mutableStateListOf<MyWindowState>()

    init {
        windows += MyWindowState("Initial window")
    }

    fun openNewWindow() {
        windows += MyWindowState("Window ${windows.size}")
    }

    fun exit() {
        windows.clear()
    }

    private fun MyWindowState(
        title: String
    ) = MyWindowState(
        title,
        openNewWindow = ::openNewWindow,
        exit = ::exit,
        windows::remove
    )
}

class MyWindowState(
    val title: String,
    val openNewWindow: () -> Unit,
    val exit: () -> Unit,
    private val close: (MyWindowState) -> Unit
) {
    var currentPage by mutableStateOf("Page1")

    fun close() = close(this)

    fun navigateTo(page: String) {
        currentPage = page
    }
}

@Composable
private fun ApplicationScope.MyWindow(
    state: MyWindowState
) {
    val audioData = remember { mutableStateOf(byteArrayOf()) }

    Window(onCloseRequest = state::close, title = state.title) {
        when (state.currentPage) {
            "Page1" -> Page1(state)
            "Page2" -> Page2(state)
            "Code" -> Code(state)
            "Audio" -> {
                RecordButton(audioData)
                val floats = audioDataToFloats(audioData.value)
                LineChart(floats)
            }
            else -> Text("Unknown page: ${state.currentPage}")
        }
    }
}
@Composable
fun Code(state: MyWindowState) {
Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
       Text("Code $random_numero")

}
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        //Make a button on the left down corner to go back to the first page
        Button(onClick = {
            state.navigateTo("Page1")
        }) {
            Text("Back")
        }
    }
}
    }

@Composable
fun Page1(state: MyWindowState) {
    val startClicked = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            startClicked.value = true
          //
        }) {
            Text("Start")
        }

        if (startClicked.value) {
            LaunchedEffect(Unit) {
                val Redeem_code = MongoClientConnectionExample
                Redeem_code.push_code("location")
                state.navigateTo("Page2")
            }
        }
    }
}

@Composable
fun Page2(state: MyWindowState) {
    val codeClicked = remember { mutableStateOf(false) }
    val audiCLicked = remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                codeClicked.value = true
            }) {
                Text("Code")
            }

            if (codeClicked.value) {
                LaunchedEffect(Unit) {
                    val Redeem_code = MongoClientConnectionExample
                    Redeem_code.push_code("Redeem_code")
                    state.navigateTo("Code")
                }
            }
            if(audiCLicked.value){
                state.navigateTo("Audio")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                audiCLicked.value = true
            }) {
                Text("Audio")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MyApp()
    }

    }

@Composable
fun MyApp() {
    val audioData = remember { mutableStateOf(byteArrayOf()) }

    Column {
        RecordButton(audioData)
        PlayButton(audioData.value)
        val floats = audioDataToFloats(audioData.value)
        LineChart(floats)
    }
}

@Composable
fun RecordButton(audioData: MutableState<ByteArray>) {
    val isPressed = remember { mutableStateOf(false) }
    val format = AudioFormat(8000.0f, 16, 1, true, true)
    val info = DataLine.Info(TargetDataLine::class.java, format)
    val line = AudioSystem.getLine(info) as TargetDataLine
    val out = ByteArrayOutputStream()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed.value = true
                        line.open(format)
                        line.start()
                        val buf = ByteArray(line.bufferSize)
                        thread(start = true) {
                            while (isPressed.value) {
                                val count = line.read(buf, 0, buf.size)
                                if (count > 0) {
                                    out.write(buf, 0, count)
                                }
                            }
                            out.close()
                            line.stop()
                            line.close()
                            audioData.value = out.toByteArray()

                            // Calculate and print the length of the recording
                            val lengthInSeconds = audioData.value.size / (format.sampleRate * format.channels * (format.sampleSizeInBits / 8.0)).toDouble()
                            println("Length of the recording: $lengthInSeconds seconds")

                            // Save the recording to a file
                            val audioInputStream = AudioInputStream(ByteArrayInputStream(audioData.value), format, audioData.value.size.toLong())
                            val file = File("recording.wav")
                            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file)
                            println("Recording saved to ${file.absolutePath}")
                        }
                        tryAwaitRelease()
                        isPressed.value = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(if (isPressed.value) "Recording..." else "Hold to Record")
    }
}

@Composable
fun PlayButton(audioData: ByteArray) {
    val isPressed = remember { mutableStateOf(false) }
    val format = AudioFormat(8000.0f, 16, 1, true, true)
    val info = DataLine.Info(SourceDataLine::class.java, format)
    val line = AudioSystem.getLine(info) as SourceDataLine

    Button(onClick = {
        isPressed.value = true
        line.open(format)
        line.start()
        thread(start = true) {
            line.write(audioData, 0, audioData.size)
            line.drain()
            line.stop()
            line.close()
            isPressed.value = false
        }
    }) {
        Text(if (isPressed.value) "Playing..." else "Play")
    }
}
fun audioDataToFloats(audioData: ByteArray): List<Float> {
    val floats = mutableListOf<Float>()
    for (i in audioData.indices step 2) {
        val value = (audioData[i].toInt() shl 8) or (audioData[i + 1].toInt() and 0xFF)
        floats.add(value.toFloat() / Short.MAX_VALUE.toFloat())
    }
    return floats
}

@Composable
fun LineChart(data: List<Float>) {
    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue
    println("MOR")
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (i in 1 until data.size) {
            val x1 = (i - 1) * canvasWidth / (data.size - 1)
            val y1 = canvasHeight - (data[i - 1] - minValue) / range * canvasHeight
            val x2 = i * canvasWidth / (data.size - 1)
            val y2 = canvasHeight - (data[i] - minValue) / range * canvasHeight

            drawLine(
                color = Color.Blue,
                start = Offset(x1, y1),
                end = Offset(x2, y2)
            )
        }
    }
}