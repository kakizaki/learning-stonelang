import java.io.IOException
import java.io.Reader
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea

class CodeDialog : Reader() {
    companion object {
        fun showDialog(): String? {
            val area = JTextArea(20, 40)
            val pane = JScrollPane(area)
            val result = JOptionPane.showOptionDialog(
                null, pane, "input",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, null, null
            )
            if (result == JOptionPane.OK_OPTION) {
                return area.getText()
            }
            return null
        }
    }


    private var buffer = ""

    private var pos = 0

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (buffer.length <= pos) {
            val s = showDialog()
            if (s == null) {
                return -1
            }
            buffer = s + "\n"
            pos = 0
        }

        var size = 0
        while (pos < buffer.length && size < len) {
            cbuf[off + size] = buffer[pos]
            ++pos
            ++size
        }

        return size
    }

    override fun close() {
        throw IOException()
    }
}
