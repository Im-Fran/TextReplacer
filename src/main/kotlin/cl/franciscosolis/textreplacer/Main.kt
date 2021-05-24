package cl.franciscosolis.textreplacer

import sun.security.action.GetPropertyAction
import java.io.*
import java.nio.charset.Charset
import java.security.AccessController

fun main(args: Array<String>) {
    if(args.size <= 2) {
        println("In order to use the program use the following command:")
        println("java -jar TextReplacer.jar \"string to search\" \"string to replace with\" \"directory/file to search\"")
    }else{
        Main(args)
    }
}

class Main(args: Array<String>){

    private val files = mutableListOf<File>()
    private val replace: String = args[0]
    private val replacement: String = args[1]

    init {
        println("Loading '$replace' as value to search for and '$replacement' to replace it with")
        val root = File(args[2])
        if(!root.exists())
            throw RuntimeException("The file/folder ${root.path} doesn't exist!")
        println("Scanning for files..")
        this.scanFiles(root)
        println("Searching & Replacing through ${files.size} file(s)...")
        files.forEach {
            Thread {
                this.scanAndReplace(it)
            }.start()
        }
    }

    private fun scanAndReplace(file: File){
        val inputStream = BufferedReader(FileReader(file))
        val inputBuffer = StringBuffer()
        val lineSeparator = AccessController.doPrivileged(GetPropertyAction("line.separator"))
        inputStream.forEachLine { line ->
            inputBuffer.append(line)
            inputBuffer.append(lineSeparator)
        }
        inputStream.close()

        val outputStream = FileOutputStream(file)
        outputStream.write(inputBuffer.toString().replace(this.replace, this.replacement).toByteArray(Charset.defaultCharset()))
        outputStream.close()
    }

    private fun scanFiles(dir: File){
        if(dir.isFile){
            this.files.add(dir)
        }else{
            val files = dir.listFiles()
            if(files != null && files.isNotEmpty()){
                files.filter { it != null && it.exists() && it.name != "TextReplacer.jar" }.forEach {
                    if(it.isDirectory) {
                        this.scanFiles(it)
                    }else{
                        this.files.add(it)
                    }
                }
            }
        }
    }
}
