import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author SughoshKumar on 02/09/17.
 */
open class UnityXmlParser(listOfFiles: List<File>): XMLParser (listOfFiles){
    override var xmlFile: File? = null
        set(value) {
            field = value
            try {
                stripFile(value)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }

    @Throws (FileNotFoundException::class)
    private fun stripFile(file: File?) {
        val documentFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentFactory.newDocumentBuilder()
        if (!file.isNotNullAndExists()) throw FileNotFoundException("File not found or does not exists")
        val document = documentBuilder.parse(file)
        val stringTagList = document.getElementsByTagName("string")
        val xmlObject = mutableListOf<Pair<String, String>>()
        (0 until stringTagList.length)
                .mapNotNull { stringTagList.item(it) }
                .forEach {
                    val stringTag = it.attributes.getNamedItem("name")
                    xmlObject.add(Pair(stringTag.nodeValue, it.textContent))
                }
        processPair(xmlObject)
    }

    override fun processPair(listOfPairs: MutableList<Pair<String, String>>) {
        val builder = StringBuilder()
        listOfPairs
                .forEach {
                    val value = processValues(it.second)
                    val name = it.first
                    builder.append("$name = $value\n")
                }
        try {
            writeOutput(builder)
        } catch (e : FileNotFoundException){
            e.printStackTrace()
        } catch (e : IOException){
            e.printStackTrace()
        }
    }

    private fun processValues(value: String): String {
        val pattern = Pattern.compile("%(\\d+\\$)?(s|d)")
        val matcher = pattern.matcher(value)
        var counter = 0
        val stringBuffer = StringBuffer()
        while (matcher.find()) {
            val digitsPattern = Pattern.compile("\\d+")
            val digitsMatcher = digitsPattern.matcher(matcher.group())
            var digits = 0
            if (digitsMatcher.find()) {
                digits = digitsMatcher.group().toInt()
            }

            val replacer =  if (digits == 0) {
                "{$counter}"
            } else {
                //** handles illegal string files too **
                if (stringBuffer.contains("{"+digits.minus(1)+"}") && stringBuffer.contains("{$digits}")){
                    "{"+digits.plus(1)+"}"
                } else if (stringBuffer.contains("{"+digits.minus(1)+"}")){
                    "{$digits}"
                } else{
                    "{" + digits.minus(1) + "}"
                }
            }
            matcher.appendReplacement(stringBuffer, replacer)
            counter++
        }
        matcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }

    @Throws (FileNotFoundException::class, IOException::class)
    override fun writeOutput(builder: StringBuilder) {
        val file = xmlFile ?: throw FileNotFoundException("File not found or does not exists")
        // create output directory
        val outputDir = File(file.parentFile.absolutePath, "outputs")
        if (!outputDir.exists()){
            outputDir.mkdir()
        }
        val outputName = file.name.split(".xml")[0]+".txt"
        val outputFile = File(outputDir, outputName)
        if (outputFile.exists()){
            outputFile.delete()
        }
        outputFile.createNewFile()
        outputFile.printWriter().use { out ->
            out.write(builder.toString())
        }
    }

    fun File?.isNotNullAndExists() : Boolean {
        if (this == null) return false
        return exists()
    }
}