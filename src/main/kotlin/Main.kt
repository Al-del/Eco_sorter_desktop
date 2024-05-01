import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import javax.sound.sampled.*
import java.io.ByteArrayOutputStream
import javax.sound.sampled.*
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
) = Window(onCloseRequest = state::close, title = state.title) {
    when (state.currentPage) {
        "Page1" -> Page1(state)
        "Page2" -> Page2(state)
        "Code" -> Code(state)
        "Audio" -> RecordButton()
        else -> Text("Unknown page: ${state.currentPage}")
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
    val applicationState = remember { MyApplicationState() }

    for (window in applicationState.windows) {
        key(window) {
            MyWindow(window)
        }

    }
}
@Composable
fun RecordButton() {
    val isPressed = remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed.value = true
                        recorder.startRecording()
                        tryAwaitRelease()
                        isPressed.value = false
                        recorder.stopRecording()
                        recorder.playRecording()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(if (isPressed.value) "Recording..." else "Hold to Record")
    }
}




class AudioRecorder {
    private var targetDataLine: TargetDataLine? = null
    private var audioBytes: ByteArray? = null

    fun startRecording() {
        val audioFormat = AudioFormat(16000.0f, 16, 2, true, true)
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
        targetDataLine = AudioSystem.getLine(info) as TargetDataLine
        targetDataLine?.open(audioFormat)
        targetDataLine?.start()

        // Start a new thread to read the audio data from the TargetDataLine
        Thread {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (targetDataLine?.isRunning == true) {
                bytesRead = targetDataLine?.read(buffer, 0, buffer.size) ?: 0
                out.write(buffer, 0, bytesRead)
            }
            audioBytes = out.toByteArray()
        }.start()
    }

    fun stopRecording() {
        targetDataLine?.stop()
        targetDataLine?.close()
    }

    fun playRecording() {
        val audioFormat = AudioFormat(16000.0f, 16, 2, true, true)
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        val sourceDataLine = AudioSystem.getLine(info) as SourceDataLine
        sourceDataLine.open(audioFormat)
        sourceDataLine.start()

        // Write the audio data to the SourceDataLine
        audioBytes?.let { sourceDataLine.write(it, 0, it.size) }

        sourceDataLine.drain()
        sourceDataLine.close()
    }
}