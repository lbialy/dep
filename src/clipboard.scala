package ma.chinespirit.dep

// import javafx.scene.input.{Clipboard => JavaFXClipboard}
// import javafx.scene.input.ClipboardContent
// import javafx.application.Platform
// import java.util.concurrent.CountDownLatch
// import java.util.logging.{Logger, Level}

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard:
  // locally {
  //   val rootLogger = Logger.getLogger("")
  //   rootLogger.setLevel(Level.OFF)
  //   Platform.startup(() => {})
  // }

  // lazy val clipboard = JavaFXClipboard.getSystemClipboard()
  lazy val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

  def put(text: String): Unit =
    clipboard.setContents(new StringSelection(text), null)

    // val latch = CountDownLatch(1)
    // only because clipboard.setContent has to be run on the JavaFX thread
    // Platform.runLater(() => {
    //   try
    //     val content = ClipboardContent()
    //     content.putString(text)
    //     clipboard.setContent(content)
    //   finally latch.countDown()
    // })

    // but this is a sync call!
    // latch.await()
