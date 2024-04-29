import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Perform action when "Audio" button is clicked
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