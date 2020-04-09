package com.doobie.pdfrendercomparison.ui.grid

import android.graphics.Bitmap
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.doobie.pdfrendercomparison.databinding.FragmentPdfGridBinding
import com.doobie.pdfrendercomparison.getPdfFile
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


//NOTE: Not hooked up. It turns out PdfiumCore doesnt really offer much
class GridFragmentWithLibraries : Fragment() {

    private lateinit var binding: FragmentPdfGridBinding

    lateinit var pdfiumCore: PdfiumCore
    lateinit var pdfDocument: PdfDocument

    var pageIndex = 0
    var xOffset = 0
    var yOffset = 0
    var scale = 1.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPdfGridBinding.inflate(inflater)
        val step = 100
        binding.buttonLeft.setOnClickListener { xOffset += step; loadPage() }
        binding.buttonRight.setOnClickListener { xOffset -= step; loadPage() }
        binding.buttonUp.setOnClickListener { yOffset -= step; loadPage() }
        binding.buttonDown.setOnClickListener { yOffset += step; loadPage() }
        binding.buttonZoomin.setOnClickListener { scale *= 2f; loadPage() }
        binding.buttonZoomout.setOnClickListener { scale /= 2f; loadPage() }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadPdf()
        loadPage()
    }

    fun loadPdf() {
        val file = getPdfFile(requireContext())
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfiumCore = PdfiumCore(context)
        pdfDocument = pdfiumCore.newDocument(fileDescriptor)
    }

    fun loadPage() {

        // Constants - bitmap size
        val w = 1024
        val h = 1024

        // Make bitmap
        val bitmap: Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(-0x1)

        pdfiumCore.openPage(pdfDocument, pageIndex)
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIndex, xOffset, yOffset, w, h)

        binding.imageView.setImageBitmap(bitmap)
    }

}
