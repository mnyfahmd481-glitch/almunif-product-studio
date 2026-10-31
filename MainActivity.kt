package com.almunif.productstudio

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.io.IOException

class MainActivity : Activity() {
    private val categories = listOf("العصارات", "الأفران", "الغسالات", "الشاشات", "البطاريات", "العروض", "منتجات جديدة")
    private var selectedCategory = categories.first()
    private lateinit var status: TextView
    private val PICK_IMAGES = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(12, 55, 112)
        window.navigationBarColor = android.graphics.Color.rgb(12, 55, 112)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 24)
            setBackgroundColor(android.graphics.Color.rgb(247, 249, 252))
        }
        val title = TextView(this).apply {
            text = "المنيف\nاستوديو المنتجات"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(12, 55, 112))
            setPadding(8, 10, 8, 22)
        }
        page.addView(title, LinearLayout.LayoutParams(-1, -2))

        val intro = TextView(this).apply {
            text = "اختر مجلد القسم، ثم أضف صور المنتج. ستُحفظ الصور في ألبوم مستقل يظهر ضمن ألبومات الصور في الهاتف، ويمكن اختيارها من واتساب الأعمال."
            textSize = 16f
            setTextColor(android.graphics.Color.DKGRAY)
            setPadding(4, 8, 4, 18)
        }
        page.addView(intro)

        val label = TextView(this).apply {
            text = "القسم"
            textSize = 18f
            setTextColor(android.graphics.Color.rgb(12, 55, 112))
        }
        page.addView(label)

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }
        }
        page.addView(spinner, LinearLayout.LayoutParams(-1, 58))

        val add = Button(this).apply {
            text = "＋  إضافة صور إلى منتج"
            textSize = 17f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.rgb(12, 55, 112)))
            setOnClickListener { askProductName() }
        }
        val addParams = LinearLayout.LayoutParams(-1, 58).apply { topMargin = 22 }
        page.addView(add, addParams)

        val folderInfo = Button(this).apply {
            text = "مكان حفظ الصور"
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("مجلدات المنيف")
                    .setMessage("Pictures/Almunif/اسم القسم/اسم المنتج\n\nتظهر الصور في تطبيق المعرض، وقد تظهر ألبوماتها داخل منتقي الصور في واتساب الأعمال بحسب إصدار واتساب وأندرويد.")
                    .setPositiveButton("حسناً", null).show()
            }
        }
        page.addView(folderInfo, LinearLayout.LayoutParams(-1, 52))

        status = TextView(this).apply {
            text = "جاهز لإضافة صور المنتجات"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(190, 30, 40))
            setPadding(8, 26, 8, 8)
        }
        page.addView(status, LinearLayout.LayoutParams(-1, -2))
        page.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        val foot = TextView(this).apply {
            text = "نسخة أولية 1.0  •  تنظيم الصور فقط – لا رسائل ولا ردود آلية"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.GRAY)
        }
        page.addView(foot)
        setContentView(page)
    }

    private fun askProductName() {
        val input = EditText(this).apply {
            hint = "مثال: عصارة براون J700"
            setSingleLine(true)
            setPadding(24, 18, 24, 18)
        }
        AlertDialog.Builder(this)
            .setTitle("اسم المنتج")
            .setMessage("ستُجمع الصور المختارة في مجلد خاص بهذا المنتج.")
            .setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("اختيار الصور") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) {
                    status.text = "اكتب اسم المنتج أولاً"
                } else {
                    pickImages(name)
                }
            }.show()
    }

    private fun pickImages(product: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pendingProduct = product
        startActivityForResult(intent, PICK_IMAGES)
    }

    private var pendingProduct = ""

    @Deprecated("Deprecated in Android API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_IMAGES || resultCode != Activity.RESULT_OK || data == null) return
        val uris = mutableListOf<Uri>()
        data.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) uris.add(clip.getItemAt(i).uri)
        } ?: data.data?.let { uris.add(it) }

        var saved = 0
        var failed = 0
        uris.forEach { uri ->
            try {
                if (copyToPictures(uri, selectedCategory, pendingProduct)) saved++ else failed++
            } catch (_: Exception) { failed++ }
        }
        status.text = "تم حفظ $saved صورة في ألبوم: $pendingProduct" +
                if (failed > 0) " • تعذر حفظ $failed" else ""
    }

    private fun copyToPictures(source: Uri, category: String, product: String): Boolean {
        val mime = contentResolver.getType(source) ?: "image/jpeg"
        val ext = when (mime) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
        val safeCategory = clean(category)
        val safeProduct = clean(product)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${safeProduct}_${System.currentTimeMillis()}$ext")
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Almunif/$safeCategory/$safeProduct")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val dest = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        try {
            contentResolver.openInputStream(source).use { input ->
                contentResolver.openOutputStream(dest).use { output ->
                    if (input == null || output == null) throw IOException("Cannot open image")
                    input.copyTo(output)
                }
            }
            val done = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            contentResolver.update(dest, done, null, null)
            return true
        } catch (e: Exception) {
            contentResolver.delete(dest, null, null)
            throw e
        }
    }

    private fun clean(value: String): String =
        value.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "منتج" }
}
