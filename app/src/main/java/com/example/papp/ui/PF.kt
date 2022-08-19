package com.example.papp.ui

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.DataForSendToPrinterTSC
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import com.example.papp.ui.dialog.WarningPopup
import com.example.papp.BuildConfig
import com.example.papp.MakeLabel
import com.example.papp.PSApplication
import com.example.papp.R
import ru.atol.xmlparser.XmlParser
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.NoSuchElementException


class PF :Fragment(),View.OnClickListener {

    private lateinit var  barcodeTypeSpinner: Spinner
    private val printerSettings by lazy { PSApplication.settings }
    lateinit var eanText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_printer_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: Button = view.findViewById(R.id.print_button)
        barcodeTypeSpinner = view.findViewById(R.id.barcode_type_spinner)
        val templateButton: Button = view.findViewById(R.id.template_button)
        val templateXMLButton: Button = view.findViewById(R.id.button_templates_xml)
        eanText = view.findViewById(R.id.editTextTextPersonName2)
        button.setOnClickListener(this)
        templateButton.setOnClickListener(this)
        templateXMLButton.setOnClickListener(this)
        barcodeTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        Timber.d("Выбран тип штрихкода 128")
                        eanText.inputType = InputType.TYPE_CLASS_TEXT
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(200))
                    }
                    1 -> {
                        Timber.d("Выбран тип штрихкода 93")
                        eanText.inputType = InputType.TYPE_CLASS_TEXT
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(200))
                    }
                    2-> {
                        Timber.d("Выбран тип штрихкода EAN8")
                        eanText.inputType = InputType.TYPE_CLASS_NUMBER
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(7))
                    }
                    3-> {
                        Timber.d("Выбран тип штрихкода EAN13")
                        eanText.inputType = InputType.TYPE_CLASS_NUMBER
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(12))
                    }
                    4-> {
                        Timber.d("Выбран тип штрихкода EAN14")
                        eanText.inputType = InputType.TYPE_CLASS_NUMBER
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(13))
                    }
                    5-> {
                        Timber.d("Выбран тип штрихкода ITF14")
                        eanText.inputType = InputType.TYPE_CLASS_NUMBER
                        eanText.text.clear()
                        eanText.filters = arrayOf(LengthFilter(13))
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.print_button -> {
                Timber.d("Нажато: 'Печать'")
                print()
            }
            R.id.template_button -> {
                Timber.d("Нажато: 'Печать шаблона'")
                printTemplate()
            }
            R.id.button_templates_xml -> {
                Timber.d("Нажато: 'Печать XML'")
                printXMLTemplate()
            }
        }
    }

    private fun print() {
        val wide:Int = if (barcodeTypeSpinner.selectedItem.toString() === "ITF14") 20 else 2
        PSApplication.binder.writeDataByYouself(object : UiExecute {
            override fun onsucess() {

            }
            override fun onfailed() {

            }
        }, ProcessData {
            val list = ArrayList<ByteArray>()
            DataForSendToPrinterTSC.setCharsetName("866")
            //default is gbk,if you don't set the charset
            //first you have to set the width and heigt ,
            // you can also use dot or inch as a unit method, specific conversion reference programming manual
            // размер печатного листа ( лучше оставить такие значения)
            list.add(DataForSendToPrinterTSC.sizeBymm(58.0, 30.0))
            //set the gap
            //зазор ( расстояние между этикетками)
            list.add(DataForSendToPrinterTSC.gapBymm(0.0, 0.0))
            //clear cach
            list.add(DataForSendToPrinterTSC.cls())
            //print barcode
            //штрихкод
            list.add(
                DataForSendToPrinterTSC.barCode(
                    32,
                    9,
                    barcodeTypeSpinner.selectedItem.toString(),
                    100,
                    2,
                    0,
                    2,
                    wide,
                    eanText.text.toString()
                )
            )
            //list.add(DataForSendToPrinterTSC.qrCode(10,10,"H",4,"M",0,"1124124214"))
            //list.add(DataForSendToPrinterTSC.text(0,0,"ROBOTOSR.TTF",0,10,10,"йцукенгшщзхъфывапролджэячсмитьбю"))
            //list.add(DataForSendToPrinterTSC.text((28*8)-(4*8) - (2*10),56,"ROBOTO-B.TTF",0,8,8,"Nike"))
            //list.add(DataForSendToPrinterTSC.text((28*8)-(4*8)-(6*12),90,"ROBOTOSR.TTF",0,10,10,"Air Jordan 1")) //право
            //list.add(DataForSendToPrinterTSC.bar((34*8)-(2*8)-(4*12)-(2*8),114,(4*12)+(4*8),3))
            //list.add(DataForSendToPrinterTSC.text(20,58,"ROBOTOSR.TTF",0,10,10,"привет"))
            //list.add(DataForSendToPrinterTSC.block(10,10,200,200,"0",0,8,8,"dfsgvsdgfafog;lrfjhhbvglrfkjasbv,g.lrkabglkarghjarfg.jrhalogu"))
            //print количество напечатанных баркодов
            list.add(DataForSendToPrinterTSC.print(1))
            return@ProcessData list
        })
    }

    private fun printTemplate() {
        PSApplication.binder.writeDataByYouself(object : UiExecute {
            override fun onsucess() {

            }
            override fun onfailed() {

            }
        }, ProcessData {
            val list = ArrayList<ByteArray>()
            val temple = MakeLabel.printTemplate()
            list.add(temple)
            return@ProcessData list
        })
    }

    private fun printXMLTemplate() {
        if (printerSettings.template == "") {
            Timber.d("Не выбран шаблон для печати!")
            showSnackbar("Не выбран шаблон для печати!")
        } else {
            if (File(printerSettings.templatePath + printerSettings.template).exists()) {
                PSApplication.binder.writeDataByYouself(object : UiExecute {
                    override fun onsucess() {

                    }

                    override fun onfailed() {

                    }
                }, ProcessData {
                    val list = ArrayList<ByteArray>()
                    val arrayValues = arrayListOf(
                        "24999",
                        "Air Jordan 1",
                        "хмммм",
                        "пара",
                        "127468126747",
                        "Nike",
                        "20.05.2022",
                        "25999"
                    )
                    val map = mapOf(
                        "Цена" to "29999", "Организация" to "Air Jordan 1",
                        "Номенклатура" to "Печенье Шлёпа",
                        "ЕдиницаИзмерения" to "пачка",
                        "ДатаЦены" to "20.05.2022",
                        "Штрихкод" to "127468126747")
                    if (BuildConfig.DEBUG){
                        parseAndPrint(printerSettings.templatePath,
                            printerSettings.template,
                            map,
                            list)
                    } else {
                        XmlParser.parseAndPrint(
                            printerSettings.templatePath,
                            printerSettings.template,
                            arrayValues,
                            list
                        )
                    }
                    //parseAndPrint(printerSettings.printerTemplatePath,printerSettings.printerTemplate,arrayValues,list)
                    list.add(DataForSendToPrinterTSC.print(1))
                    return@ProcessData list
                })
            } else {
                Timber.d("Выбранный шаблон отсутствует в папке с шаблонами!")
                showSnackbar("Выбранный шаблон отсутствует в папке с шаблонами!")
            }
        }
    }

    fun parseAndPrint(path:String, template:String, map: Map<String,String>, list:ArrayList<ByteArray>) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        val file = File(path + template)
        val fis = checkForUtf8BOMAndDiscardIfAny(FileInputStream(file))
        parser.setInput(InputStreamReader(fis))
        DataForSendToPrinterTSC.setCharsetName("866")
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name.equals("Formatting")) {
                list.add(
                    DataForSendToPrinterTSC.sizeBymm(
                        parser.getAttributeValue(null, "Width").toDouble(),
                        parser.getAttributeValue(null, "Height").toDouble()
                    )
                )
                list.add(DataForSendToPrinterTSC.gapBymm(0.0, 0.0))
                list.add(DataForSendToPrinterTSC.cls())
            }
            if (parser.eventType == XmlPullParser.START_TAG && parser.name.equals("Barcode")) {
                val content = substituteValues(parser.getAttributeValue(null, "Value").toString(),map)
                val widthBarcode = widthBarcode(parser.getAttributeValue(null, "Width").toInt())
                val orientation:Int = try {
                    orientation(parser.getAttributeValue(null, "Orientation").toInt())
                } catch (e:Exception){
                    0
                }
                val type:String = typeBarcodes(parser.getAttributeValue(null, "Type"))
                when (parser.getAttributeValue(null, "Type")) {
                    "EAN13", "EAN8", "Code128", "EAN128", "Code39", "Code93", "ITF14", "EAN13Addon2", "EAN13Addon5" ->
                        list.add(
                            DataForSendToPrinterTSC.barCode(
                                parser.getAttributeValue(null, "Left").toInt() * 8,
                                parser.getAttributeValue(null, "Top").toInt() * 8,
                                type,
                                parser.getAttributeValue(null, "Height").toInt() * 5,
                                2,
                                orientation,
                                widthBarcode,
                                widthBarcode,
                                content
                            )
                        )
                    "DataMatrix" -> list.add(DataForSendToPrinterTSC.dmatrix(
                        parser.getAttributeValue(null, "Left").toInt() * 8,
                        parser.getAttributeValue(null, "Top").toInt() * 8,
                        parser.getAttributeValue(null, "Height").toInt() * 8,
                        parser.getAttributeValue(null, "Width").toInt() * 8,
                        "X8",
                        ""
                    )
                    )
                    "QRCode" -> list.add(DataForSendToPrinterTSC.qrCode(
                        parser.getAttributeValue(null, "Left").toInt() * 8,
                        parser.getAttributeValue(null, "Top").toInt() * 8,
                        "H",
                        4,
                        "M",
                        orientation,
                        content
                    ))
                }
            }
            if (parser.eventType == XmlPullParser.START_TAG && parser.name.equals("Text")) {
                val content:String = if (parser.getAttributeValue(null, "TypeFill") == "Parameter") {
                    substituteValues(parser.getAttributeValue(null, "Value").toString(), map)
                } else {
                    parser.getAttributeValue(null, "Value").toString()
                }
                val alignment = when (parser.getAttributeValue(null, "Align")) {
                    "Center" -> ((parser.getAttributeValue(null, "Width").toInt() / 2) * 8) - (4 * 8) -
                            ((content.length / 2) *
                                    (parser.getAttributeValue(null, "FontSize").toInt() + 2))
                    "Right" -> ((parser.getAttributeValue(null, "Width").toInt()
                            + parser.getAttributeValue(null, "Left").toInt()-1) * 8) - (4 * 8) -
                            content.length * (parser.getAttributeValue(null, "FontSize").toInt() + content.length)
                    else -> parser.getAttributeValue(null, "Left").toInt() * 8
                }
                val font = when (parser.getAttributeValue(null, "FontStyle")) {
                    "Bold" -> "ROBOTO-B.TTF"
                    "Bold Underline" -> "ROBOTO-B.TTF"
                    else -> "ROBOTOSR.TTF"
                }
                when (parser.getAttributeValue(null, "FontStyle")){
                    "Bold Underline", "Underline" -> list.add(DataForSendToPrinterTSC.bar(
                        alignment,
                        ((parser.getAttributeValue(null, "Top").toInt() +
                                (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8) +
                                (parser.getAttributeValue(null, "FontSize").toInt() * 3),
                        content.length * parser.getAttributeValue(null, "FontSize").toInt() + content.length * 2 ,
                        3))
                    "Strikeout", "Bold Strikeout" -> list.add(DataForSendToPrinterTSC.bar(
                        alignment,
                        ((parser.getAttributeValue(null, "Top").toInt() +
                                (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8) +
                                parser.getAttributeValue(null, "FontSize").toInt(),
                        content.length * parser.getAttributeValue(null, "FontSize").toInt() + content.length * 2,
                        3))
                }
                val orientation:Int = try {
                    orientation(parser.getAttributeValue(null, "Orientation").toInt())
                } catch (e:Exception){
                    0
                }
                if (parser.getAttributeValue(null, "TypeFill") == "Parameter") {
                    if (content != "") {
                        list.add(
                            DataForSendToPrinterTSC.text(
                                alignment ,
                                (parser.getAttributeValue(null, "Top").toInt() +
                                        (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8,
                                font,
                                orientation,
                                parser.getAttributeValue(null, "FontSize").toInt(),
                                parser.getAttributeValue(null, "FontSize").toInt(),
                                content
                            )
                        )
                    }
                } else {
                    when (parser.getAttributeValue(null, "FontStyle")){
                        "Bold Underline", "Underline" -> list.add(DataForSendToPrinterTSC.bar(
                            alignment,
                            ((parser.getAttributeValue(null, "Top").toInt() +
                                    (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8) +
                                    (parser.getAttributeValue(null, "FontSize").toInt() * 3),
                            (parser.getAttributeValue(null, "Value").length * parser.getAttributeValue(null, "FontSize").toInt() + parser.getAttributeValue(null, "Value").length * 4) ,
                            3))
                        "Strikeout", "Bold Strikeout" -> list.add(DataForSendToPrinterTSC.bar(
                            alignment,
                            ((parser.getAttributeValue(null, "Top").toInt() +
                                    (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8) +
                                    parser.getAttributeValue(null, "FontSize").toInt(),
                            (parser.getAttributeValue(null, "Value").length * parser.getAttributeValue(null, "FontSize").toInt() + parser.getAttributeValue(null, "Value").length * 4),
                            3))
                    }
                    list.add(
                        DataForSendToPrinterTSC.text(
                            alignment,
                            (parser.getAttributeValue(null, "Top").toInt() +
                                    (parser.getAttributeValue(null, "Height").toInt()) / 4) * 8,
                            font,
                            orientation,
                            parser.getAttributeValue(null, "FontSize").toInt(),
                            parser.getAttributeValue(null, "FontSize").toInt(),
                            parser.getAttributeValue(null, "Value").toString()
                        )
                    )
                }
                // val name = parser.getAttributeName(8)
                //list.add(parser.getAttributeValue(0) + " " + parser.getAttributeValue(1) + "\n" + parser.getAttributeValue(2));
            }
            parser.next();
        }
    }

    @Throws(IOException::class)
    private fun checkForUtf8BOMAndDiscardIfAny(inputStream: InputStream): InputStream? {
        val pushbackInputStream = PushbackInputStream(BufferedInputStream(inputStream), 3)
        val bom = ByteArray(3)
        if (pushbackInputStream.read(bom) !== -1) {
            if (!(bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte())) {
                pushbackInputStream.unread(bom)
            }
        }
        return pushbackInputStream
    }

    private fun substituteValues(xmlParametr:String ,values: Map<String,String>): String {
        return when (xmlParametr) {
           xmlParametr -> try {
               values.getValue(xmlParametr)
           }catch (e:NoSuchElementException){
               ""
           }
            else -> ""
        }
    }

    private fun widthBarcode(xmlParametr:Int): Int {
        return when (xmlParametr) {
            in  1..20 -> 1
            in  21..35 -> 2
            in  36..45 -> 3
            in  46..58 -> 4
            in  59..78 -> 5
            else -> 2
        }
    }

    private fun orientation(xmlParametr:Int): Int {
        return when (xmlParametr) {
            90 -> 90
            180 -> 180
            270 -> 270
            else -> 0
        }
    }

    private fun typeBarcodes(xmlParametr:String): String {
        return when (xmlParametr) {
            "Code128" -> "128"
            "Code39" -> "39"
            "Code93" -> "93"
            "EAN13Addon2" -> "EAN13+2"
            "EAN13Addon5" -> "EAN13+5"
            else -> xmlParametr
        }
    }

    private fun showSnackbar(showstring: String) {
        //Snackbar.make(container, showstring, Snackbar.LENGTH_LONG).show()
        val w = WarningPopup(requireContext())
        w.text = showstring
        w.show()
    }
}
