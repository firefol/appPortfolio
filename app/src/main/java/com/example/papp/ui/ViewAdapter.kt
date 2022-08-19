package com.example.papp.ui

import android.content.Context
import android.os.Build
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.papp.R
import com.example.papp.ui.dialog.InputDialog2
import com.example.papp.ui.dialog.EnumDialog
import java.lang.Exception
import java.lang.NumberFormatException
import kotlin.reflect.KMutableProperty0

class ViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Item>()

    operator fun plusAssign(item: Item) {
        items += item
        notifyItemInserted(items.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == -1)
            throw IllegalStateException("viewType not found")

        val inflater = LayoutInflater.from(parent.context)
        return object : RecyclerView.ViewHolder(inflater.inflate(viewType, parent, false)) {}
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.adapter_view_item_button -> {
                val dataItem = (items[position] as DataItem)

                holder.itemView.findViewById<TextView>(R.id.nameView).apply {
                    text = makeTitle(holder.itemView.context, dataItem)
                    if (!dataItem.isReadOnly) {
                        setOnClickListener { (dataItem.data as ButtonData).callback.invoke() }
                    } else {
                        setOnClickListener {
                            // do nothing
                        }
                    }
                }
            }
            R.layout.adapter_view_item_info -> {
                val context = holder.itemView.context
                val titleView =
                    holder.itemView.findViewById<TextView>(R.id.infoItemTitleView)
                val textView = holder.itemView.findViewById<TextView>(R.id.infoItemTextView)

                val diagnosticsItem = (items[position] as DiagnosticsItem)
                titleView.text = diagnosticsItem.name
                textView.text = diagnosticsItem.text

                fun setDrawable(attr: Int) {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(attr, typedValue, true)

                    titleView.setCompoundDrawablesWithIntrinsicBounds(
                        if (diagnosticsItem.icon != 0) context.getDrawable(diagnosticsItem.icon) else null,
                        null,
                        context.getDrawable(typedValue.resourceId),
                        null
                    )
                }

                when (diagnosticsItem.state) {
                    DiagnosticsItem.State.Success -> setDrawable(R.attr.diagnosticsSuccessDrawable)
                    DiagnosticsItem.State.Warning -> setDrawable(R.attr.diagnosticsWarningDrawable)
                    DiagnosticsItem.State.Failure -> setDrawable(R.attr.diagnosticsFailureDrawable)
                }
            }
            R.layout.adapter_view_item_enum -> {
                val dataItem = (items[position] as DataItem)
                val enumData = dataItem.data as EnumEditData

                val name = makeTitle(holder.itemView.context, dataItem)

                holder.itemView.findViewById<TextView>(R.id.nameView).apply {
                    text = name
                }

                val i = enumData.value

                holder.itemView.findViewById<TextView>(R.id.valueView).text =
                    enumData.variants.getOrNull(i) ?: "Не выбрано"

                if (!dataItem.isReadOnly) {
                    holder.itemView.findViewById<View>(R.id.enumLayout).setOnClickListener {
                        val dialog = EnumDialog(
                            context = holder.itemView.context,
                            title = name,
                            variants = enumData.variants,
                            value = enumData.value)

                        dialog.setOnSelectListener { value ->
                            editItemById(dataItem.id) {
                                enumData.value = value
                            }
                        }

                        dialog.show()
                    }
                } else {
                    holder.itemView.findViewById<View>(R.id.enumLayout).setOnClickListener {
                        // do nothing
                    }
                }
            }
            R.layout.adapter_view_item_seek -> {
                val dataItem = (items[position] as DataItem)

                val seekData = dataItem.data as SeekEditData

                holder.itemView.findViewById<TextView>(R.id.nameView).apply {
                    text = makeTitle(holder.itemView.context, dataItem)
                }

                holder.itemView.findViewById<TextView>(R.id.descriptionView).text =
                    seekData.description

                val valueView = holder.itemView.findViewById<TextView>(R.id.valueView)
                valueView.text = seekData.value.toString()

                holder.itemView.findViewById<SeekBar>(R.id.seekBarView).apply {
                    max = seekData.range.last - seekData.range.first
                    progress = seekData.value - seekData.range.first

                    keyProgressIncrement = 1
                    isEnabled = !dataItem.isReadOnly

                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                seekData.value = progress + seekData.range.first
                                valueView.text = seekData.value.toString()
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    })
                }
            }
            R.layout.adapter_view_item_text,  R.layout.adapter_view_item_text_horizontal -> {
                val dataItem = (items[position] as DataItem)
                val textData = dataItem.data as TextEditData

                val name = makeTitle(holder.itemView.context, dataItem)

                holder.itemView.findViewById<TextView>(R.id.nameView).apply {
                    text = name
                }

                val valueView = holder.itemView.findViewById<TextView>(R.id.valueView)

                valueView.text = textData.value ?: "Не выбрано"

                if (!dataItem.isReadOnly) {
                    holder.itemView.findViewById<View>(R.id.textLayout).setOnClickListener {
                        val dialog = InputDialog2(
                            context = holder.itemView.context,
                            title = name,
                            text = textData.value ?: "")

                        dialog.setInputType(textData.inputType)

                        textData.canEditFunc?.let {
                            dialog.setCanEditFunc(it)
                        }

                        dialog.setOnInputListener { value ->
                            editItemById(dataItem.id) { textData.value = value }
                        }

                        dialog.show()
                    }
                } else {
                    holder.itemView.findViewById<View>(R.id.textLayout).setOnClickListener {
                        // do nothing
                    }
                }
            }
            R.layout.adapter_view_item_fraction -> {
                val firstTextView = holder.itemView.findViewById<TextView>(R.id.firstTextView)
                val secondTextView = holder.itemView.findViewById<TextView>(R.id.secondTextView)
                val firstView = holder.itemView.findViewById<View>(R.id.firstView)
                val secondView = holder.itemView.findViewById<View>(R.id.secondView)

                val fractionItem = (items[position] as FractionItem)

                firstTextView.text = fractionItem.firstText
                secondTextView.text = fractionItem.secondText

                firstView.layoutParams = LinearLayout.LayoutParams(0, 8).apply { this.weight = fractionItem.fractionPercent / 100.0F }
                secondView.layoutParams = LinearLayout.LayoutParams(0, 8).apply { this.weight = (100.0F - fractionItem.fractionPercent) / 100.0F }
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is GroupItem -> R.layout.adapter_view_item_group
            items[position] is DiagnosticsItem -> R.layout.adapter_view_item_info
            items[position] is FractionItem -> R.layout.adapter_view_item_fraction
            items[position] is DataItem -> with((items[position] as DataItem).data) {
                when (this) {
                    is ButtonData -> R.layout.adapter_view_item_button
                    is TextEditData -> if ((this as? TextEditData)?.isHorizontal == true) {
                        R.layout.adapter_view_item_text_horizontal
                    } else {
                        R.layout.adapter_view_item_text
                    }
                    is SeekEditData -> R.layout.adapter_view_item_seek
                    is EnumEditData -> R.layout.adapter_view_item_enum
                    else -> -1
                }
            }
            else -> -1
        }
    }

    override fun getItemCount(): Int = items.size

    fun editItemById(id: Int, function: (item: Item) -> Unit) {
        val i = items.indexOfFirst {
            (it is DataItem && it.id == id) || (it is DiagnosticsItem && it.id == id) || (it is FractionItem && it.id == id)
        }

        if (i != -1) {
            function(items[i])
            notifyItemChanged(i)
        }
    }

    fun getItemById(id: Int): Item? {
        return items.find { (it is DataItem && it.id == id) || (it is DiagnosticsItem && it.id == id) }
    }

    fun updateStringValue(id: Int, value: String?) {
        ((getItemById(id) as? DataItem)?.data as? TextEditData)?.value = value
    }

    fun updateIntValue(id: Int, value: Int?) {
        ((getItemById(id) as? DataItem)?.data as? TextEditData)?.value = value?.toString()
    }

    fun updateBooleanValue(id: Int, value: Boolean) {
        ((getItemById(id) as? DataItem)?.data as? BoolEditData)?.value = value
    }

    fun updateEnumValue(id: Int, value: Int, variants: Array<String>? = null) {
        val data = ((getItemById(id) as? DataItem)?.data as? EnumEditData)!!
        data.value = value

        variants?.let {
            data.variants = it
        }
    }

    fun getStringValue(id: Int): String? {
        return ((getItemById(id) as? DataItem)?.data as? TextEditData)?.value
    }

    fun getIntValue(id: Int): Int? {
        return try {
            Integer.parseInt(((getItemById(id) as? DataItem)?.data as? TextEditData)?.value ?: "")
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun getBooleanValue(id: Int): Boolean {
        return ((getItemById(id) as? DataItem)?.data as? BoolEditData)?.value ?: false
    }

    fun getEnumValue(id: Int): Int {
        return ((getItemById(id) as? DataItem)?.data as? EnumEditData)?.value ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun makeTitle(context: Context, dataItem: DataItem, addText: String = ""): Spanned {
        val text = dataItem.name + addText
        val textAsterisk = SpannableString("$text *")
        val span = ForegroundColorSpan(context.getColor(R.color.colorSecondary))
        textAsterisk.setSpan(span,
            text.length + 1,
            text.length + 2,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        return if (dataItem.base) textAsterisk else SpannableString(text)
    }


    interface Item

    interface Data

    data class GroupItem(
        var name: String,
        var topMargin: Boolean
    ) : Item

    data class DiagnosticsItem(
        var id: Int,
        var name: String,
        var text: String = "",
        var icon: Int = 0,
        var state: State = State.Failure
    ) : Item {
        enum class State {
            Success,
            Warning,
            NearFailure,
            Failure
        }
    }

    data class DataItem(
        var id: Int,
        var name: String,
        var data: Data,
        var base: Boolean = false,
        var isReadOnly: Boolean = false
    ) : Item

    data class FractionItem(
        var id: Int,
        var fractionPercent: Float = 0.0F,
        var firstText: String = "",
        var secondText: String = ""
    ) : Item

    data class ButtonData(
        var callback: () -> Unit
    ) : Data

    data class TextEditData(
        var value: String?,
        var canEditFunc: (String, String) -> Boolean = { _, _ -> true },
        var inputType: Int = InputType.TYPE_CLASS_TEXT,
        var isHorizontal: Boolean = false
    ) : Data


    data class SeekEditData(
        var description: String,
        var range: IntRange,
        var value: Int
    ) : Data

    data class EnumEditData(
        var variants: Array<String>,
        var value: Int
    ) : Data {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EnumEditData

            if (!variants.contentEquals(other.variants)) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = variants.contentHashCode()
            result = 31 * result + value
            return result
        }
    }

    data class ListEditData(
        var variants: Array<String>,
        var value: KMutableProperty0<String>
    ) : Data {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ListEditData

            if (!variants.contentEquals(other.variants)) return false
            try {
                if (!value.get().equals(other.value.get())) return false
            } catch (e: Exception) {
                return false
            }

            return true
        }
    }

    data class IntEditData(
        var value: KMutableProperty0<Int>,
        var canEditFunc: (String, String) -> Boolean = { _, _ -> true },
        var range: IntRange
    ) : Data

    data class BoolEditData(
        var value: Boolean,
        var description: String
    ) : Data
}