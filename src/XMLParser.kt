import java.io.File
import java.io.FileNotFoundException

/**
 * @author SughoshKumar on 02/09/17.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Program needs arguments to execute")
        println("define command: \"\$kotlin XMLParser pathOf<<resource_directory> or <file1>, <file2>, <file3>...>\"")
        return
    }
    val builder = XMLParser.Companion.Builder()

    if (args.size == 1) {
        val file = File(args[0])
        if (file.isDirectory){
            builder.setDirectory(file)
        } else if (file.extension == "xml") {
            builder.addFiles(file)
        } else {
            println("File must of type xml or a resource directory containing the xml files")
            return
        }
    } else {
        builder.addFiles(args)
    }
    builder.setType(XMLParserType.UNITY)
    val parser = builder.build()
    parser.start()
}

abstract class XMLParser protected constructor(protected val listOfFiles: List<File>){
    abstract var xmlFile: File?
    abstract fun processPair(listOfPairs: MutableList<Pair<String, String>>)
    abstract fun writeOutput(builder: StringBuilder)

    fun start(){
        if (listOfFiles.isEmpty()){
            throw RuntimeException("file list is null")
        }
        listOfFiles.filter { it.extension == "xml" }
                .forEach { xmlFile = it }
    }

    companion object{
        class Builder {
            private var listOfFiles = mutableListOf<File>()
            private var type: XMLParserType? = null
            fun setType(type:XMLParserType): Builder {
                this.type = type
                return this
            }

            @Throws (FileNotFoundException::class, IllegalArgumentException::class)
            fun setDirectory(file: File): Builder {
                if (!file.exists()) {
                    throw FileNotFoundException("File not found or does not exists")
                }
                if (!file.isDirectory){
                    throw IllegalArgumentException("Provided file is not a directory")
                }
                (0 until file.listFiles().size)
                        .mapNotNull { file.listFiles()[it] }
                        .filter { it.extension == "xml"  }
                        .forEach {
                            listOfFiles.add(it)
                        }
                return this
            }

            fun addFiles(vararg file: File): Builder{
                listOfFiles = file.toMutableList()
                return this
            }

            fun addFiles(paths: Array<String>): Builder{
                paths.mapNotNull { File(it) }
                        .filter { it.exists() && it.extension == ".xml" }
                        .forEach { listOfFiles.add(it) }
                return this
            }

            @Throws(NullPointerException::class, IllegalArgumentException::class)
            fun build(): XMLParser {
                val type = this.type ?: throw NullPointerException("Type cannot be null")
                if (listOfFiles.isEmpty()){
                    throw IllegalArgumentException("list of files cannot be null, provide one or more files to convert")
                }
                return when (type){
                    XMLParserType.UNITY -> UnityXmlParser(listOfFiles)
                    XMLParserType.IOS -> {
                        // for example sake else here would be
                        // an iOS converter
                        UnityXmlParser(listOfFiles)
                    }
                }
            }

        }
    }
}

enum class XMLParserType{
    UNITY,
    IOS
}

