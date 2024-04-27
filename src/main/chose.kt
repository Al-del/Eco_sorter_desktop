@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Durere!") }

    MaterialTheme {
        Button(onClick = {


        }) {
            Text(text)
        }
    }
}

fun main() = application {

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
