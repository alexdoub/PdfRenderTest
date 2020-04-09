package com.doobie.pdfrendercomparison

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Alex Doub on 4/8/2020.
 */

val USE_THIS_PDF = R.raw.mobo_manual

fun getPdfFile(context: Context): File {
    val file = File(context.cacheDir, "tmp.pdf")
    if (file.exists()) {
        file.delete()
    }

    val input = context.resources.openRawResource(USE_THIS_PDF)
    val output = FileOutputStream(file)

    val buffer = ByteArray(1024)
    while (true) {
        val size = input.read(buffer)
        if (size == -1) {
            break;
        }
        output.write(buffer, 0, size)
    }

    input.close()
    output.close()
    return file
}