package ma.chinespirit.dep

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard:
  lazy val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

  def put(text: String): Unit =
    clipboard.setContents(new StringSelection(text), null)

  def setupClipboard(): Unit =
    System.setProperty("testfx.robot", "glass")
    System.setProperty("testfx.headless", "true")
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.text", "t2k")
